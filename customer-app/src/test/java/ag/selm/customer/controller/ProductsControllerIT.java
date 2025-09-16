package ag.selm.customer.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

//интеграционный тест
@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
class ProductsControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("фильтр"))
                .willReturn(okJson("""
                     [
                        {
                            "id": 1,
                            "title": "Отфильтрованный товар №1",
                            "details": "Описание отфильтрованного товара №1"
                        },
                        {
                            "id": 2,
                            "title": "Отфильтрованный товар №2",
                            "details": "Описание отфильтрованного товара №2"
                        },
                        {
                            "id": 3,
                            "title": "Отфильтрованный товар №3",
                            "details": "Описание отфильтрованного товара №3"
                        }
                     ]
                     """)));
    }

    @Test
    void getProductsListPage_ReturnsProductsPage(){
        //given


        //when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/list?filter=фильтр")
                .exchange()
        //then
                .expectStatus().isOk();

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("фильтр")));
    }

    @Test
    void getProductsListPage_UserIsNotAuthenticated_RedirectsToLoginPage(){
        //given

        //when
        this.webTestClient
                .get()
                .uri("/customer/products/list")
                .exchange()
                //then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }

    @Test
    void getFavouriteProductsPage_ReturnsFavouriteProductsPage(){
        //given
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/feedback-api/favourite-products"))
                .willReturn(okJson("""
                    [
                        {
                            "id": "a16f0218-cbaf-11ee-9e6c-6b0fa3631587",
                            "productId": 1,
                            "userId": "user-1"
                        },
                        {
                            "id": "a42ff37c-cbaf-11ee-8b1d-cb00912914b5",
                            "productId": 3,
                            "userId": "user-1"
                        }
                    ]
                                  """)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        //when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/favourites?filter=фильтр")
                .exchange()
        //then
                .expectStatus().isOk();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", equalTo("фильтр")));
        verify(getRequestedFor(urlPathMatching("/feedback-api/favourite-products")));
    }

    @Test
    void getFavouriteProductsPage_UserIsNotAuthenticated_RedirectsToLoginPage(){
        //given

        //when
        this.webTestClient
                .get()
                .uri("/customer/products/favourites")
                .exchange()
        //then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }
}