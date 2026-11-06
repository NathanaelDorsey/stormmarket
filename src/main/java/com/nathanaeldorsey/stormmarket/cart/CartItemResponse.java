package com.nathanaeldorsey.stormmarket.cart;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO for a cart line item: product id, name, price, quantity, subtotal,
 * and whether the product is active and in stock.
 */
@Getter
@Setter
public class CartItemResponse {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private boolean isActive;
    private boolean inStock;
}
