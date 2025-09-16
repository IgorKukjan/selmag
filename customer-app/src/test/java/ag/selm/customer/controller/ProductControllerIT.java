package ag.selm.customer.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

//интеграционный тест
@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
class ProductControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
          {
            "id": 1,
            "title": "Название товара №1",
            "details": "Описание товара №1"
          }
          """)
                        //относится к ответу
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void getProductPage_ProductExists_ReturnsProductPage(){
        //given
        WireMock.stubFor(WireMock.get("/feedback-api/product-reviews/by-product-id/1")
                .willReturn(okJson("""
                [
                   {
                       "id": "595d4e5a-cbc1-11ee-864f-8fb72674ccaf",
                       "productId": 1,
                       "rating": 3,
                       "review": "Ну, на троечку...",
                       "userId": "user-1"
                   },
                   {
                       "id": "63c4410a-cbc1-11ee-92ea-eff590e7852e",
                       "productId": 1,
                       "rating": 5,
                       "review": "Отличный товар!",
                       "userId": "user-2"
                   }
                ]
                """)
                        //относится к ответу
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        WireMock.stubFor(WireMock.get("/feedback-api/favourite-products/by-product-id/1")
                .willReturn(created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                        "id": "2ecc74c2-cb17-11ee-b719-e35a0e241f11",
                                        "productId": 1
                                }
                                """)));

        //when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/1")
                .exchange()
        //then
                .expectStatus().isOk();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalogue-api/products/1")));
        WireMock.verify(getRequestedFor(urlPathMatching("/feedback-api/product-reviews/by-product-id/1")));
        WireMock.verify(getRequestedFor(urlPathMatching("/feedback-api/favourite-products/by-product-id/1")));
    }

    @Test
    void getProductPage_ProductDoesNotExist_ReturnsNotFound() {
        //given

        //when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .get()
                .uri("/customer/products/404")
                //then
                .exchange()
                .expectStatus().isNotFound();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalogue-api/products/404")));
    }

    @Test
    void getProductPage_UserIsNotAuthorized_RedirectsToLoginPage(){
        // given

        // when
        this.webTestClient
                .get()
                .uri("/customer/products/1")
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void addProductToFavourites_RequestIsValid_ReturnsRedirectionToProductPage(){
        //given
        WireMock.stubFor(WireMock.post("/feedback-api/favourite-products")
                .withRequestBody(WireMock.equalToJson("""
                 {
                    "productId": 1
                 }
                """))
                //относится к запросу
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(created()
                        //относится к ответу
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": "2ecc74c2-cb17-11ee-b719-e35a0e241f11",
                                    "productId": 1
                                }
                                """)));

        //when
        this.webTestClient
                .mutateWith(mockUser())
                //автоматически в запрос подставлялся csrf-token
                //тк включена защита от csrf
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/add-to-favourites")
                .exchange()
        //then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        WireMock.verify(getRequestedFor(urlPathMatching("/catalogue-api/products/1")));
        WireMock.verify(postRequestedFor(urlPathMatching("/feedback-api/favourite-products"))
                .withRequestBody(equalToJson("""
                  {
                    "productId": 1
                  }
              """)));
    }

    @Test
    void addProductToFavourites_ProductDoesNotExists_ReturnsNotFoundPage(){
        //given

        //when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/404/add-to-favourites")
                //then
                .exchange()
                .expectStatus().isNotFound();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalogue-api/products/404")));
    }

    @Test
    void addProductToFavourites_UserIsNotAuthorized_RedirectsToLoginPage(){
        //given

        //when
        this.webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/add-to-favourites")
                .exchange()
        //then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void removeProductFromFavourites_ProductExists_ReturnsRedirectionToProductPage(){
        //given
        WireMock.stubFor(WireMock.delete("/feedback-api/favourite-products/by-product-id/1")
                .willReturn(noContent()));

        //when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/remove-from-favourites")
                .exchange()
        //then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products/1")));
        verify(deleteRequestedFor(urlPathMatching("/feedback-api/favourite-products/by-product-id/1")));
    }

    @Test
    void removeProductFromFavourites_ProductDoesNotExist_ReturnsNotFoundPage(){
        //given

        //when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/404/remove-from-favourites")
                .exchange()
                //then
                .expectStatus().isNotFound();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products/404")));
    }

    @Test
    void removeProductFromFavourites_UserIsNotAuthorized_RedirectsToLoginPage(){
        //given

        //when
        this.webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/remove-from-favourites")
                .exchange()
        //then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }


    @Test
    void createReview_RequestIsValid_ReturnsRedirectionToProductPage(){
        //given
        WireMock.stubFor(WireMock.post("/feedback-api/product-reviews")
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                          "productId": 1,
                          "rating": 3,
                          "review": "Ну, на троечку"
                        }
                        """))
                .willReturn(created()
                        .withHeader(HttpHeaders.LOCATION, "http://localhost/feedback-api/product-reviews/b852bc8e-cbc5-11ee-bbc5-bf192e2492e5")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": "b852bc8e-cbc5-11ee-bbc5-bf192e2492e5",
                                    "productId": 1,
                                    "rating": 3,
                                    "review": "Ну, на троечку",
                                    "userId": "user-1"
                                }
                                """)));

        //when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/create-review")
                .body(BodyInserters.fromFormData("rating", "3")
                        .with("review", "Ну, на троечку"))
                .exchange()
        //then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        verify(postRequestedFor(urlPathMatching("/feedback-api/product-reviews"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                                                {
                                                  "productId": 1,
                                                  "rating": 3,
                                                  "review": "Ну, на троечку"
                                                }
                                                """)));
    }

    @Test
    void createReview_RequestIsInvalid_ReturnsProductPage(){
        //given
        WireMock.stubFor(WireMock.post("/feedback-api/product-reviews")
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                          "productId": 1,
                          "rating": -1,
                          "review": "Ну, на троечку"
                        }
                        """))
                .willReturn(badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                     "errors": ["Ошибка 1", "Ошибка 2"]
                                }
                                """)));

        WireMock.stubFor(WireMock.get("/feedback-api/favourite-products/by-product-id/1")
                .willReturn(okJson("""
                               {
                                  "id": "ec586ecc-cbc8-11ee-8e7d-4fce5e860855",
                                  "productId": 1,
                                  "userId": "user-1"
                               }
                              """)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        //when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/create-review")
                .body(BodyInserters.fromFormData("rating", "-1")
                        .with("review", "Ну, на троечку"))
                .exchange()
                //then
                .expectStatus().isBadRequest();

        verify(postRequestedFor(urlPathMatching("/feedback-api/product-reviews"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                                                {
                                                  "productId": 1,
                                                  "rating": -1,
                                                  "review": "Ну, на троечку"
                                                }
                                                """)));
    }

    @Test
    void createReview_ProductDoesNotExist_ReturnsNotFoundPage(){
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/404/create-review")
                .body(BodyInserters.fromFormData("rating", "3")
                        .with("review", "Ну, на троечку"))
                .exchange()
                // then
                .expectStatus().isNotFound();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products/404")));
    }

    @Test
    void createReview_UserIsNotAuthorized_RedirectsToLoginPage(){
        //given

        //when
        this.webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/create-review")
                .exchange()
        //then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }
}