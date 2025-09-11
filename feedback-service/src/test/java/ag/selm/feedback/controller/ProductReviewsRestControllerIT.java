package ag.selm.feedback.controller;

import ag.selm.feedback.entity.ProductReview;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

//интеграционный тест
@SpringBootTest
//компонент, при помощи которого можно обращаться к web приложению
@AutoConfigureWebTestClient
@Slf4j
class ProductReviewsRestControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        this.reactiveMongoTemplate.insertAll(List.of(
                new ProductReview(UUID.fromString("bd7779c2-cb05-11ee-b5f3-df46a1249898"), 1, 1,
                        "Отзыв №1", "user-1"),
                new ProductReview(UUID.fromString("be424abc-cb05-11ee-ab16-2b747e61f570"), 1, 3,
                        "Отзыв №2", "user-2"),
                new ProductReview(UUID.fromString("be77f95a-cb05-11ee-91a3-1bdc94fa9de4"), 1, 5,
                        "Отзыв №3", "user-3")
        ))
                //заблокирует текущий поток, дождется пока это все будет добавлено
                //синхронно
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        this.reactiveMongoTemplate.remove(ProductReview.class).all().block();
    }

    @Test
    void findProductReviewsByProductId_ReturnsReviews() {
        // given
        // when
        //security -> запрос происходит от какого-то пользователя mockJwt
        this.webTestClient.mutateWith(mockJwt())
                //изменить текущий testClient и создать новый testClient с новыми настройкми
                .mutate().filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    log.info("========== REQUEST ==========");
                    log.info("{} {}", clientRequest.method(), clientRequest.url());
                    clientRequest.headers().forEach((header, value) -> log.info("{}: {}", header, value));
                    log.info("======== END REQUEST ========");
                    return Mono.just(clientRequest);
                }))
                //получить новый testClient
                .build()
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                // then
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("""
                        [
                            {
                                "id": "bd7779c2-cb05-11ee-b5f3-df46a1249898",
                                "productId": 1,
                                "rating": 1,
                                "review": "Отзыв №1",
                                "userId": "user-1"
                            },
                            {"id": "be424abc-cb05-11ee-ab16-2b747e61f570", "productId": 1, "rating": 3, "review": "Отзыв №2", "userId": "user-2"},
                            {"id": "be77f95a-cb05-11ee-91a3-1bdc94fa9de4", "productId": 1, "rating": 5, "review": "Отзыв №3", "userId": "user-3"}
                        ]""");
    }

    @Test
    void findProductReviewsByProductId_UserIsNotAuthenticated_ReturnsNotAuthorized() {
        // given
        // when
        //security -> запрос происходит от какого-то пользователя mockJwt
        this.webTestClient
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void createProductReview_RequestIsValid_ReturnsCreatedProductReview(){
        //given


        //when
        this.webTestClient
                //указать что пользователь имеет идентификатор
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                   {
                           "productId": 1,
                           "rating": 2,
                           "review": "На пятёрочку"          
                   }""")
        //then
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                    .json("""
                               {
                                       "productId": 1,
                                       "rating": 2,
                                       "review": "На пятёрочку",
                                       "userId": "user-tester"          
                               }""")
                //в корневом объекте есть свойство id и оно существует
                .jsonPath("$.id").exists();
    }

    @Test
    void createProductReview_RequestIsInvalid_ReturnsBadRequest(){
        //given


        //when
        this.webTestClient
                //указать что пользователь имеет идентификатор
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                   {
                           "productId": null,
                           "rating": -1,
                           "review": "На пятёрочку"          
                   }""")
                //then
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().doesNotExist(HttpHeaders.LOCATION)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json("""
                               {
                                       "errors": [
                                       "Товар не указан",
                                       "Оценка меньше 1"
                                       ]     
                               }""");
    }

    @Test
    void createProductReview_UserIsNotAuthenticated_ReturnsNotAuthorized(){
        //given


        //when
        this.webTestClient
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                   {
                           "productId": 1,
                           "rating": 2,
                           "review": "На пятёрочку"          
                   }""")
                //then
                .exchange()
                .expectStatus().isUnauthorized();
    }

}