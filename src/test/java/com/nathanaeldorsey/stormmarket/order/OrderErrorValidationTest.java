package com.nathanaeldorsey.stormmarket.order;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Validates that all {@link OrderError} constants exist and carry the
 * expected error messages. Guards against accidental removal or
 * message drift in domain error constants.
 */
class OrderErrorValidationTest {

    private static final Map<String, String> EXPECTED_MESSAGES = Map.of(
            "INVALID_STATUS_TRANSITION", "Invalid status transition",
            "PAYMENT_NOT_CAPTURED", "Payment not captured",
            "INSUFFICIENT_STOCK", "Insufficient stock",
            "ORDER_NOT_FOUND", "Order not found",
            "PRODUCT_INACTIVE", "Product is inactive",
            "COUPON_INVALID", "Coupon is invalid",
            "COUPON_EXPIRED", "Coupon has expired",
            "COUPON_USAGE_LIMIT_REACHED", "Coupon usage limit reached"
    );

    @Test
    void allOrderErrorConstantsExist() {
        for (String constantName : EXPECTED_MESSAGES.keySet()) {
            try {
                Field field = OrderError.class.getDeclaredField(constantName);
                assertThat(field).isNotNull();
            } catch (NoSuchFieldException e) {
                fail("Missing OrderError constant: " + constantName);
            }
        }
    }

    @Test
    void allOrderErrorConstantsHaveCorrectMessages() {
        for (Map.Entry<String, String> entry : EXPECTED_MESSAGES.entrySet()) {
            OrderError error = constantByName(entry.getKey());
            assertThat(error.getMessage()).isEqualTo(entry.getValue());
        }
    }

    @Test
    void allOrderErrorConstantsAreNotNull() {
        for (String constantName : EXPECTED_MESSAGES.keySet()) {
            assertThat(constantByName(constantName)).isNotNull();
        }
    }

    @SuppressWarnings("unchecked")
    private OrderError constantByName(String constantName) {
        try {
            Field field = OrderError.class.getDeclaredField(constantName);
            field.setAccessible(true);
            return (OrderError) field.get(null);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to resolve OrderError constant: " + constantName, e);
        }
    }
}
