package ag.selm.customer.client;

import ag.selm.customer.client.exception.ClientBadRequestException;
import ag.selm.customer.client.payload.NewProductReviewPayload;
import ag.selm.customer.entity.ProductReview;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class WebClientProductReviewsClient implements ProductReviewsClient {

    private final WebClient webClient;

    @Override
    public Flux<ProductReview> findProductReviewsByProductId(Integer productId) {
        return this.webClient
                .get()
                .uri("feedback-api/product-reviews/by-product-id/{productId}", productId)
                .retrieve().bodyToFlux(ProductReview.class);
    }

    @Override
    public Mono<ProductReview> createProductReview(Integer productId, Integer rating, String reviews) {
        return this.webClient
                .post()
                .uri("feedback-api/product-reviews")
                .bodyValue(new NewProductReviewPayload(productId, rating, reviews))
                .retrieve().bodyToMono(ProductReview.class)
                //невалидный запрос NewProductReviewPayload
                //WebClientResponseException нужно преобразовать к списку ошибок(не можем тк возвращаемый тип Mono<ProductReview>)
                //либо преобразовать в другое исключение нами написанное ClientBadRequestException и более формализованное(если WebClient заменится, исключение тоже поменяется)
                .onErrorMap(WebClientResponseException.BadRequest.class,
                        exception -> new ClientBadRequestException("Возникла ошибка при добавление отзыва о товаре",
                                    exception,
                                    ((List<String>)exception.getResponseBodyAs(ProblemDetail.class)
                                            .getProperties().get("errors"))));
    }
}
