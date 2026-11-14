package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.common.exception.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductRepositoryResultTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void reserveStockReturnsResultOkWhenSuccessful() {
        // Given a product with sufficient stock
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setReservedStock(0);
        product.setActive(true);
        productRepository.save(product);

        // When reserving stock
        Result<Integer, StockError> result = productRepository.reserveStock(product.getId(), 10);

        // Then Result is Ok with rows updated
        assertThat(result.isOk()).isTrue();
        assertThat(result.getValue()).isEqualTo(1);
    }

    @Test
    void reserveStockReturnsResultErrWhenInsufficientStock() {
        // Given a product with insufficient stock
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(5);
        product.setReservedStock(0);
        product.setActive(true);
        productRepository.save(product);

        // When reserving more than available
        Result<Integer, StockError> result = productRepository.reserveStock(product.getId(), 10);

        // Then Result is Err with INSUFFICIENT_STOCK
        assertThat(result.isErr()).isTrue();
    }

    @Test
    void deductStockReturnsResultOkWhenSuccessful() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setReservedStock(0);
        product.setActive(true);
        productRepository.save(product);

        Result<Integer, StockError> result = productRepository.deductStock(product.getId(), 10);

        assertThat(result.isOk()).isTrue();
        assertThat(result.getValue()).isEqualTo(1);
    }

    @Test
    void deductStockReturnsResultErrWhenInsufficientStock() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(5);
        product.setReservedStock(0);
        product.setActive(true);
        productRepository.save(product);

        Result<Integer, StockError> result = productRepository.deductStock(product.getId(), 10);

        assertThat(result.isErr()).isTrue();
    }

    @Test
    void captureStockReturnsResultOkWhenSuccessful() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setReservedStock(10);
        product.setActive(true);
        productRepository.save(product);

        Result<Integer, StockError> result = productRepository.captureStock(product.getId(), 5);

        assertThat(result.isOk()).isTrue();
        assertThat(result.getValue()).isEqualTo(1);
    }

    @Test
    void captureStockReturnsResultErrWhenInsufficientStock() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(5);
        product.setReservedStock(0);
        product.setActive(true);
        productRepository.save(product);

        Result<Integer, StockError> result = productRepository.captureStock(product.getId(), 10);

        assertThat(result.isErr()).isTrue();
    }

    @Test
    void restoreStockReturnsResultOk() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setActive(true);
        productRepository.save(product);

        Result<Void, StockError> result = productRepository.restoreStock(product.getId(), 10);

        assertThat(result.isOk()).isTrue();
    }

    @Test
    void releaseReservedStockReturnsResultOk() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setReservedStock(10);
        product.setActive(true);
        productRepository.save(product);

        Result<Void, StockError> result = productRepository.releaseReservedStock(product.getId(), 5);

        assertThat(result.isOk()).isTrue();
    }
}