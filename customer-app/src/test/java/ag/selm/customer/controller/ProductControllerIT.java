package ag.selm.customer.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

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

    @Test
    void addProductToFavourites_RequestIsValid_ReturnsRedirectionToProductPage(){
        //given
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
          {
            "id": 1,
            "title": "Название товара №1",
            "details": "Описание товара №1"
          }
          """)
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        WireMock.stubFor(WireMock.post("/feedback-api/favourite-products")
                .withRequestBody(WireMock.equalToJson("""
                 {
                    "productId": 1
                 }
                """))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
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
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/add-to-favourites")
        //then
                .exchange()
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
                .uri("/customer/products/1/add-to-favourites")
                //then
                .exchange()
                .expectStatus().isNotFound();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalogue-api/products/1")));
    }
}