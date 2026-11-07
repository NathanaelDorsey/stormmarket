package com.nathanaeldorsey.stormmarket.payment;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.order.Order;
import com.nathanaeldorsey.stormmarket.order.OrderRepository;
import com.nathanaeldorsey.stormmarket.order.OrderStatus;
import com.nathanaeldorsey.stormmarket.order.OrderService;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages payment processing for orders.
 *
 * Uses a mock payment provider. In production, this would integrate with a real
 * payment provider such as Stripe, PayPal, or Adyen, which requires API keys.
 *
 * Flow: PENDING -> AUTHORIZED -> CAPTURED -> REFUNDED,
 * with FAILED as a terminal state reachable from PENDING or AUTHORIZED.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository,
            @Lazy OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderService = orderService;
    }

    /**
     * Creates a payment intent for an order. Called when an order is placed.
     *
     * @param orderId the order ID
     * @return the payment intent ID
     * @throws ResourceNotFoundException if the order does not exist
     */
    @Transactional
    public String createPaymentIntent(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("Payment already exists for order: " + orderId);
        }

        String intentId = "pi_" + UUID.randomUUID().toString();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setIntentId(intentId);
        payment.setProvider("mock");
        payment.setAmount(order.getTotal());
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);

        return intentId;
    }

    /**
     * Authorizes a payment intent (mock). Verifies the payment can be authorized.
     *
     * @param intentId the payment intent ID
     * @param userId the user ID (for ownership check)
     * @return the updated payment
     * @throws ResourceNotFoundException if the payment does not exist
     */
    @Transactional
    public Payment authorizePayment(String intentId, Long userId) {
        // Use pessimistic lock to prevent concurrent operations
        Payment payment = paymentRepository.findByIntentIdWithLock(intentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Payment not found or access denied");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot authorize payment in state: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.AUTHORIZED);
        paymentRepository.save(payment);

        return payment;
    }

    /**
     * Captures a payment intent (mock). Captures the payment, deducts stock,
     * and confirms the order.
     *
     * @param intentId the payment intent ID
     * @param userId the user ID (for ownership check)
     * @return the updated payment
     * @throws ResourceNotFoundException if the payment does not exist
     */
    @Transactional
    public Payment capturePayment(String intentId, Long userId) {
        // Use pessimistic lock to prevent concurrent capture/refund
        Payment payment = paymentRepository.findByIntentIdWithLock(intentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Payment not found or access denied");
        }

        // Check order state - only PENDING orders can be captured
        Order order = payment.getOrder();
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot capture payment for order in status: " + order.getStatus());
        }

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new IllegalStateException("Cannot capture payment in state: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.CAPTURED);
        paymentRepository.save(payment);

        // Deduct stock and confirm the order
        orderService.completeOrderWithPayment(payment.getOrder().getId());

        return payment;
    }

    /**
     * Refunds a captured payment. Restores stock and cancels the order.
     *
     * @param intentId the payment intent ID
     * @param userId the user ID (for ownership check)
     * @return the updated payment
     * @throws ResourceNotFoundException if the payment does not exist
     */
    @Transactional
    public Payment refundPayment(String intentId, Long userId) {
        // Use pessimistic lock to prevent concurrent capture/refund
        Payment payment = paymentRepository.findByIntentIdWithLock(intentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Payment not found or access denied");
        }

        // Lock the order row before reading its state to prevent race conditions
        // with concurrent shipping/cancellation
        Order order = orderRepository.findByIdWithLock(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check order state - only CAPTURED orders that are not DELIVERED/SHIPPED can be refunded
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot refund " + order.getStatus() + " order without return authorization");
        }

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Cannot refund payment in state: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        // Restore stock and cancel the order
        orderService.cancelOrderWithRefund(payment.getOrderId());

        return payment;
    }

    /**
     * Fails a payment intent.
     *
     * @param intentId the payment intent ID
     * @param userId the user ID (for ownership check)
     * @return the updated payment
     * @throws ResourceNotFoundException if the payment does not exist
     */
    @Transactional
    public Payment failPayment(String intentId, Long userId) {
        // Use pessimistic lock to prevent concurrent operations
        Payment payment = paymentRepository.findByIntentIdWithLock(intentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Payment not found or access denied");
        }

        if (payment.getStatus() == PaymentStatus.CAPTURED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Cannot fail payment in state: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        return payment;
    }

    /**
     * Gets the payment for an order.
     *
     * @param orderId the order ID
     * @return the payment
     * @throws ResourceNotFoundException if the payment does not exist
     */
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }
}
