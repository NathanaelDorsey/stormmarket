package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.common.exception.DomainError;
import com.nathanaeldorsey.stormmarket.common.exception.Result;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StockErrorTest {

    @Test
    void stockErrorExtendsDomainError() {
        assertThat(StockError.INSUFFICIENT_STOCK).isInstanceOf(DomainError.class);
    }

    @Test
    void stockErrorConstantsAreSingletons() {
        assertThat(StockError.INSUFFICIENT_STOCK).isSameAs(StockError.INSUFFICIENT_STOCK);
    }

    @Test
    void stockErrorHasCorrectMessages() {
        assertThat(StockError.INSUFFICIENT_STOCK.getMessage()).isEqualTo("Insufficient stock");
        assertThat(StockError.PRODUCT_NOT_FOUND.getMessage()).isEqualTo("Product not found");
        assertThat(StockError.RESERVATION_FAILED.getMessage()).isEqualTo("Reservation failed");
        assertThat(StockError.CAPTURE_FAILED.getMessage()).isEqualTo("Capture failed");
        assertThat(StockError.RELEASE_FAILED.getMessage()).isEqualTo("Release failed");
        assertThat(StockError.PRODUCT_INACTIVE.getMessage()).isEqualTo("Product is inactive");
    }

    @Test
    void stockErrorCanBeUsedInResult() {
        Result<Integer, StockError> result = Result.err(StockError.INSUFFICIENT_STOCK);
        assertThat(result.isErr()).isTrue();
    }

    @Test
    void stockErrorConstantsAreNotNull() {
        assertThat(StockError.INSUFFICIENT_STOCK).isNotNull();
        assertThat(StockError.PRODUCT_NOT_FOUND).isNotNull();
        assertThat(StockError.RESERVATION_FAILED).isNotNull();
        assertThat(StockError.CAPTURE_FAILED).isNotNull();
        assertThat(StockError.RELEASE_FAILED).isNotNull();
        assertThat(StockError.PRODUCT_INACTIVE).isNotNull();
    }
}