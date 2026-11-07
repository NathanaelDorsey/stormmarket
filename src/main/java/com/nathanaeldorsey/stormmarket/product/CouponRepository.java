package com.nathanaeldorsey.stormmarket.product;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for coupon entities.
 */
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * Finds a coupon by its code.
     *
     * @param code the coupon code
     * @return the coupon, or empty if none exists
     */
    Coupon findByCode(String code);
}
