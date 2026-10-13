package com.nathanaeldorsey.stormmarket.order;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order){
        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setTotal(order.getTotal());
        response.setOrderStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();
        response.setItems(items);

        return response;
    }
    private OrderItemResponse toItemResponse(OrderItem item){
        OrderItemResponse response = new OrderItemResponse();

        response.setProductId(item.getId());
        response.setProductName(item.getProductName());
        response.setPrice(item.getPrice());
        response.setQuantity(item.getQuantity());
        BigDecimal subtotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        response.setSubtotal(subtotal);

        return response;
    }
}
