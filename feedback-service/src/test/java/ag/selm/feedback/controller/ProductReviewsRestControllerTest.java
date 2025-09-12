package ag.selm.feedback.controller;

import ag.selm.feedback.controller.payload.NewProductReviewPayload;
import ag.selm.feedback.entity.ProductReview;
import ag.selm.feedback.service.ProductReviewsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductReviewsRestControllerTest {

    ProductReviewsService productReviewsService = Mockito.mock(ProductReviewsService.class);

    ReactiveMongoTemplate reactiveMongoTemplate = Mockito.mock(ReactiveMongoTemplate.class);

    ProductReviewsRestController controller = new ProductReviewsRestController(productReviewsService, reactiveMongoTemplate);

    @Test
    void findProductReviewsByProductId_ReturnsProductReviews() {
        //given
        var productReviews = List.of(
            new ProductReview(UUID.fromString("bd7779c2-cb05-11ee-b5f3-df46a1249898"), 1, 1, "на 1", "user-1"),
            new ProductReview(UUID.fromString("be424abc-cb05-11ee-ab16-2b747e61f570"), 1, 3, "на 3", "user-2"),
            new ProductReview(UUID.fromString("be77f95a-cb05-11ee-91a3-1bdc94fa9de4"), 1, 5, "на 5", "user-3")
        );

        var principal = Mockito.mock(JwtAuthenticationToken.class);
        var jwtAuthenticationToken = new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                .headers(headers -> headers.put("foo", "bar"))
                .claim("sub", "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c").build());

        doReturn(Flux.fromIterable(productReviews))
                .when(this.productReviewsService).findProductReviewsByProduct(1);

        //when
        StepVerifier.create(this.controller.findProductReviewsByProductId(1,
                Mono.just(principal),
                Mono.just(jwtAuthenticationToken)))
        //then
                .expectNext(new ProductReview(UUID.fromString("bd7779c2-cb05-11ee-b5f3-df46a1249898"), 1, 1, "на 1", "user-1"),
                        new ProductReview(UUID.fromString("be424abc-cb05-11ee-ab16-2b747e61f570"), 1, 3, "на 3", "user-2"),
                        new ProductReview(UUID.fromString("be77f95a-cb05-11ee-91a3-1bdc94fa9de4"), 1, 5, "на 5", "user-3"))
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.productReviewsService).findProductReviewsByProduct(1);
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.productReviewsService);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.reactiveMongoTemplate);
    }

    @Test
    void createProductReview_ReturnsCreatedProductReview() {
        //given
        var jwtAuthenticationToken = new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                .headers(headers -> headers.put("foo", "bar"))
                .claim("sub", "user-1").build());

        var newProductReviewPayload = new  NewProductReviewPayload(1, 3, "На 3");

        var productReview = new ProductReview(UUID.fromString("5a9ba234-cbd6-11ee-acab-5748ca6678b9"), 1, 3, "на 3", "user-1");
        doReturn(Mono.just(productReview))
                .when(this.productReviewsService).createProductReview(1, 3, "На 3", "user-1");

        //when
        StepVerifier.create(this.controller.createProductReview(Mono.just(jwtAuthenticationToken),
                Mono.just(newProductReviewPayload),
                UriComponentsBuilder.fromUriString("http://localhost")))
        //then
                .expectNext(ResponseEntity.created(URI.create("http://localhost/feedback-api/product-reviews/5a9ba234-cbd6-11ee-acab-5748ca6678b9"))
                        .body(new ProductReview(UUID.fromString("5a9ba234-cbd6-11ee-acab-5748ca6678b9"), 1, 3, "на 3", "user-1")))
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.productReviewsService).createProductReview(1, 3, "На 3", "user-1");
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.productReviewsService);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.reactiveMongoTemplate);
    }
}