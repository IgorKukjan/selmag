package ag.selm.manager.controller;

import ag.selm.manager.client.BadRequestException;
import ag.selm.manager.client.ProductsRestClient;
import ag.selm.manager.controller.payload.NewProductPayload;
import ag.selm.manager.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductsController")
class ProductsControllerTest {

    //1 sposob
//    ProductsRestClient productsRestClient = Mockito.mock(ProductsRestClient.class);

//    ProductsController controller = new ProductsController(this.productsRestClient);

    //2 sposob
    @Mock
    ProductsRestClient productsRestClient;

    @InjectMocks
    ProductsController controller;

    //1-название тестируемого метода
    //2-условие при котором тестируется метод
    //3-ожидаемый результат
    @Test
    @DisplayName("createProduct создаст новый товар и перенаправит на страницу товара")
    void createProduct_RequestIsValid_ReturnsRedirectionToProductPage(){
        //given - 1-определение состояния при котором будет протекать тест
        var payload = new NewProductPayload("Новый товар", "Описание нового товара");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doReturn(new Product(1, "Новый товар", "Описание нового товара"))
                .when(this.productsRestClient)
                .createProduct("Новый товар", "Описание нового товара");
                //title notNull //details any
//                .createProduct(notNull(), any());

        //when - 2-вызов тестируемого метода
        var result = this.controller.createProduct(payload, model, response);

        //then - 3-проверка результата
        assertEquals("redirect:/catalogue/products/1", result);

        //данный метод действительно был вызван
        verify(this.productsRestClient).createProduct("Новый товар", "Описание нового товара");

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    @DisplayName("createProduct вернет страницу с ошибками, если запрос невалиден")
    void createProduct_RequestIsInvalid_ReturnsProductFormWithErrors(){
        //given - 1-определение состояния при котором будет протекать тест
        var payload = new NewProductPayload("   ", null);
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        //не нужно возвращат нужный товар
//        doReturn(new Product(1, "Новый товар", "Описание нового товара"))
        //нужно выбрасывать исключение
        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.productsRestClient)
                .createProduct("   ", null);

        //when - 2-вызов тестируемого метода
        var result = this.controller.createProduct(payload, model, response);

        //then - 3-проверка результата
        assertEquals("catalogue/products/new_product", result);
        //нужно проверить, что в model есть  payload, который мы передовали в данный метод
        //а также список ошибок
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));

        //данный метод действительно был вызван
        verify(this.productsRestClient).createProduct("   ", null);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productsRestClient);
    }
}