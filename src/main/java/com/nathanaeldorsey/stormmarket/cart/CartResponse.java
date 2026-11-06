package com.nathanaeldorsey.stormmarket.cart;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO for a shopping cart: cart id, list of line items, and total amount.
 */
@Getter
@Setter
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
    private BigDecimal total;
}
