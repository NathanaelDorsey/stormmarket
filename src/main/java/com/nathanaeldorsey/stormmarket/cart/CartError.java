package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.common.exception.DomainError;

/**
 * Expected failures for cart operations.
 * All instances are static singletons for zero-allocation hot paths.
 */
public class CartError extends DomainError {

    public static final CartError INACTIVE_PRODUCT = new CartError("Product is inactive");
    public static final CartError INSUFFICIENT_STOCK = new CartError("Insufficient stock");
    public static final CartError CART_FULL = new CartError("Cart is full");
    public static final CartError INVALID_QUANTITY = new CartError("Invalid quantity");
    public static final CartError PRODUCT_NOT_IN_CART = new CartError("Product not in cart");
    public static final CartError CART_NOT_FOUND = new CartError("Cart not found");
    public static final CartError PRODUCT_NOT_FOUND = new CartError("Product not found");
    public static final CartError COUPON_INVALID = new CartError("Coupon is invalid");
    public static final CartError COUPON_EXPIRED = new CartError("Coupon has expired");
    public static final CartError COUPON_USAGE_LIMIT_REACHED = new CartError("Coupon usage limit reached");
    public static final CartError COUPON_INACTIVE = new CartError("Coupon is inactive");

    private CartError(String message) {
        super(message);
    }
}