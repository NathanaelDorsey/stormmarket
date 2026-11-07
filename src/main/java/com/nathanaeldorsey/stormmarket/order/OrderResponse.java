package com.nathanaeldorsey.stormmarket.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Response DTO for an order: id, user id and username, total, status, creation timestamp,
 * and list of line items.
 */
public class OrderResponse {
    private Long id;
    private Long userId;
    private String username;
    private BigDecimal total;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private String couponCode;
    private BigDecimal discountAmount;
}
