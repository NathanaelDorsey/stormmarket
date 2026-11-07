package com.nathanaeldorsey.stormmarket.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceAlreadyExistsException;
import com.nathanaeldorsey.stormmarket.order.Order;
import com.nathanaeldorsey.stormmarket.order.OrderRepository;
import com.nathanaeldorsey.stormmarket.order.OrderService;
import com.nathanaeldorsey.stormmarket.order.OrderStatus;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderService orderService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository, orderRepository, productRepository, orderService);
    }

    private Payment createPayment(String intentId, Order order, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setIntentId(intentId);
        payment.setOrder(order);
        payment.setStatus(status);
        return payment;
    }

    private Order createOrder(Long id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        return order;
    }

    // --- Ownership tests ---

    @Test
    void authorizePaymentRejectsOtherUsersOrder() {
        Order order = createOrder(1L, OrderStatus.PENDING);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(99L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.PENDING);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.authorizePayment("pi_1", 1L));
    }

    @Test
    void capturePaymentRejectsOtherUsersOrder() {
        Order order = createOrder(1L, OrderStatus.PENDING);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(99L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.AUTHORIZED);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.capturePayment("pi_1", 1L));
    }

    @Test
    void refundPaymentRejectsOtherUsersOrder() {
        Order order = createOrder(1L, OrderStatus.CONFIRMED);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(99L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.CAPTURED);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.refundPayment("pi_1", 1L));
    }

    // --- Payment state rules ---

    @Test
    void authorizePaymentRejectsNonPendingStatus() {
        Order order = createOrder(1L, OrderStatus.PENDING);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(1L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.AUTHORIZED);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class,
                () -> paymentService.authorizePayment("pi_1", 1L));
    }

    @Test
    void capturePaymentRejectsNonAuthorizedStatus() {
        Order order = createOrder(1L, OrderStatus.PENDING);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(1L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.PENDING);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class,
                () -> paymentService.capturePayment("pi_1", 1L));
    }

    @Test
    void refundPaymentRejectsNonCapturedStatus() {
        Order order = createOrder(1L, OrderStatus.PENDING);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(1L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.PENDING);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> paymentService.refundPayment("pi_1", 1L));
    }

    @Test
    void refundPaymentRejectsShippedOrder() {
        Order order = createOrder(1L, OrderStatus.SHIPPED);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(1L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.CAPTURED);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> paymentService.refundPayment("pi_1", 1L));
    }

    @Test
    void refundPaymentRejectsDeliveredOrder() {
        Order order = createOrder(1L, OrderStatus.DELIVERED);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(1L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.CAPTURED);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> paymentService.refundPayment("pi_1", 1L));
    }

    @Test
    void failPaymentRejectsCapturedOrRefunded() {
        for (PaymentStatus status : new PaymentStatus[]{PaymentStatus.CAPTURED, PaymentStatus.REFUNDED}) {
            Order order = createOrder(1L, OrderStatus.PENDING);
            com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
            user.setId(1L);
            order.setUser(user);

            Payment payment = createPayment("pi_1", order, status);

            when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));

            assertThrows(IllegalStateException.class,
                    () -> paymentService.failPayment("pi_1", 1L));
        }
    }

    // --- Not found ---

    @Test
    void authorizePaymentThrowsWhenNotFound() {
        when(paymentRepository.findByIntentIdWithLock("pi_missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.authorizePayment("pi_missing", 1L));
    }

    @Test
    void capturePaymentThrowsWhenNotFound() {
        when(paymentRepository.findByIntentIdWithLock("pi_missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.capturePayment("pi_missing", 1L));
    }

    @Test
    void refundPaymentThrowsWhenNotFound() {
        when(paymentRepository.findByIntentIdWithLock("pi_missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.refundPayment("pi_missing", 1L));
    }

    // --- Successful flows ---

    @Test
    void authorizePaymentSuccess() {
        Order order = createOrder(1L, OrderStatus.PENDING);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(1L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.PENDING);
        Payment authorized = createPayment("pi_1", order, PaymentStatus.AUTHORIZED);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(authorized);

        Payment result = paymentService.authorizePayment("pi_1", 1L);

        assertEquals(PaymentStatus.AUTHORIZED, result.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void capturePaymentSuccess() {
        Order order = createOrder(1L, OrderStatus.PENDING);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(1L);
        order.setUser(user);
        order.setTotal(new BigDecimal("99.99"));

        Payment payment = createPayment("pi_1", order, PaymentStatus.AUTHORIZED);
        Payment captured = createPayment("pi_1", order, PaymentStatus.CAPTURED);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(captured);
        // completeOrderWithPayment is void-ish, returns OrderResponse but we don't need to stub it


        Payment result = paymentService.capturePayment("pi_1", 1L);

        assertEquals(PaymentStatus.CAPTURED, result.getStatus());
        verify(orderService).completeOrderWithPayment(1L);
    }

    @Test
    void refundPaymentSuccess() {
        Order order = createOrder(1L, OrderStatus.CONFIRMED);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(1L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.CAPTURED);
        Payment refunded = createPayment("pi_1", order, PaymentStatus.REFUNDED);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(refunded);
        when(orderService.cancelOrderWithRefund(1L)).thenReturn(
                new com.nathanaeldorsey.stormmarket.order.OrderResponse());

        Payment result = paymentService.refundPayment("pi_1", 1L);

        assertEquals(PaymentStatus.REFUNDED, result.getStatus());
        verify(orderService).cancelOrderWithRefund(1L);
    }

    @Test
    void failPaymentSuccess() {
        Order order = createOrder(1L, OrderStatus.PENDING);
        com.nathanaeldorsey.stormmarket.user.User user = new com.nathanaeldorsey.stormmarket.user.User();
        user.setId(1L);
        order.setUser(user);

        Payment payment = createPayment("pi_1", order, PaymentStatus.PENDING);
        Payment failed = createPayment("pi_1", order, PaymentStatus.FAILED);

        when(paymentRepository.findByIntentIdWithLock("pi_1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(failed);

        Payment result = paymentService.failPayment("pi_1", 1L);

        assertEquals(PaymentStatus.FAILED, result.getStatus());
    }

    @Test
    void createPaymentIntentSuccess() {
        Order order = createOrder(1L, OrderStatus.PENDING);
        order.setTotal(new BigDecimal("49.99"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        String intentId = paymentService.createPaymentIntent(1L);

        assertNotNull(intentId);
        assertTrue(intentId.startsWith("pi_"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPaymentIntentRejectsDuplicate() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(createOrder(1L, OrderStatus.PENDING)));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(new Payment()));

        assertThrows(IllegalStateException.class,
                () -> paymentService.createPaymentIntent(1L));
    }

    @Test
    void getPaymentByOrderIdThrowsWhenNotFound() {
        when(paymentRepository.findByOrderId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getPaymentByOrderId(99L));
    }
}
