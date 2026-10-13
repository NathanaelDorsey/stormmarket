package com.nathanaeldorsey.stormmarket.order;

import com.nathanaeldorsey.stormmarket.cart.Cart;
import com.nathanaeldorsey.stormmarket.cart.CartItem;
import com.nathanaeldorsey.stormmarket.cart.CartRepository;
import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    private User getUserForCheckout(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Cart getCartForCheckout(Long userId){
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    private void validateCart(Cart cart) {
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot checkout with an empty cart"
            );
        }
    }

    // Snapshot of product for order
    private OrderItem createOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();

        orderItem.setProduct(cartItem.getProduct());
        orderItem.setProductName(cartItem.getProduct().getName());
        orderItem.setPrice(cartItem.getProduct().getPrice());
        orderItem.setQuantity(cartItem.getQuantity());

        return orderItem;
    }

    private BigDecimal calculateOrderTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            BigDecimal itemTotal = item.getProduct()
                    .getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            total = total.add(itemTotal);
        }

        return total;
    }

    private Order createOrder(User user, Cart cart) {
        Order order = new Order();

        order.setUser(user);
        order.setTotal(calculateOrderTotal(cart));
        order.setStatus(OrderStatus.PENDING);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = createOrderItem(cartItem);
            order.addItem(orderItem);
        }

        return order;
    }

    @Transactional // insertions updates and deletions happen in one transaction
    public OrderResponse placeOrder(Long userId) {
        User user = getUserForCheckout(userId);
        Cart cart = getCartForCheckout(userId);

        validateCart(cart);

        Order order = createOrder(user, cart);

        // Reduce stock for each purchased product.
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            int rowsUpdated = productRepository.deductStock(
                    product.getId(),
                    item.getQuantity()
            );
            // Zero rows means the product is missing or stock is insufficient.
            if (rowsUpdated == 0) {
                throw new IllegalStateException(
                        "Insufficient stock for product: " + product.getName()
                );
            }
        }
        // Save the order and its items.
        Order savedOrder = orderRepository.save(order);
        // Remove the purchased items from the cart.
        cart.getItems().clear();

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long userId) {
        User user = getUserForCheckout(userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                // checks requesting userID and existingOrders userId for match
                .filter(existingOrder ->
                        existingOrder.getUser().getId().equals(userId))
                .orElseThrow(() ->
                        new NoSuchElementException("Order not found"));

        return orderMapper.toResponse(order);
    }
}
