package ag.selm.feedback.repository;

import ag.selm.feedback.entity.FavouriteProduct;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FavouriteProductRepository extends ReactiveCrudRepository<FavouriteProduct, UUID> {

//    Mono<FavouriteProduct> save(FavouriteProduct favouriteProduct);

//    Mono<Void> deleteByProductId(int productId);

//    Mono<FavouriteProduct> findByProductId(int productId);

    Flux<FavouriteProduct> findAllByUserId(String userId);

    Mono<FavouriteProduct> findByProductIdAndUserId(int productId, String userId);

    Mono<Void> deleteByProductIdAndUserId(int productId, String userId);

//    Flux<FavouriteProduct> findAll();
}
