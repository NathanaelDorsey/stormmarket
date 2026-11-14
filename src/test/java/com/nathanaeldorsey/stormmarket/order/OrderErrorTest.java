package com.nathanaeldorsey.stormmarket.order;

import com.nathanaeldorsey.stormmarket.common.exception.DomainError;
import com.nathanaeldorsey.stormmarket.common.exception.Result;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderErrorTest {

    @Test
    void orderErrorExtendsDomainError() {
        assertThat(OrderError.INVALID_STATUS_TRANSITION).isInstanceOf(DomainError.class);
    }

    @Test
    void orderErrorConstantsAreSingletons() {
        assertThat(OrderError.INVALID_STATUS_TRANSITION).isSameAs(OrderError.INVALID_STATUS_TRANSITION);
    }

    @Test
    void orderErrorHasCorrectMessages() {
        assertThat(OrderError.INVALID_STATUS_TRANSITION.getMessage()).isEqualTo("Invalid status transition");
        assertThat(OrderError.PAYMENT_NOT_CAPTURED.getMessage()).isEqualTo("Payment not captured");
        assertThat(OrderError.INSUFFICIENT_STOCK.getMessage()).isEqualTo("Insufficient stock");
        assertThat(OrderError.ORDER_NOT_FOUND.getMessage()).isEqualTo("Order not found");
        assertThat(OrderError.PRODUCT_INACTIVE.getMessage()).isEqualTo("Product is inactive");
        assertThat(OrderError.COUPON_INVALID.getMessage()).isEqualTo("Coupon is invalid");
        assertThat(OrderError.COUPON_EXPIRED.getMessage()).isEqualTo("Coupon has expired");
        assertThat(OrderError.COUPON_USAGE_LIMIT_REACHED.getMessage()).isEqualTo("Coupon usage limit reached");
    }

    @Test
    void orderErrorCanBeUsedInResult() {
        Result<Void, OrderError> result = Result.err(OrderError.INVALID_STATUS_TRANSITION);
        assertThat(result.isErr()).isTrue();
    }

    @Test
    void orderErrorConstantsAreNotNull() {
        assertThat(OrderError.INVALID_STATUS_TRANSITION).isNotNull();
        assertThat(OrderError.PAYMENT_NOT_CAPTURED).isNotNull();
        assertThat(OrderError.INSUFFICIENT_STOCK).isNotNull();
        assertThat(OrderError.ORDER_NOT_FOUND).isNotNull();
        assertThat(OrderError.PRODUCT_INACTIVE).isNotNull();
        assertThat(OrderError.COUPON_INVALID).isNotNull();
        assertThat(OrderError.COUPON_EXPIRED).isNotNull();
        assertThat(OrderError.COUPON_USAGE_LIMIT_REACHED).isNotNull();
    }
}