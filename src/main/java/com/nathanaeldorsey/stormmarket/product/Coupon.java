package com.nathanaeldorsey.stormmarket.product;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Represents a discount coupon that can be applied to carts or orders.
 *
 * Supports percentage-based or fixed-amount discounts with optional minimum order
 * value, expiration date, and usage limit.
 */
@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private java.math.BigDecimal discountValue;

    @Column(name = "min_order_value", nullable = false)
    private java.math.BigDecimal minOrderValue;

    @Column(name = "expires_at")
    private java.time.LocalDateTime expiresAt;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @PrePersist
    protected void onCreate() {
        if (usedCount == null) {
            usedCount = 0;
        }
    }

    public boolean isExpired() {
        return expiresAt != null && java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")).isAfter(expiresAt);
    }

    public boolean isUsageLimitReached() {
        return usageLimit != null && usedCount >= usageLimit;
    }

    public boolean isValid() {
        return !isExpired() && !isUsageLimitReached();
    }

    public java.math.BigDecimal calculateDiscount(java.math.BigDecimal orderTotal) {
        if (discountType == DiscountType.PERCENTAGE) {
            return orderTotal.multiply(discountValue).divide(java.math.BigDecimal.valueOf(100));
        } else {
            return discountValue;
        }
    }
}
