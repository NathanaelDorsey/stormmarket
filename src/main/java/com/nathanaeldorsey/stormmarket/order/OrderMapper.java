package com.nathanaeldorsey.stormmarket.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
/**
 * Maps order entities to response DTOs, including user info and mapped line items.
 */
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setUsername(order.getUser().getUsername());
        response.setTotal(order.getTotal());
        response.setOrderStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        List<OrderItemResponse> items =
                order.getItems().stream().map(this::toItemResponse).toList();
        response.setItems(items);

        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();

        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProductName());
        response.setPrice(item.getPrice());
        response.setQuantity(item.getQuantity());
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        response.setSubtotal(subtotal);

        return response;
    }
}
