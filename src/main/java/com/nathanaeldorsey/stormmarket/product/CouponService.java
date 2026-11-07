package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
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
    private static final String COUPON_NOT_FOUND = "Coupon not found";

    /**
     * Validates a coupon code and returns the coupon if valid.
     *
     * @param code the coupon code
     * @return the coupon if valid
     * @throws ResourceNotFoundException if the coupon does not exist
     * @throws IllegalStateException if the coupon is expired or usage limit reached
     */
    @Transactional(readOnly = true)
    public Coupon validate(String code) {
        Coupon coupon = couponRepository.findByCode(code);
        if (coupon == null) {
            throw new ResourceNotFoundException(COUPON_NOT_FOUND);
        }

        if (!coupon.isValid()) {
            throw new IllegalStateException("Coupon is expired or usage limit reached");
        }

        return coupon;
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
