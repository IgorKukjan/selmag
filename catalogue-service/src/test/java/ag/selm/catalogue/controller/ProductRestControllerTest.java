package ag.selm.catalogue.controller;

import ag.selm.catalogue.controller.payload.UpdateProductPayload;
import ag.selm.catalogue.entity.Product;
import ag.selm.catalogue.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductRestControllerTest {

    ProductService productService = Mockito.mock(ProductService.class);

    MessageSource messageSource = Mockito.mock(MessageSource.class);

    ProductRestController productRestController = new ProductRestController(productService, messageSource);

    @Test
    void getProduct_ProductExists_ReturnsProduct(){
        //given
        var product = new Product(1, "Название товара", "Описание товара");

        doReturn(Optional.of(product))
                .when(this.productService).findProduct(1);

        //when
        var result = this.productRestController.getProduct(1);

        //then
        assertEquals(product, result);

        //данный метод действительно был вызван
        verify(this.productService).findProduct(1);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsNoSuchElementException(){
        //given

        //when
        var exception = assertThrows(NoSuchElementException.class, ()->this.productRestController.getProduct(1));

        //then
        assertEquals("catalogue.errors.product.not_found", exception.getMessage());

        //данный метод действительно был вызван
        verify(this.productService).findProduct(1);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void findProduct_ReturnsProduct(){
        //given
        var product = new Product(1, "Название товара", "Описание товара");

        //when
        var result = this.productRestController.findProduct(product);

        //then
        assertEquals(product, result);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void updateProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        //given
        var product = new Product(1, "Название товара", "Описание товара");
        var payload = new UpdateProductPayload("Обновленный товар","Описание обновленного товара");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        //when
        var result = this.productRestController.updateProduct(product.getId(), payload, bindingResult);

        //then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        assertNotNull(result);

        //данный метод действительно был вызван
        verify(this.productService).updateProduct(1, "Обновленный товар","Описание обновленного товара");

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() throws BindException {
        //given
        var product = new Product(1, "Название товара", "Описание товара");
        var payload = new UpdateProductPayload("Обновленный товар","Описание обновленного товара");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));

        //when
        var exception = assertThrows(BindException.class, () -> this.productRestController.updateProduct(product.getId(), payload, bindingResult));

        //then
        assertEquals(List.of(new FieldError("payload", "title", "error") ), exception.getAllErrors());

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void updateProduct_RequestIsInvalidAndBindingResultIsBindException_ReturnsBadRequest(){
        //given
        var product = new Product(1, "Название товара", "Описание товара");
        var payload = new UpdateProductPayload("Обновленный товар","Описание обновленного товара");
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));

        //when
        var exception = assertThrows(BindException.class, () -> this.productRestController.updateProduct(product.getId(), payload, bindingResult));

        //then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }


    @Test
    void deleteProduct_ReturnsNoContent(){
        //given
        var product = new Product(1, "Название товара", "Описание товара");

        //when
        var result = this.productRestController.deleteProduct(product.getId());

        //then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        assertNotNull(result);

        //данный метод действительно был вызван
        verify(this.productService).deleteProduct(1);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void handleNoSuchElementException_ReturnsNotFound(){
        //given
        var exception = new NoSuchElementException("error_code");
        var locale = Locale.of("ru");

        doReturn("error_details")
                .when(this.messageSource).getMessage("error_code", new Object[0], "error_code", Locale.of("ru"));

        //when
        var result = this.productRestController.handleNoSuchElementException(exception, locale);

        //then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

        assertNotNull(result);
        assertInstanceOf(ProblemDetail.class, result.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getBody().getStatus());
        assertEquals("error_details", result.getBody().getDetail());

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }
}
