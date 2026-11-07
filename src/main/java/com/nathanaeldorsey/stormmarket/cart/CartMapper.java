package com.nathanaeldorsey.stormmarket.cart;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
/**
 * Maps cart domain entities to their response DTOs, computing item subtotals and cart total.
 */
public class CartMapper {
    public CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());

        List<CartItemResponse> items =
                cart.getItems().stream().map(this::toItemResponse).toList();
        response.setItems(items);

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // add all subtotals starting from 0.00

        // Apply coupon discount if a coupon code is set on the cart
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (cart.getCouponCode() != null && !cart.getCouponCode().isBlank()) {
            // Discount is applied at checkout time; here we just report the coupon code
            // and a zero discount in the cart view (final discount applied in OrderService)
            response.setCouponCode(cart.getCouponCode());
        }
        response.setDiscountAmount(discountAmount);
        response.setTotal(subtotal.subtract(discountAmount));

        return response;
    }

    private CartItemResponse toItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();

        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setPrice(item.getProduct().getPrice());
        response.setQuantity(item.getQuantity());
        BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        response.setSubtotal(subtotal);
        response.setActive(item.getProduct().isActive());
        response.setInStock(item.getProduct().getStockQuantity() > 0);
        return response;
    }
}
