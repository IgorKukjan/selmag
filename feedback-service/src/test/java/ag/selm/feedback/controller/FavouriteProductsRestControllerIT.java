package ag.selm.feedback.controller;

import ag.selm.feedback.entity.FavouriteProduct;
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

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

//интеграционный тест
@SpringBootTest
//компонент, при помощи которого можно обращаться к web приложению
@AutoConfigureWebTestClient
@Slf4j
class FavouriteProductsRestControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp(){
        this.reactiveMongoTemplate.insertAll(List.of(
                new FavouriteProduct(UUID.fromString("bd7779c2-cb05-11ee-b5f3-df46a1249898"), 1, "user-1"),
                new FavouriteProduct(UUID.fromString("be424abc-cb05-11ee-ab16-2b747e61f570"), 2, "user-2"),
                new FavouriteProduct(UUID.fromString("be77f95a-cb05-11ee-91a3-1bdc94fa9de4"), 3, "user-1")
        ))
                //заблокирует текущий поток, дождется пока это все будет добавлено
                //синхронно
                .blockLast();
    }

    @AfterEach
    void tearDown(){
        this.reactiveMongoTemplate.remove(FavouriteProduct.class).all().block();
    }

    @Test
    void findFavouriteProducts_ReturnsFavouriteProducts(){
        //given
        //when
        //security -> запрос происходит от какого-то пользователя mockJwt
        this.webTestClient
                .mutateWith(mockJwt()
                        .jwt(builder -> builder.subject("user-1")))
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
        //then
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("""
                    [
                          {
                            "id": "bd7779c2-cb05-11ee-b5f3-df46a1249898",
                            "productId": 1,
                            "userId": "user-1"
                          },
                          {
                            "id": "be77f95a-cb05-11ee-91a3-1bdc94fa9de4",
                            "productId": 3,
                            "userId": "user-1"
                          }
                    ]
                    """
                );
    }

    @Test
    void findFavouriteProducts_UserIsNotAuthenticated_ReturnsNotAuthorized(){
        //given
        //when
        //security -> запрос происходит от какого-то пользователя mockJwt
        this.webTestClient
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
                //then
                .expectStatus().isUnauthorized();
    }

    @Test
    void findFavouriteProductByProductId_ReturnsFavouriteProduct(){
        //given
        //when
        //security -> запрос происходит от какого-то пользователя mockJwt
        this.webTestClient.mutateWith(mockJwt()
                        .jwt(builder ->  builder.subject("user-1")))
                .get()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
        //then
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json(
                        """
                          {
                            "id": "bd7779c2-cb05-11ee-b5f3-df46a1249898",
                            "productId": 1,
                            "userId": "user-1"
                          }
                          """
                );

    }

    @Test
    void findFavouriteProductByProductId_UserIsNotAuthenticated_ReturnsNotAuthorized(){
        //given
        //when
        //security -> запрос происходит от какого-то пользователя mockJwt
        this.webTestClient
                .get()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                //then
                .expectStatus().isUnauthorized();

    }

    @Test
    void addProductToFavourites_RequestIsValid_ReturnsCreatedFavouriteProduct() {
        //given
        //when
        this.webTestClient
                .mutateWith(mockJwt()
                        .jwt(builder -> builder.subject("user-1")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 1                       
                        } 
                        """)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json(
                        """
                                      {
                                        "productId": 1,
                                        "userId": "user-1"
                                      }
                                    """
                ).jsonPath("$.id").exists();
    }

    @Test
    void addProductToFavourites_RequestIsInvalid_ReturnsBadRequest() {
        //given
        //when
        this.webTestClient
                .mutateWith(mockJwt()
                        .jwt(builder -> builder.subject("user-1")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": null                      
                        } 
                        """)
                .exchange()
        //then
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json(
                        """
                                      {
                                       "errors": ["Товар не указан"]
                                      }
                                    """
                );
    }

    @Test
    void addProductToFavourites_UserIsNotAuthenticated_ReturnsNotAuthorized() {
        // given
        // when
        this.webTestClient
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": null
                        }""")
                .exchange()
        // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void removeProductFromFavourites_ReturnsNoContent() {
        //given
        //when
        this.webTestClient.mutateWith(mockJwt())
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
        //then
                .expectStatus().isNoContent();
    }

    @Test
    void removeProductFromFavourites_UserIsNotAuthenticated_ReturnsNotAuthorized() {
        //given
        //when
        this.webTestClient
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                //then
                .expectStatus().isUnauthorized();
    }
}