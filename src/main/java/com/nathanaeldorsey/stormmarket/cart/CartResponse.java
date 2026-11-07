package com.nathanaeldorsey.stormmarket.cart;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO for a shopping cart: cart id, list of line items, total amount,
 * applied coupon code, and discount amount.
 */
@Getter
@Setter
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
    private BigDecimal total;
    private String couponCode;
    private BigDecimal discountAmount;
}
