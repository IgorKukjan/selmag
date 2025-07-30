package ag.selm.manager.controller;

import ag.selm.manager.client.BadRequestException;
import ag.selm.manager.client.ProductsRestClient;
import ag.selm.manager.controller.payload.UpdateProductPayload;
import ag.selm.manager.entity.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import java.util.List;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductController")
class ProductControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductController controller;

    @Test
    void product_ProductExists_ReturnsProduct(){
        //given
        var product = new Product(1, "Товар №1", "Описание товара №1");

        doReturn(Optional.of(product))
                .when(this.productsRestClient).findProduct(1);
        //when
        var result = this.controller.product(1);

        //then
        assertEquals(product, result);

        //данный метод действительно был вызван
        verify(this.productsRestClient).findProduct(1);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void product_ProductNotExists_ThrowsNoSuchElementException(){
        //given


        //when
        var exception = assertThrows(NoSuchElementException.class, () -> this.controller.product(1));

        //then
        assertEquals("catalogue.errors.product.not_found", exception.getMessage());

        //данный метод действительно был вызван
        verify(this.productsRestClient).findProduct(1);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void getProduct_ReturnsProductPage() {
        //given

        //when
        var result = this.controller.getProduct();
        //then
        assertEquals("catalogue/products/product", result);
    }

    @Test
    void getProductEditPage_ReturnsProductEditPage() {
        //given

        //when
        var result = this.controller.getProductEditPage();
        //then
        assertEquals("catalogue/products/edit", result);
    }

    @Test
    void updateProduct_RequestIsValid_RedirectsToProductPage(){
        //given
        var product = new Product(1, "Товар №1", "Описание товара №1");
        var payload = new UpdateProductPayload("Обновленное название", "Обновленное описание");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        //when
        var result = this.controller.updateProduct(product, payload, model, response);

        //then
        assertEquals("redirect:/catalogue/products/1", result);

        //данный метод действительно был вызван
        verify(this.productsRestClient).updateProduct(1, "Обновленное название", "Обновленное описание");

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsProductEditPage(){
        //given
        var product = new Product(1, "Товар №1", "Описание товара №1");
        var payload = new UpdateProductPayload("  ", null);
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.productsRestClient).updateProduct(1, "  ", null);

        //when
        var result = this.controller.updateProduct(product, payload, model, response);

        //then
        assertEquals("catalogue/products/edit", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        //данный метод действительно был вызван
        verify(this.productsRestClient).updateProduct(1, "  ", null);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void deleteProduct_RedirectsToProductsListPage(){
        //given
        var product = new Product(1, "Товар №1", "Описание товара №1");

        //when
        var result = this.controller.deleteProduct(product);

        //then
        assertEquals("redirect:/catalogue/products/list", result);

        //данный метод действительно был вызван
        verify(this.productsRestClient).deleteProduct(1);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void handleNoSuchElementException_Returns404ErrorPage(){
        //given
        var  exception = new NoSuchElementException("error");
        var  model = new ConcurrentModel();
        var  response = new MockHttpServletResponse();
        var  locale = Locale.of("ru");

        doReturn("Ошибка")
                .when(this.messageSource)
                .getMessage("error", new Object[0], "error", Locale.of("ru") );

        //when
        var result = this.controller.handleNoSuchElementException(exception, model, response, locale);

        //then
        assertEquals("errors/404", result);

        //данный метод действительно был вызван
        verify(this.messageSource).getMessage("error", new Object[0], "error", Locale.of("ru"));

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.messageSource);
        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoInteractions(this.productsRestClient);
    }
}