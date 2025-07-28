package ag.selm.manager.controller;

import ag.selm.manager.controller.payload.NewProductPayload;
import ag.selm.manager.controller.payload.UpdateProductPayload;
import ag.selm.manager.entity.Product;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//для того чтобы запустить контекст приложения в тестовом режиме
//для того чтобы можно было обращаться к нашему приложению
@SpringBootTest
//Позволит обращаться к нашему приложению по http(по факту обращение внутри приложения)
@AutoConfigureMockMvc
//поднимает реальный компонент, который имеет порт на машине с замоканным поведением
@WireMockTest(httpPort = 54321)
class ProductControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getProduct_ProductExists_ReturnsProductPage() throws Exception {
        //given - описываем параметры запроса, которые собираемся отправить в MockMvc
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("j.dewar").roles("MANAGER"));

        //замокать поведение сервиса
        //WireMock.get-GET-запрос на url "/catalogue-api/products/1"
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .willReturn(WireMock
                        .ok/*статус*/("""
                                {"id": 1, "title": "Товар №1", "details": "Описание товара №1"}
                                """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        //when - выполняем запрос
        this.mockMvc.perform(requestBuilder)

        //then - манипуляции с результатом
                .andDo(print())//вывести в лог
                .andExpectAll(
                     status().isOk(),
                     view().name("catalogue/products/product"),
                     model().attribute("product", new Product(1, "Товар №1", "Описание товара №1"))
                );

        //Можно провалидировать, что вызов данного метода у нас был
        //getRequestedFor-GET-запрос "/catalogue-api/products/1"
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1")));
    }

    @Test
    void getProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        //given - описываем параметры запроса, которые собираемся отправить в MockMvc
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("j.dewar").roles("MANAGER"));

        //замокать поведение сервиса
        //WireMock.get-GET-запрос на url "/catalogue-api/products/1"
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .willReturn(WireMock
                        .notFound()));

        //when - выполняем запрос
        this.mockMvc.perform(requestBuilder)

                //then - манипуляции с результатом
                .andDo(print())//вывести в лог
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error",  "Товар не найден")
                );

        //Можно провалидировать, что вызов данного метода у нас был
        //getRequestedFor-GET-запрос "/catalogue-api/products/1"
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1")));
    }

    @Test
    void getProductEditPage_ProductExists_ReturnsProductEditPage()throws Exception {
        //given - описываем параметры запроса, которые собираемся отправить в MockMvc
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("j.dewar").roles("MANAGER"));

        //замокать поведение сервиса
        //WireMock.get-GET-запрос на url "/catalogue-api/products/1"
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .willReturn(WireMock
                        .ok/*статус*/("""
                                {"id": 1, "title": "Товар №1", "details": "Описание товара №1"}
                                """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        //when - выполняем запрос
        this.mockMvc.perform(requestBuilder)

        //then - манипуляции с результатом
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/edit"),
                        model().attribute("product", new Product(1, "Товар №1", "Описание товара №1"))
                );

        //Можно провалидировать, что вызов данного метода у нас был
        //getRequestedFor-GET-запрос "/catalogue-api/products/1"
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching(("/catalogue-api/products/1"))));
    }

    @Test
    void getProductEditPage_ProductDoesNotExist_ReturnsError404Page() throws Exception{
        //given - описываем параметры запроса, которые собираемся отправить в MockMvc
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("j.dewar").roles("MANAGER"));

        //замокать поведение сервиса
        //WireMock.get-GET-запрос на url "/catalogue-api/products/1"
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .willReturn(WireMock
                        .notFound()));

        //when - выполняем запрос
        this.mockMvc.perform(requestBuilder)

        //then - манипуляции с результатом
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error",  "Товар не найден")
                );

        //Можно провалидировать, что вызов данного метода у нас был
        //getRequestedFor-GET-запрос "/catalogue-api/products/1"
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching(("/catalogue-api/products/1"))));
    }

    @Test
    void updateProduct_RequestIsValidProductExists_RedirectsToProductPage() throws Exception{
        //given - описываем параметры запроса, которые собираемся отправить в MockMvc
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "Обновленный товар")
                .param("details", "Описание обновленного товара")
                .with(user("j.dewar").roles("MANAGER"))
                .with(csrf());

        //замокать поведение сервиса
        //WireMock.get-GET-запрос на url "/catalogue-api/products/1"
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Товар",
                            "details": "Описание товара"
                        }
                        """)));

        //замокать поведение сервиса
        //WireMock.patch-PATCH-запрос на url "/catalogue-api/products/1"
        WireMock.stubFor(WireMock.patch("/catalogue-api/products/1")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "Обновленный товар",
                            "details": "Описание обновленного товара"
                        }"""))
                .willReturn(WireMock.noContent()));

        //when - выполняем запрос
        this.mockMvc.perform(requestBuilder)

        //then - манипуляции с результатом
        .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/products/1")
                );

        //Можно провалидировать, что вызов данного метода у нас был
        //getRequestedFor-GET-запрос "/catalogue-api/products/1"
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching(("/catalogue-api/products/1"))));

        //Можно провалидировать, что вызов данного метода у нас был
        //patchRequestedFor-PATCH-запрос "/catalogue-api/products/1"
        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "Обновленный товар",
                            "details": "Описание обновленного товара"
                        }""")));
    }

    @Test
    void updateProduct_RequestIsValidProductDoesNotExist_ReturnsError404Page() throws Exception{
        //given - описываем параметры запроса, которые собираемся отправить в MockMvc
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "Обновленный товар")
                .param("details", "Описание обновленного товара")
                .with(user("j.dewar").roles("MANAGER"))
                .with(csrf());

        //замокать поведение сервиса
        //WireMock.get-GET-запрос на url "/catalogue-api/products/1"
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        //when - выполняем запрос
        this.mockMvc.perform(requestBuilder)

                //then - манипуляции с результатом
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Товар не найден")
                );

        //Можно провалидировать, что вызов данного метода у нас был
        //getRequestedFor-GET-запрос "/catalogue-api/products/1"
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching(("/catalogue-api/products/1"))));
    }

    @Test
    void updateProduct_RequestIsInvalidProductExists_ReturnsProductEditPage() throws Exception{
        //given - описываем параметры запроса, которые собираемся отправить в MockMvc
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "  ")
//                .param("details", null)
                .with(user("j.dewar").roles("MANAGER"))
                .with(csrf());

        //замокать поведение сервиса
        //WireMock.get-GET-запрос на url "/catalogue-api/products/1"
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Товар",
                            "details": "Описание товара"
                        }
                        """)));

        //замокать поведение сервиса
        //WireMock.patch-PATCH-запрос на url "/catalogue-api/products/1"
        WireMock.stubFor(WireMock.patch("/catalogue-api/products/1")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "  ",
                            "details": null
                        }"""))
                .willReturn(WireMock.badRequest()//статус
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                 {"errors": ["Ошибка 1", "Ошибка 2"]}
                                """)));

        //when - выполняем запрос
        this.mockMvc.perform(requestBuilder)

                //then - манипуляции с результатом
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("catalogue/products/edit"),
                        model().attribute("product", new Product(1, "Товар", "Описание товара")),
                        model().attribute("payload", new UpdateProductPayload("  ", null)),
                        model().attribute("errors", List.of("Ошибка 1", "Ошибка 2"))
                );

        //Можно провалидировать, что вызов данного метода у нас был
        //getRequestedFor-GET-запрос "/catalogue-api/products/1"
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching(("/catalogue-api/products/1"))));

        //Можно провалидировать, что вызов данного метода у нас был
        //patchRequestedFor-PATCH-запрос "/catalogue-api/products/1"
        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "  ",
                            "details": null
                        }""")));
    }
}