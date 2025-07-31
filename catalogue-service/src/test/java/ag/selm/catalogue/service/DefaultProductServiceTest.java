package ag.selm.catalogue.service;

import ag.selm.catalogue.entity.Product;
import ag.selm.catalogue.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultProductServiceTest {

    ProductRepository productRepository = Mockito.mock(ProductRepository.class);

    DefaultProductService defaultProductService = new DefaultProductService(productRepository);

    @Test
    void findAllProducts_FilterIsNotSet_ReturnsProductsList(){
        //given
        String filter = null;
        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "товар №%d".formatted(i)  , "Описание товара №%d".formatted(i)))
                .toList();

        doReturn(products)
                .when(this.productRepository).findAll();

        //when
        var result = this.defaultProductService.findAllProducts(filter);

        //then
        assertEquals(products, result);

        //данный метод действительно был вызван
        verify(this.productRepository).findAll();

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void findAllProducts_FilterIsSet_ReturnsFilteredProductsList(){
        //given
        String filter = "товар";
        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "товар №%d".formatted(i)  , "Описание товара №%d".formatted(i)))
                .toList();

        doReturn(products)
            .when(this.productRepository).findAllByTitleLikeIgnoreCase("%товар%");

        //when
        var result = this.defaultProductService.findAllProducts(filter);

        //then
        assertEquals(products, result);

        //данный метод действительно был вызван
        verify(this.productRepository).findAllByTitleLikeIgnoreCase("%товар%");

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void findProduct_ProductExists_ReturnsProduct(){
        //given
        var product = new Product(1, "товар №1", "Описание товара №1");

        doReturn(Optional.of(product))
                .when(this.productRepository).findById(1);

        //when
        var result = this.defaultProductService.findProduct(1);

        //then
        assertEquals(product, result.get());

        //данный метод действительно был вызван
        verify(this.productRepository).findById(1);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void findProduct_ProductDoesNotExist_ReturnsEmptyOptional(){
        //given

        //when
        var result = this.defaultProductService.findProduct(1);

        //then
        assertTrue(result.isEmpty());
        assertNotNull(result);

        //данный метод действительно был вызван
        verify(this.productRepository).findById(1);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void createProduct_ReturnsCreatedProduct(){
        //given
        var title = "Новый товар";
        var details = "Описание нового товара";

        doReturn(new Product(1, "Новый товар", "Описание нового товара"))
                .when(this.productRepository).save(new Product(null, "Новый товар", "Описание нового товара"));

        //when
        var result = this.defaultProductService.createProduct(title, details);

        //then
        assertEquals(new Product(1, "Новый товар", "Описание нового товара"), result);

        //данный метод действительно был вызван
        verify(this.productRepository).save(new Product(null, "Новый товар", "Описание нового товара"));

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void updateProduct_ProductExists_UpdatesProduct(){
        //given
        var id = 1;
        var title = "Обновленный товар";
        var details = "Описание обновленного товара";

        var product = new Product(1, "Новый товар", "Описание нового товара");

        doReturn(Optional.of(product))
                .when(this.productRepository).findById(1);

        //when
        this.defaultProductService.updateProduct(id, title, details);

        //then
        //данный метод действительно был вызван
        verify(this.productRepository).findById(id);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void updateProduct_ProductDoesNotExist_ThrowsNoSuchElementException(){
        //given
        var id = 1;
        var title = "Обновленный товар";
        var details = "Описание обновленного товара";

        //when
        assertThrows(NoSuchElementException.class,
                () -> this.defaultProductService.updateProduct(id, title, details));

        //then
        //данный метод действительно был вызван
        verify(this.productRepository).findById(id);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void deleteProduct_DeletesProduct(){
        //given
        var id = 1;

        //when
        this.defaultProductService.deleteProduct(id);

        //then
        //данный метод действительно был вызван
        verify(this.productRepository).deleteById(id);

        //к данному Mock объекту больше не было никаких обращений в рамках тестируемого метода
        verifyNoMoreInteractions(this.productRepository);
    }
}