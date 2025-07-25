package ag.selm.catalogue.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional//бд в исходное состояние
//каждый тестовый метод будет выполняться в новой транзакции
@SpringBootTest
@AutoConfigureMockMvc
class ProductRestControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/products.sql")
    void findProduct_ProductExists_ReturnsProduct() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        //when
        this.mockMvc.perform(requestBuilder)

        //then
        .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"id": 1, "title": "Товар №1", "details":  "Описание товара №1"}
                        """));
    }

    @Test
    @Sql("/sql/products.sql")
    void findProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products/5")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)

        // then
            .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql("/sql/products.sql")
    void updateProduct_RequestIsValid_ReturnsNoContent() throws Exception {
        //given
        var requstBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                  {"title": "Одновленный товар", "details":  "Обновленное описание обновленного товара"}
                """)
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));
        //when
        this.mockMvc.perform(requstBuilder)

        //then
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void updateProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        //given
        var requstBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                  {"title": "Одновленный товар", "details":  "Обновленное описание обновленного товара"}
                """)
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));
        //when
        this.mockMvc.perform(requstBuilder)

                //then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void deleteProduct_RequestIsValid_ReturnsNoContent() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));
        //when
        this.mockMvc.perform(requestBuilder)

        //then
            .andDo(print())
                .andExpectAll(
                       status().isNoContent()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void deleteProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/products/5")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));
        //when
        this.mockMvc.perform(requestBuilder)

                //then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound()
                );
    }
}