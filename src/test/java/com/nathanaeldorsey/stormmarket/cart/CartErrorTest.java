package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.common.exception.DomainError;
import com.nathanaeldorsey.stormmarket.common.exception.Result;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CartErrorTest {

    @Test
    void cartErrorExtendsDomainError() {
        assertThat(CartError.INACTIVE_PRODUCT).isInstanceOf(DomainError.class);
    }

    @Test
    void cartErrorConstantsAreSingletons() {
        assertThat(CartError.INACTIVE_PRODUCT).isSameAs(CartError.INACTIVE_PRODUCT);
    }

    @Test
    void cartErrorHasCorrectMessages() {
        assertThat(CartError.INACTIVE_PRODUCT.getMessage()).isEqualTo("Product is inactive");
        assertThat(CartError.INSUFFICIENT_STOCK.getMessage()).isEqualTo("Insufficient stock");
        assertThat(CartError.CART_FULL.getMessage()).isEqualTo("Cart is full");
        assertThat(CartError.INVALID_QUANTITY.getMessage()).isEqualTo("Invalid quantity");
        assertThat(CartError.PRODUCT_NOT_IN_CART.getMessage()).isEqualTo("Product not in cart");
        assertThat(CartError.CART_NOT_FOUND.getMessage()).isEqualTo("Cart not found");
        assertThat(CartError.PRODUCT_NOT_FOUND.getMessage()).isEqualTo("Product not found");
        assertThat(CartError.COUPON_INVALID.getMessage()).isEqualTo("Coupon is invalid");
        assertThat(CartError.COUPON_EXPIRED.getMessage()).isEqualTo("Coupon has expired");
        assertThat(CartError.COUPON_USAGE_LIMIT_REACHED.getMessage()).isEqualTo("Coupon usage limit reached");
        assertThat(CartError.COUPON_INACTIVE.getMessage()).isEqualTo("Coupon is inactive");
    }

    @Test
    void cartErrorCanBeUsedInResult() {
        Result<Void, CartError> result = Result.err(CartError.INACTIVE_PRODUCT);
        assertThat(result.isErr()).isTrue();
    }

    @Test
    void cartErrorConstantsAreNotNull() {
        assertThat(CartError.INACTIVE_PRODUCT).isNotNull();
        assertThat(CartError.INSUFFICIENT_STOCK).isNotNull();
        assertThat(CartError.CART_FULL).isNotNull();
        assertThat(CartError.INVALID_QUANTITY).isNotNull();
        assertThat(CartError.PRODUCT_NOT_IN_CART).isNotNull();
        assertThat(CartError.CART_NOT_FOUND).isNotNull();
        assertThat(CartError.PRODUCT_NOT_FOUND).isNotNull();
        assertThat(CartError.COUPON_INVALID).isNotNull();
        assertThat(CartError.COUPON_EXPIRED).isNotNull();
        assertThat(CartError.COUPON_USAGE_LIMIT_REACHED).isNotNull();
        assertThat(CartError.COUPON_INACTIVE).isNotNull();
    }
}