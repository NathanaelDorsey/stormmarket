package com.nathanaeldorsey.stormmarket.cart;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {
    public CartResponse toResponse(Cart cart){
        CartResponse response = new CartResponse();
        response.setId(cart.getId());

        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        response.setItems(items);

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // add all subtotals starting from 0.00
        response.setTotal(total);

        return response;
    }

    private CartItemResponse toItemResponse(CartItem item){
        CartItemResponse response = new CartItemResponse();

        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setPrice(item.getProduct().getPrice());
        response.setQuantity(item.getQuantity());
        BigDecimal subtotal = item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
        response.setSubtotal(subtotal);
        return response;
    }
}
