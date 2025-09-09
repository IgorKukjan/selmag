package ag.selm.feedback.service;

import ag.selm.feedback.entity.FavouriteProduct;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface FavouriteProductsService {

    Mono<FavouriteProduct> addProductToFavourites(int productId, String userId);

    Mono<Void> removeProductFromFavourites(int productId, String userId);

    Mono<FavouriteProduct> findFavouriteProductByProduct(int productId, String userId);

    Flux<FavouriteProduct> findFavouriteProducts(String userId);
}
