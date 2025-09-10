package ag.selm.customer.client;

import ag.selm.customer.client.exception.ClientBadRequestException;
import ag.selm.customer.client.payload.NewFavouriteProductPayload;
import ag.selm.customer.entity.FavouriteProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class WebClientFavouriteProductsClient implements FavouriteProductsClient {

    private final WebClient webClient;

    @Override
    public Flux<FavouriteProduct> findFavouriteProducts() {
        return this.webClient
                .get()
                .uri("feedback-api/favourite-products")
                .retrieve().bodyToFlux(FavouriteProduct.class);
    }

    @Override
    public Mono<FavouriteProduct> findFavouriteProductByProductId(int productId) {
        return this.webClient
                .get()
                .uri("feedback-api/favourite-productsby-product-id/{productId}", productId)
                .retrieve().bodyToMono(FavouriteProduct.class)
                //когда нет указанного избранного  товара, то у нас там на той стороне пустой Mono
                //нужно это обработать. ошибка 404
                .onErrorComplete(WebClientResponseException.NotFound.class);
    }

    @Override
    public Mono<FavouriteProduct> addProductToFavourites(int productId) {
        return this.webClient
                .post()
                .uri("feedback-api/favourite-products")
                .bodyValue(new NewFavouriteProductPayload(productId))
                .retrieve().bodyToMono(FavouriteProduct.class)
                //невалидный запрос NewFavouriteProductPayload
                //WebClientResponseException нужно преобразовать к списку ошибок(не можем тк возвращаемый тип Mono<FavouriteProduct>)
                //либо преобразовать в другое исключение нами написанное ClientBadRequestException и более формализованное(если WebClient заменится, исключение тоже поменяется)
                .onErrorMap(WebClientResponseException.BadRequest.class,
                        exception -> new ClientBadRequestException("Возникла ошибка при добавление товара в избранные",
                                exception,
                                ((List<String>)exception.getResponseBodyAs(ProblemDetail.class)
                                        .getProperties().get("errors"))));
    }

    @Override
    public Mono<Void> removeProductFromFavourites(int productId) {
        return this.webClient
                .delete()
                .uri("feedback-api/favourite-products/by-product-id/{productId}", productId)
                .retrieve().toBodilessEntity()
                .then();
    }
}
