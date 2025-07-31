package ag.selm.catalogue.controller;

import ag.selm.catalogue.controller.payload.NewProductPayload;
import ag.selm.catalogue.entity.Product;
import ag.selm.catalogue.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductsRestControllerTest {

    ProductService productService = Mockito.mock(ProductService.class);

    ProductsRestController productsRestController = new ProductsRestController(this.productService);

    @Test
    void findProducts_ReturnsProductsList(){
        //given
        var filter = "товар";
        var principal = Mockito.mock(JwtAuthenticationToken.class);

        doReturn(List.of(new Product(1, "товар №1", "Описание товара №1"),
                new Product(2, "товар №2", "Описание товара №2")))
                .when(this.productService).findAllProducts("товар");

        //when
        var result = this.productsRestController.findProducts(filter, principal);

        //then
        assertEquals(List.of(new Product(1, "товар №1", "Описание товара №1"),
                new Product(2, "товар №2", "Описание товара №2")), result);

        //данный метод действительно был вызван
        verify(this.productService).findAllProducts("товар");

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        //given
        var payload = new NewProductPayload("Новый товар", "Описание нового товара");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl("http://localhost");

        doReturn(new Product(1, "Новый товар", "Описание нового товара"))
                .when(this.productService).createProduct("Новый товар", "Описание нового товара");

        //when
        var result = this.productsRestController.createProduct(payload,bindingResult, uriComponentsBuilder);

        //then
        assertEquals(new Product(1, "Новый товар", "Описание нового товара"), result.getBody());

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(URI.create("http://localhost/catalogue-api/products/1"), result.getHeaders().getLocation());

        //данный метод действительно был вызван
        verify(this.productService).createProduct("Новый товар", "Описание нового товара");

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsBadRequest(){
        //given
        var payload = new NewProductPayload("  ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl("http://localhost");

        //when
        var exception  = assertThrows(BindException.class,
                () -> this.productsRestController.createProduct(payload,bindingResult, uriComponentsBuilder));

        //then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalidAndBindResultIsBindException_ReturnsBadRequest(){
        //given
        var payload = new NewProductPayload("  ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl("http://localhost");

        //when
        var exception  = assertThrows(BindException.class,
                () -> this.productsRestController.createProduct(payload,bindingResult, uriComponentsBuilder));

        //then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productService);
    }
}