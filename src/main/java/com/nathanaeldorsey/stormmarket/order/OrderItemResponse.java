package com.nathanaeldorsey.stormmarket.order;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Response DTO for an order line item: product id, name, price, quantity, and subtotal.
 */
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
