package com.nathanaeldorsey.stormmarket.order;

import com.nathanaeldorsey.stormmarket.cart.Cart;
import com.nathanaeldorsey.stormmarket.cart.CartItem;
import com.nathanaeldorsey.stormmarket.cart.CartRepository;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
/**
 * Manages the order lifecycle: placing orders from a cart, retrieving order history,
 * viewing order details, admin order listing, and updating order status.
 *
 * Order placement snapshots product details into order items, deducts stock,
 * clears the cart, and runs in a single transaction. Order status transitions
 * follow a fixed flow: PENDING -> CONFIRMED -> SHIPPED -> DELIVERED, with
 * cancellation allowed from PENDING, CONFIRMED, or SHIPPED.
 */
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private static final String ORDER_NOT_FOUND = "Order not found";
    private static final String CART_NOT_FOUND = "Cart not found";

    private User getUserForCheckout(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Cart getCartForCheckout(Long userId) {
        return cartRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException(CART_NOT_FOUND));
    }

    private void validateCart(Cart cart) {
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout with an empty cart");
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
            BigDecimal itemTotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

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
            // Check if the product is active before deducting stock (admin may have deactivated it).
            if (!product.isActive()) {
                throw new IllegalStateException("Cannot purchase inactive product: " + product.getName());
            }
            int rowsUpdated = productRepository.deductStock(product.getId(), item.getQuantity());
            // Zero rows means the product is missing or stock is insufficient.
            if (rowsUpdated == 0) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
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
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForAdmin(OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository
                    .findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(orderMapper::toResponse);
        }
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long userId, Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                // checks requesting userID and existingOrders userId for match
                .filter(existingOrder -> existingOrder.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND));

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailsForAdmin(Long orderId) {
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND));

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND));

        OrderStatus currentStatus = order.getStatus();
        boolean validOrderFlow = switch (currentStatus) {
            case PENDING -> status == OrderStatus.CONFIRMED || status == OrderStatus.CANCELED;
            case CONFIRMED -> status == OrderStatus.SHIPPED || status == OrderStatus.CANCELED;
            case SHIPPED -> status == OrderStatus.DELIVERED;
            case DELIVERED, CANCELED -> false; // No further transitions allowed
        };

        if (!validOrderFlow) {
            throw new IllegalStateException("Cannot change order status from " + currentStatus + " to " + status);
        }
        // If the order is canceled, restore stock for each item.
        if (status == OrderStatus.CANCELED) {
            for (OrderItem item : order.getItems()) {
                productRepository.restoreStock(item.getProduct().getId(), item.getQuantity());
            }
        }
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponse(updatedOrder);
    }
}
