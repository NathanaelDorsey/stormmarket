package com.nathanaeldorsey.stormmarket.product;

/**
 * Expected failures when validating a coupon.
 * Extends RuntimeException to satisfy Result<E extends Throwable> bound.
 */
public class CouponValidationError extends RuntimeException {
    public static final CouponValidationError NOT_FOUND = new CouponValidationError("Coupon not found");
    public static final CouponValidationError EXPIRED = new CouponValidationError("Coupon has expired");
    public static final CouponValidationError USAGE_LIMIT_REACHED = new CouponValidationError("Coupon usage limit reached");
    public static final CouponValidationError INACTIVE = new CouponValidationError("Coupon is not active");

    private CouponValidationError(String message) {
        super(message);
    }
}