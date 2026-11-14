package com.nathanaeldorsey.stormmarket.order;

import com.nathanaeldorsey.stormmarket.common.exception.DomainError;

/**
 * Expected failures for order operations.
 * All instances are static singletons for zero-allocation hot paths.
 */
public class OrderError extends DomainError {

    public static final OrderError INVALID_STATUS_TRANSITION = new OrderError("Invalid status transition");
    public static final OrderError PAYMENT_NOT_CAPTURED = new OrderError("Payment not captured");
    public static final OrderError INSUFFICIENT_STOCK = new OrderError("Insufficient stock");
    public static final OrderError ORDER_NOT_FOUND = new OrderError("Order not found");
    public static final OrderError PRODUCT_INACTIVE = new OrderError("Product is inactive");
    public static final OrderError COUPON_INVALID = new OrderError("Coupon is invalid");
    public static final OrderError COUPON_EXPIRED = new OrderError("Coupon has expired");
    public static final OrderError COUPON_USAGE_LIMIT_REACHED = new OrderError("Coupon usage limit reached");

    private OrderError(String message) {
        super(message);
    }
}