package ag.selm.feedback.controller;

import ag.selm.feedback.controller.payload.NewFavouriteProductPayload;
import ag.selm.feedback.entity.FavouriteProduct;
import ag.selm.feedback.service.FavouriteProductsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

class FavouriteProductsRestControllerTest {

    FavouriteProductsService favouriteProductsService = Mockito.mock(FavouriteProductsService.class);

    FavouriteProductsRestController controller = new FavouriteProductsRestController(favouriteProductsService);

    @Test
    void findFavouriteProducts_ReturnsFavouriteProducts() {
        //given
        var jwtAuthenticationToken = new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                .headers(headers -> headers.put("foo", "bar"))
                .claim("sub", "user-1").build());

        var favouriteProducts = List.of(
                new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1, "user-1"),
                new FavouriteProduct(UUID.fromString("23ff1d58-cbd8-11ee-9f4f-ef497a4e4799"), 2, "user-1"),
                new FavouriteProduct(UUID.fromString("5a9ba234-cbd6-11ee-acab-5748ca6678b9"), 3, "user-1")
        );

        doReturn(Flux.fromIterable(favouriteProducts))
                .when(this.favouriteProductsService).findFavouriteProducts("user-1");

        //when
        StepVerifier.create(this.controller.findFavouriteProducts(Mono.just(jwtAuthenticationToken)))
        //then
                .expectNext(new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1, "user-1"),
                        new FavouriteProduct(UUID.fromString("23ff1d58-cbd8-11ee-9f4f-ef497a4e4799"), 2, "user-1"),
                        new FavouriteProduct(UUID.fromString("5a9ba234-cbd6-11ee-acab-5748ca6678b9"), 3, "user-1"))
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.favouriteProductsService).findFavouriteProducts("user-1");
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductsService);
        //не было вызовов к другим mock объектам

    }

    @Test
    void findFavouriteProductByProductId_ReturnsFavouriteProduct() {
        //given
        var jwtAuthenticationToken = new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                .headers(headers -> headers.put("foo", "bar"))
                .claim("sub", "user-1").build());

        doReturn(Mono.just(new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1, "user-1")))
                .when(this.favouriteProductsService).findFavouriteProductByProduct(1, "user-1");

        //when
        StepVerifier.create(this.controller.findFavouriteProductByProductId(Mono.just(jwtAuthenticationToken), 1))
        //then
                .expectNext(new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1, "user-1"))
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.favouriteProductsService).findFavouriteProductByProduct(1, "user-1");
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductsService);
        //не было вызовов к другим mock объектам
    }

    @Test
    void addProductToFavourites_ReturnsCreatedFavouriteProduct() {
        //given
        var jwtAuthenticationToken = new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                .headers(headers -> headers.put("foo", "bar"))
                .claim("sub", "user-1").build());

        doReturn(Mono.just(new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1 , "user-1")))
                .when(this.favouriteProductsService).addProductToFavourites(1, "user-1");

        //when
        StepVerifier.create(this.controller.addProductToFavourites(
                        Mono.just(jwtAuthenticationToken),
                        Mono.just(new NewFavouriteProductPayload(1)),
                        UriComponentsBuilder.fromUriString("http://localhost")))
        //then
                .expectNext(ResponseEntity.created(URI.create("http://localhost/feedback-api/favourite-products/fe87eef6-cbd7-11ee-aeb6-275dac91de02"))
                        .body(new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1, "user-1")))
                        .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.favouriteProductsService).addProductToFavourites(1, "user-1");
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductsService);
        //не было вызовов к другим mock объектам
    }

    @Test
    void removeProductFromFavourites_ReturnsNoContent() {
        //given
        var jwtAuthenticationToken = new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                .headers(headers -> headers.put("foo", "bar"))
                .claim("sub", "user-1").build());

        doReturn(Mono.empty())
                .when(this.favouriteProductsService).removeProductFromFavourites(1, "user-1");

        //when
        StepVerifier.create(this.controller.removeProductFromFavourites(Mono.just(jwtAuthenticationToken), 1))
        //then
                .expectNext(ResponseEntity.noContent().build())
                .verifyComplete();



        //проверка действительно был вызван метод
        verify(this.favouriteProductsService).removeProductFromFavourites(1, "user-1");
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductsService);
        //не было вызовов к другим mock объектам
    }
}