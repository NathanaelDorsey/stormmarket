package com.nathanaeldorsey.stormmarket.order;

import com.nathanaeldorsey.stormmarket.cart.Cart;
import com.nathanaeldorsey.stormmarket.cart.CartItem;
import com.nathanaeldorsey.stormmarket.cart.CartRepository;
import com.nathanaeldorsey.stormmarket.common.exception.Ok;
import com.nathanaeldorsey.stormmarket.common.exception.Result;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.payment.PaymentService;
import com.nathanaeldorsey.stormmarket.payment.PaymentStatus;
import com.nathanaeldorsey.stormmarket.product.Coupon;
import com.nathanaeldorsey.stormmarket.product.CouponService;
import com.nathanaeldorsey.stormmarket.product.CouponValidationError;
import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
/**
 * Manages the order lifecycle: placing orders from a cart, retrieving order history,
 * viewing order details, admin order listing, and updating order status.
 *
 * Order placement snapshots product details into order items, creates a payment
 * intent, and clears the cart. Stock is deducted when payment is captured,
 * not at order placement time. Order status transitions follow a fixed flow:
 * PENDING -> CONFIRMED -> SHIPPED -> DELIVERED, with cancellation allowed from
 * PENDING or CONFIRMED.
 */
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;
    private final CouponService couponService;
    private final OrderEventRepository orderEventRepository;
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

        // Snapshot coupon code and discount amount from cart onto the order
        if (cart.getCouponCode() != null && !cart.getCouponCode().isBlank()) {
            Result<Coupon, CouponValidationError> result = couponService.validate(cart.getCouponCode());
            if (result.isOk()) {
                Coupon coupon = ((Ok<Coupon, CouponValidationError>) result).value();
                BigDecimal subtotal = calculateOrderTotal(cart);
                BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
                order.setCouponCode(cart.getCouponCode());
                order.setDiscountAmount(discount);
                order.setTotal(subtotal.subtract(discount));
            }
        }

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = createOrderItem(cartItem);
            order.addItem(orderItem);
        }
        return order;
    }

    private Order createGuestOrder(GuestCheckoutRequest guestRequest) {
        Order order = new Order();
        order.setUser(null); // Guest order - no user
        order.setTotal(BigDecimal.ZERO); // Will be calculated from items
        order.setStatus(OrderStatus.PENDING);

        // Create shipping address from guest request
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFirstName(guestRequest.getFirstName());
        shippingAddress.setLastName(guestRequest.getLastName());
        shippingAddress.setEmail(guestRequest.getEmail());
        shippingAddress.setPhone(guestRequest.getPhone());
        shippingAddress.setAddressLine1(guestRequest.getAddressLine1());
        shippingAddress.setAddressLine2(guestRequest.getAddressLine2());
        shippingAddress.setCity(guestRequest.getCity());
        shippingAddress.setState(guestRequest.getState());
        shippingAddress.setPostalCode(guestRequest.getPostalCode());
        shippingAddress.setCountry(guestRequest.getCountry());
        order.setShippingAddress(shippingAddress);

        return order;
    }

    /**
     * Places an order from a user's cart or from a guest checkout request.
     * For authenticated users, reserves stock and creates a payment intent.
     * For guests, creates the order directly from the provided request.
     *
     * @param userId       the authenticated user's ID, or null for guest checkout
     * @param guestRequest the guest checkout details (required when userId is null)
     * @return the placed order response
     * @throws IllegalArgumentException if guest checkout is missing its request
     * @throws IllegalStateException    if the cart is empty, stock is insufficient, or a product is inactive
     */
    @Transactional
    public OrderResponse placeOrder(Long userId, GuestCheckoutRequest guestRequest) {
        if (userId != null) {
            // Authenticated user checkout
            User user = getUserForCheckout(userId);
            Cart cart = getCartForCheckout(userId);

            validateCart(cart);

            // Check for inactive products and reserve stock atomically
            for (CartItem item : cart.getItems()) {
                if (!item.getProduct().isActive()) {
                    throw new IllegalStateException("Cannot purchase inactive product: " + item.getProduct().getName());
                }
                // Check for insufficient stock
                int availableStock = item.getProduct().getStockQuantity() - item.getProduct().getReservedStock();
                if (item.getQuantity() > availableStock) {
                    throw new IllegalStateException("Insufficient stock for product: " + item.getProduct().getName());
                }
                // Reserve stock for this order atomically
                int reserved = productRepository.reserveStock(item.getProduct().getId(), item.getQuantity());
                if (reserved == 0) {
                    throw new IllegalStateException("Insufficient stock for product: " + item.getProduct().getName());
                }
            }

            Order order = createOrder(user, cart);

            // Save the order first so we have an ID for the payment intent.
            Order savedOrder = orderRepository.save(order);

            // Create a payment intent for the order. Stock will be deducted when
            // payment is captured, not at order placement time.
            paymentService.createPaymentIntent(savedOrder.getId());

            // Remove the purchased items and consumed coupon from the cart.
            cart.getItems().clear();
            cart.setCouponCode(null);
            cartRepository.save(cart);

            return orderMapper.toResponse(savedOrder);
        } else {
            // Guest checkout - requires guestRequest
            if (guestRequest == null) {
                throw new IllegalArgumentException("Guest checkout request is required for unauthenticated orders");
            }

            // Create order directly from guest request (guests don't have carts)
            Order order = createGuestOrder(guestRequest);

            // Save the order first so we have an ID for the payment intent.
            Order savedOrder = orderRepository.save(order);

            // Create a payment intent for the order.
            paymentService.createPaymentIntent(savedOrder.getId());

            return orderMapper.toResponse(savedOrder);
        }
    }

    /**
     * Completes an order after payment has been captured. Captures stock for each
     * item and confirms the order.
     *
     * @param orderId the order ID
     * @throws ResourceNotFoundException if the order does not exist
     */
    @Transactional
    public OrderResponse completeOrderWithPayment(Long orderId) {
        // Lock the order row to prevent race conditions with order cancellation
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if order is in a valid state for payment completion
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot complete payment for order in status: " + order.getStatus());
        }

        // Always check product active status, regardless of stock deduction state
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new IllegalStateException("Cannot purchase inactive product: " + product.getName());
            }
        }

        // Capture stock for each purchased product only if not already deducted.
        // For orders created before the stock reservation feature, stock was already
        // deducted at checkout (stockDeducted = true), so we skip capture to avoid
        // double deduction.
        if (!order.isStockDeducted()) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                int rowsUpdated = productRepository.captureStock(product.getId(), item.getQuantity());
                if (rowsUpdated == 0) {
                    if (product.getReservedStock() > 0) {
                        // Reservation-backed order but capture failed (reservation released by
                        // concurrent cancellation or insufficient stock). Reservation failure
                        // must be fatal for reservation-backed orders.
                        throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                    }
                    // Legacy order without stock reservations: deduct stock directly.
                    // Existing pending orders created before the reservation feature have
                    // no reserved units, so captureStock returns 0 even when physical stock
                    // is available.
                    rowsUpdated = productRepository.deductStock(product.getId(), item.getQuantity());
                    if (rowsUpdated == 0) {
                        throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                    }
                }
            }
        }
        order.setStatus(OrderStatus.CONFIRMED);
        order.setStockDeducted(true);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Cancels an order after payment has been refunded. Restores stock for each item
     * only if stock was previously deducted. Releases reserved stock only if not already captured.
     *
     * @param orderId the order ID
     * @throws ResourceNotFoundException if the order does not exist
     */
    @Transactional
    public OrderResponse cancelOrderWithRefund(Long orderId) {
        // Lock the order row to prevent race conditions with payment capture
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // If already canceled, skip stock changes to avoid double-release of reservations
        if (order.getStatus() == OrderStatus.CANCELED) {
            return orderMapper.toResponse(order);
        }

        // Only allow refund for orders that are not DELIVERED or SHIPPED
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot refund " + order.getStatus() + " order without return authorization");
        }

        // Restore stock for each item only if stock was previously deducted.
        if (order.isStockDeducted()) {
            for (OrderItem item : order.getItems()) {
                productRepository.restoreStock(item.getProduct().getId(), item.getQuantity());
            }
            order.setStockDeducted(false);
        } else {
            // Order was not captured - release reserved stock only if product has reservations
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getReservedStock() > 0) {
                    productRepository.releaseReservedStock(product.getId(), item.getQuantity());
                }
            }
        }

        order.setStatus(OrderStatus.CANCELED);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponse(updatedOrder);
    }

    /**
     * Retrieves a user's order history, newest first.
     *
     * @param userId the user whose orders to retrieve
     * @return list of order responses
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long userId) {
        User user = getUserForCheckout(userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves orders for admin, optionally filtered by status, paginated newest first.
     *
     * @param status   the order status to filter by, or null for all
     * @param pageable the pagination request
     * @return page of order responses
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForAdmin(OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository
                    .findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(orderMapper::toResponse);
        }
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(orderMapper::toResponse);
    }

    /**
     * Retrieves an order owned by the given user.
     *
     * @param userId the user requesting the order
     * @param orderId the order ID
     * @return the order response
     * @throws ResourceNotFoundException if the order does not exist or does not belong to the user
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long userId, Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                // checks requesting userID and existingOrders userId for match
                .filter(existingOrder -> existingOrder.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND));

        return orderMapper.toResponse(order);
    }

    /**
     * Retrieves an order by ID for admin access.
     *
     * @param orderId the order ID
     * @return the order response
     * @throws ResourceNotFoundException if the order does not exist
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailsForAdmin(Long orderId) {
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND));

        return orderMapper.toResponse(order);
    }

    /**
     * Updates an order's status following valid transitions.
     * PENDING -> CONFIRMED requires payment to be captured.
     * Restores stock when canceling or expiring captured orders.
     *
     * @param orderId   the order ID
     * @param status    the new order status
     * @param changedBy the user making the change
     * @return the updated order response
     * @throws IllegalStateException if the transition is invalid or payment is not captured
     * @throws ResourceNotFoundException if the order does not exist
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status, String changedBy) {
        // Lock the order row to prevent race conditions with payment capture
        Order order =
                orderRepository.findByIdWithLock(orderId).orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND));

        OrderStatus currentStatus = order.getStatus();
        boolean validOrderFlow = switch (currentStatus) {
            case PENDING -> status == OrderStatus.CONFIRMED || status == OrderStatus.CANCELED || status == OrderStatus.EXPIRED;
            case CONFIRMED -> status == OrderStatus.SHIPPED || status == OrderStatus.CANCELED;
            case SHIPPED -> status == OrderStatus.DELIVERED;
            case DELIVERED, CANCELED, EXPIRED -> false; // No further transitions allowed
        };

        // PENDING -> CONFIRMED requires payment to be captured
        if (currentStatus == OrderStatus.PENDING && status == OrderStatus.CONFIRMED) {
            // Check if payment exists and is captured
            if (order.getPayment() == null || order.getPayment().getStatus() != PaymentStatus.CAPTURED) {
                throw new IllegalStateException("Cannot confirm order: payment not captured");
            }
        }

        if (!validOrderFlow) {
            throw new IllegalStateException("Cannot change order status from " + currentStatus + " to " + status);
        }
        OrderStatus previousStatus = order.getStatus();
        // If the order is canceled or expired and stock was deducted, restore physical stock.
        if ((status == OrderStatus.CANCELED || status == OrderStatus.EXPIRED) && order.isStockDeducted()) {
            for (OrderItem item : order.getItems()) {
                productRepository.restoreStock(item.getProduct().getId(), item.getQuantity());
            }
            order.setStockDeducted(false);
        } else if (status == OrderStatus.CANCELED || status == OrderStatus.EXPIRED) {
            // Order was not captured - release reserved stock only if product has reservations
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getReservedStock() > 0) {
                    productRepository.releaseReservedStock(product.getId(), item.getQuantity());
                }
            }
        }
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        // Create audit entry
        OrderEvent event = new OrderEvent();
        event.setOrderId(orderId);
        event.setOldStatus(previousStatus);
        event.setNewStatus(status);
        event.setChangedBy(changedBy);
        event.setChangedAt(java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")));
        event.setReason("Status changed from " + previousStatus + " to " + status);
        orderEventRepository.save(event);

        return orderMapper.toResponse(updatedOrder);
    }

    /**
     * Scheduled job that expires PENDING orders older than 30 minutes and releases their reserved stock.
     * Runs every 5 minutes (300000 ms). Uses pessimistic locking to prevent race conditions with payment capture.
     * For legacy orders that already had stock deducted (stockDeducted=true), restores stock instead of releasing reservations.
     */
    @Scheduled(fixedDelayString = "${order.expiration.interval.minutes:300000}")
    @Transactional
    public void expirePendingOrders() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneId.of("UTC")).minusMinutes(30);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBeforeWithLock(OrderStatus.PENDING, cutoff);

        for (Order order : expiredOrders) {
            if (order.isStockDeducted()) {
                // Legacy order - stock was already deducted, restore it
                for (OrderItem item : order.getItems()) {
                    productRepository.restoreStock(item.getProduct().getId(), item.getQuantity());
                }
            } else {
                // Normal order - release reserved stock only if product has reservations
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    if (product.getReservedStock() > 0) {
                        productRepository.releaseReservedStock(product.getId(), item.getQuantity());
                    }
                }
            }
            // Expire the order
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);

            // Create audit entry
            OrderEvent event = new OrderEvent();
            event.setOrderId(order.getId());
            event.setOldStatus(OrderStatus.PENDING);
            event.setNewStatus(OrderStatus.EXPIRED);
            event.setChangedBy("system");
            event.setChangedAt(java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")));
            event.setReason("Order expired due to non-payment");
            orderEventRepository.save(event);
        }
    }
}