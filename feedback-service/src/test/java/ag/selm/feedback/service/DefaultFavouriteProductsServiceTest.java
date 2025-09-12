package ag.selm.feedback.service;

import ag.selm.feedback.entity.FavouriteProduct;
import ag.selm.feedback.repository.FavouriteProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultFavouriteProductsServiceTest {

    FavouriteProductRepository favouriteProductRepository = Mockito.mock(FavouriteProductRepository.class);

    DefaultFavouriteProductsService service = new DefaultFavouriteProductsService(favouriteProductRepository);

    @Test
    void addProductToFavourites_ReturnsCreatedFavouriteProduct() {
        //given
        doAnswer(invocation -> Mono.justOrEmpty(invocation.getArguments()[0]))
                .when(this.favouriteProductRepository).save(any());

        //when
        StepVerifier.create(this.service.addProductToFavourites(1, "user-1"))
        //then
                .expectNextMatches(favouriteProduct ->favouriteProduct.getProductId() == 1 &&
                        favouriteProduct.getUserId().equals("user-1") &&
                        favouriteProduct.getId() != null)
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.favouriteProductRepository).save(argThat(favouriteProduct ->
                favouriteProduct.getProductId() == 1 &&
                favouriteProduct.getUserId().equals("user-1") &&
                favouriteProduct.getId() != null));
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductRepository);
    }

    @Test
    void removeProductFromFavourites_ReturnsEmptyMono() {
        //given
        doReturn(Mono.empty())
                .when(this.favouriteProductRepository).deleteByProductIdAndUserId(1, "user-1");

        //when
        StepVerifier.create(this.service.removeProductFromFavourites(1, "user-1"))
                //then
//                .expectNext()
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.favouriteProductRepository).deleteByProductIdAndUserId(1, "user-1");
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductRepository);
    }

    @Test
    void findFavouriteProductByProduct_ReturnsFavouriteProduct() {
        //given
        doReturn(Mono.just(new FavouriteProduct(UUID.fromString("86efa22c-cbae-11ee-ab01-679baf165fb7"), 1 , "user-1")))
                .when(this.favouriteProductRepository).findByProductIdAndUserId(1, "user-1");

        //when
        StepVerifier.create(this.service.findFavouriteProductByProduct(1, "user-1"))
                //then
                .expectNext(new FavouriteProduct(UUID.fromString("86efa22c-cbae-11ee-ab01-679baf165fb7"), 1 , "user-1"))
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.favouriteProductRepository).findByProductIdAndUserId(1, "user-1");
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductRepository);
    }

    @Test
    void findFavouriteProducts_ReturnsFavouriteProducts() {
        //given
        doReturn(Flux.fromIterable(List.of(
                new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1, "user-1"),
                new FavouriteProduct(UUID.fromString( "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"), 2, "user-1"),
                new FavouriteProduct(UUID.fromString("23ff1d58-cbd8-11ee-9f4f-ef497a4e4799"), 3, "user-1")
        )))
                .when(this.favouriteProductRepository).findAllByUserId("user-1");

        //when
        StepVerifier.create(this.service.findFavouriteProducts("user-1"))
                //then
                .expectNext(new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1, "user-1"),
                        new FavouriteProduct(UUID.fromString( "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"), 2, "user-1"),
                        new FavouriteProduct(UUID.fromString("23ff1d58-cbd8-11ee-9f4f-ef497a4e4799"), 3, "user-1"))
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.favouriteProductRepository).findAllByUserId("user-1");
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductRepository);
    }
}