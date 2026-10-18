package com.nathanaeldorsey.stormmarket.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponse {
    private Long id;
    private BigDecimal total;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
