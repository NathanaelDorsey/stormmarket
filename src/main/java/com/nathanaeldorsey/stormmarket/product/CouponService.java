package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.common.exception.Result;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages coupon validation and discount calculation.
 *
 * Coupons can be applied to carts to reduce the total amount.
 * Supports percentage-based and fixed-amount discounts.
 */
@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    /**
     * Validates a coupon code and returns the coupon if valid.
     * Returns a Result to make expected failures explicit at compile time.
     *
     * @param code the coupon code
     * @return Result containing the coupon on success, or CouponValidationError on failure
     */
    @Transactional(readOnly = true)
    public Result<Coupon, CouponValidationError> validate(String code) {
        Coupon coupon = couponRepository.findByCode(code);
        if (coupon == null) {
            return Result.err(CouponValidationError.NOT_FOUND);
        }

        if (coupon.isExpired()) {
            return Result.err(CouponValidationError.EXPIRED);
        }
        if (coupon.isUsageLimitReached()) {
            return Result.err(CouponValidationError.USAGE_LIMIT_REACHED);
        }

        return Result.ok(coupon);
    }

    /**
     * Calculates the discount amount for a given order total using the coupon.
     *
     * @param coupon     the coupon to apply
     * @param orderTotal the order total before discount
     * @return the discount amount
     */
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal) {
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            return orderTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else {
            return coupon.getDiscountValue();
        }
    }

    /**
     * Increments the usage count of a coupon.
     *
     * @param coupon the coupon to increment
     */
    @Transactional
    public void incrementUsage(Coupon coupon) {
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }
}