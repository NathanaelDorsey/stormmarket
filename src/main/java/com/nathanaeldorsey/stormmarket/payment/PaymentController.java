package com.nathanaeldorsey.stormmarket.payment;

import com.nathanaeldorsey.stormmarket.order.OrderRepository;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP entry point for payment operations.
 *
 * Uses a mock payment provider. In production, this would integrate with a real
 * payment provider (Stripe, PayPal, etc.) which requires API keys.
 *
 * Endpoints:
 *   POST   /v1/payments/intent/{orderId}   - Create a payment intent for an order
 *   POST   /v1/payments/{intentId}/authorize   - Authorize a payment intent
 *   POST   /v1/payments/{intentId}/capture   - Capture a payment intent (deducts stock, confirms order)
 *   POST   /v1/payments/{intentId}/refund   - Refund a captured payment (restores stock, cancels order)
 *   POST   /v1/payments/{intentId}/fail   - Fail a payment intent
 *   GET    /v1/payments/order/{orderId}   - Get payment for an order
 */
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void checkOrderOwnership(Long orderId, User user) {
        if (!orderRepository.existsByIdAndUserId(orderId, user.getId())) {
            throw new IllegalArgumentException("Order not found or access denied");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setIntentId(payment.getIntentId());
        response.setProvider(payment.getProvider());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setOrderId(payment.getOrderId());
        return response;
    }

    /**
     * Creates a payment intent for an order.
     *
     * @param orderId the order ID
     * @param authentication the authenticated user
     * @return the payment intent ID
     */
    @PostMapping("/intent/{orderId}")
    public ResponseEntity<String> createPaymentIntent(@PathVariable Long orderId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        checkOrderOwnership(orderId, user);
        String intentId = paymentService.createPaymentIntent(orderId);
        return ResponseEntity.ok(intentId);
    }

    /**
     * Authorizes a payment intent.
     *
     * @param intentId the payment intent ID
     * @param authentication the authenticated user
     * @return the updated payment response
     */
    @PostMapping("/{intentId}/authorize")
    public ResponseEntity<PaymentResponse> authorizePayment(@PathVariable String intentId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Payment payment = paymentService.authorizePayment(intentId, user.getId());
        return ResponseEntity.ok(toResponse(payment));
    }

    /**
     * Captures a payment intent. Deducts stock and confirms the order.
     *
     * @param intentId the payment intent ID
     * @param authentication the authenticated user
     * @return the updated payment response
     */
    @PostMapping("/{intentId}/capture")
    public ResponseEntity<PaymentResponse> capturePayment(@PathVariable String intentId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Payment payment = paymentService.capturePayment(intentId, user.getId());
        return ResponseEntity.ok(toResponse(payment));
    }

    /**
     * Refunds a captured payment. Restores stock and cancels the order.
     *
     * @param intentId the payment intent ID
     * @param authentication the authenticated user
     * @return the updated payment response
     */
    @PostMapping("/{intentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable String intentId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Payment payment = paymentService.refundPayment(intentId, user.getId());
        return ResponseEntity.ok(toResponse(payment));
    }

    /**
     * Fails a payment intent.
     *
     * @param intentId the payment intent ID
     * @param authentication the authenticated user
     * @return the updated payment response
     */
    @PostMapping("/{intentId}/fail")
    public ResponseEntity<PaymentResponse> failPayment(@PathVariable String intentId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Payment payment = paymentService.failPayment(intentId, user.getId());
        return ResponseEntity.ok(toResponse(payment));
    }

    /**
     * Gets the payment for an order.
     *
     * @param orderId the order ID
     * @param authentication the authenticated user
     * @return the payment response
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        checkOrderOwnership(orderId, user);
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(toResponse(payment));
    }
}
