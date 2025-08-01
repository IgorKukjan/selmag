package ag.selm.customer.service;

import ag.selm.customer.entity.FavouriteProduct;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface FavouriteProductsService {

    Mono<FavouriteProduct> addProductToFavourites(int productId);

    Mono<Void> removeProductFromFavourites(int productId);

    Mono<FavouriteProduct> findFavouriteProductByProduct(int productId);

    Flux<FavouriteProduct> findFavouriteProducts();
}
