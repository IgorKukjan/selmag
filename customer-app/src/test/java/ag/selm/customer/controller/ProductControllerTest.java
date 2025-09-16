package ag.selm.customer.controller;

import ag.selm.customer.client.FavouriteProductsClient;
import ag.selm.customer.client.ProductReviewsClient;
import ag.selm.customer.client.ProductsClient;
import ag.selm.customer.client.exception.ClientBadRequestException;
import ag.selm.customer.controller.payload.NewProductReviewPayload;
import ag.selm.customer.entity.FavouriteProduct;
import ag.selm.customer.entity.Product;
import ag.selm.customer.entity.ProductReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.ui.ConcurrentModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    ProductsClient productsClient;

    @Mock
    FavouriteProductsClient favouriteProductsClient;

    @Mock
    ProductReviewsClient productReviewsClient;

    @InjectMocks
    ProductController controller;

    @Test
    void loadProduct_ProductExists_ReturnsNotEmptyMono(){
        //given
        var product = new Product(1, "Товар №1", "Описание товара №1");
        doReturn(Mono.just(product))
                .when(this.productsClient).findProduct(1);

        //when
        //создать stream, запустить его
        StepVerifier.create(this.controller.loadProduct(1))
                //then
                //проверить содержимое стрима
                .expectNext(new Product(1, "Товар №1", "Описание товара №1"))
                //ожидаю что stream завершен успешно
                .expectComplete()
                //провалидирует, что более ничего не происходит со стримом
                .verify();

        //проверка действительно был вызван метод
        verify(this.productsClient).findProduct(1);
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.productsClient);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.favouriteProductsClient, this.productReviewsClient);
    }

    @Test
    void loadProduct_ProductDoesNotExist_ReturnsMonoWithNoSuchElementException(){
        //given
        doReturn(Mono.empty())
                .when(productsClient).findProduct(1);

        //when
        //создать stream, запустить его
        StepVerifier.create(this.controller.loadProduct(1))
                //then
                .expectErrorMatches(exception -> exception instanceof NoSuchElementException e
                        && e.getMessage().equals("customer.products.error.not_found"))
                //провалидирует, что более ничего не происходит со стримом
                .verify();

        //проверка действительно был вызван метод
        verify(this.productsClient).findProduct(1);
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.productsClient);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.favouriteProductsClient, this.productReviewsClient);
    }

    @Test
    void getProductPage_ReturnsProductPage(){
        //given
        var model = new ConcurrentModel();

        var productReviews = List.of(
                new ProductReview(UUID.fromString("6a8512d8-cbaa-11ee-b986-376cc5867cf5"), 1, 5, "На пятёрочку"),
                new ProductReview(UUID.fromString("849c3fac-cbaa-11ee-af68-737c6d37214a"), 1, 4, "Могло быть и лучше"));

        doReturn(Flux.fromIterable(productReviews))
                .when(this.productReviewsClient).findProductReviewsByProductId(1);

        var favvouriteProduct = new FavouriteProduct(UUID.fromString("af5f9496-cbaa-11ee-a407-27b46917819e"), 1);

        doReturn(Mono.just(favvouriteProduct))
                .when(this.favouriteProductsClient).findFavouriteProductByProductId(1);

        //when
        //создать stream, запустить его
        StepVerifier.create(this.controller.getProductPage( Mono.just(new Product(1, "Товар №1", "Описание товара №1")), model))
        //then
                .expectNext("customer/products/product")
                .verifyComplete();

        assertEquals(productReviews, model.getAttribute("reviews"));
        assertEquals(true, model.getAttribute("inFavourite"));

        //проверка действительно был вызван метод
        verify(this.productReviewsClient).findProductReviewsByProductId(1);
        verify(this.favouriteProductsClient).findFavouriteProductByProductId(1);
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.productReviewsClient, this.favouriteProductsClient);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.productsClient);
    }

    @Test
    void addProductToFavourites_RequestIsValid_RedirectsToProductPage(){
        //given
        var favouriteProduct = new FavouriteProduct(UUID.fromString("25ec67b4-cbac-11ee-adc8-4bd80e8171c4"), 1);
        doReturn(Mono.just(favouriteProduct))
                .when(this.favouriteProductsClient).addProductToFavourites(1);

        //when
        StepVerifier.create(this.controller.addProductToFavourites(Mono.just(new Product(1, "Товар №1", "Описание товара №1"))))
        //then
                //Первый сигнал проверяется с помощью expectNext
                .expectNext("redirect:/customer/products/1")
                //используем verify()  для запуска нашего теста
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.favouriteProductsClient).addProductToFavourites(1);
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductsClient);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.productReviewsClient, this.productsClient);
    }

    @Test
    void addProductToFavourites_RequestIsInvalid_RedirectsToProductPage(){
        //given
        doReturn(Mono.error(new ClientBadRequestException("Возникла какая-то ошибка", null,
                List.of("Какая-то ошибка"))))
                .when(this.favouriteProductsClient).addProductToFavourites(1);

        //when
        StepVerifier.create(this.controller.addProductToFavourites(Mono.just(new Product(1, "Товар №1", "Описание товара №1"))))
        //then
                //Первый сигнал проверяется с помощью expectNext
                .expectNext("redirect:/customer/products/1")
                //используем verify()  для запуска нашего теста
                .verifyComplete();

        //проверка действительно был вызван метод
        verify(this.favouriteProductsClient).addProductToFavourites(1);
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductsClient);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.productReviewsClient, this.productsClient);
    }

    @Test
    void removeProductFromFavourites_RedirectsToProductPage(){
        //given
        doReturn(Mono.empty())
                .when(this.favouriteProductsClient).removeProductFromFavourites(1);

        //when
        StepVerifier.create(this.controller.removeProductFromFavourites(Mono.just(new Product(1, "Товар №1", "Описание товара №1" ))))
                //Первый сигнал проверяется с помощью expectNext
                .expectNext("redirect:/customer/products/1")
                //используем verify()  для запуска нашего теста
                .verifyComplete();

        //then

        //проверка действительно был вызван метод
        verify(this.favouriteProductsClient).removeProductFromFavourites(1);
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductsClient);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.productReviewsClient, this.productsClient);
    }

    @Test
    void createReview_RequestIsValid_RedirectsToProductPage(){
        //given
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        doReturn(Mono.just(new ProductReview(UUID.fromString("86efa22c-cbae-11ee-ab01-679baf165fb7"), 1, 3, "Ну, на троечку...")))
                .when(this.productReviewsClient).createProductReview(1, 3, "Ну на троечку");

        //when
        StepVerifier.create(this.controller.createReview( Mono.just(new Product(1, "Товар №1", "Описание товара №1")), new NewProductReviewPayload(3, "Ну на троечку"), model, response))
        //then
                //Первый сигнал проверяется с помощью expectNext
                .expectNext("redirect:/customer/products/1")
                //используем verify()  для запуска нашего теста
                .verifyComplete();

        assertNull(response.getStatusCode());

        //проверка действительно был вызван метод
        verify(this.productReviewsClient).createProductReview(1, 3, "Ну на троечку");
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.productReviewsClient);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.favouriteProductsClient, this.productsClient);
    }

    @Test
    void createReview_RequestIsInvalid_ReturnsProductPageWithPayloadAndErrors(){
        //given
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        doReturn(Mono.error(new ClientBadRequestException("Возникла какае-то ошибка",
                null,
                List.of("Ошибка 1", "Ошибка 2"))))
                .when(this.productReviewsClient).createProductReview(1, null, "Ну на троечку");

        doReturn(Mono.just(new FavouriteProduct(UUID.fromString("af5f9496-cbaa-11ee-a407-27b46917819e"), 1)))
                .when(this.favouriteProductsClient).findFavouriteProductByProductId(1);

        //when
        StepVerifier.create(this.controller.createReview( Mono.just(new Product(1, "Товар №1", "Описание товара №1")), new NewProductReviewPayload(null, "Ну на троечку"), model, response))
        //then
                //Первый сигнал проверяется с помощью expectNext
                .expectNext("customer/products/product")
                //используем verify()  для запуска нашего теста
                .verifyComplete();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals(true, model.getAttribute("inFavourite"));
        assertEquals(new NewProductReviewPayload(null, "Ну на троечку"), model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));

        //проверка действительно был вызван метод
        verify(this.productReviewsClient).createProductReview(1, null, "Ну на троечку");
        verify(this.favouriteProductsClient).findFavouriteProductByProductId(1);
        //более не было никаких вызовов у данного mock объекта
        verifyNoMoreInteractions(this.favouriteProductsClient, this.productReviewsClient);
        //не было вызовов к другим mock объектам
        verifyNoInteractions(this.productsClient);
    }

    @Test
    @DisplayName("Исключение NoSuchElementException должно быть транслировано в страницу errors/404 ")
    void handleNoSuchElementException_ReturnsErrors404(){
        //given
        var exception  = new NoSuchElementException("Товар не найден");
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        //when
        var result = this.controller.handleNoSuchElementException(exception, model, response);

        //then
        assertEquals("errors/404", result);
        assertEquals("Товар не найден", model.getAttribute("error"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}