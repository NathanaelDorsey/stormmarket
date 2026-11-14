package com.nathanaeldorsey.stormmarket.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.order.Order;
import com.nathanaeldorsey.stormmarket.order.OrderRepository;
import com.nathanaeldorsey.stormmarket.order.OrderService;
import com.nathanaeldorsey.stormmarket.order.OrderStatus;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.User;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
/**
 * Tests for PaymentService payment processing logic.
 * Covers payment state transitions, ownership checks, and refund protection.
 */

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private Order order;
    private Payment payment;
    private String intentId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(new BigDecimal("100.00"));

        intentId = "pi_" + UUID.randomUUID().toString();

        payment = new Payment();
        payment.setId(1L);
        payment.setOrder(order);
        payment.setIntentId("pi_test");
        payment.setProvider("mock");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void createPaymentIntent_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        String intentId = paymentService.createPaymentIntent(1L);

        assertThat(intentId).startsWith("pi_");
        verify(paymentRepository).save(any());
    }

    @Test
    void createPaymentIntent_ThrowsWhenOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPaymentIntent(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    void createPaymentIntent_ThrowsWhenPaymentAlreadyExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(new Payment()));

        assertThatThrownBy(() -> paymentService.createPaymentIntent(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Payment already exists");
    }

    @Test
    void authorizePayment_Success() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));

        Payment result = paymentService.authorizePayment("pi_test", 1L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    @Test
    void authorizePayment_ThrowsWhenNotPending() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.authorizePayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot authorize payment in state");
    }

    @Test
    void capturePayment_Success() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        Payment result = paymentService.capturePayment("pi_test", 1L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        verify(orderService).completeOrderWithPayment(1L);
    }

    @Test
    void capturePayment_ThrowsWhenNotAuthorized() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.capturePayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot capture payment in state");
    }

    @Test
    void refundPayment_Success() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        Payment result = paymentService.refundPayment("pi_test", 1L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(orderService).cancelOrderWithRefund(1L);
    }

    @Test
    void refundPayment_ThrowsWhenOrderDelivered() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        Order deliveredOrder = new Order();
        deliveredOrder.setId(1L);
        deliveredOrder.setStatus(OrderStatus.DELIVERED);
        deliveredOrder.setUser(user);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(deliveredOrder));

        assertThatThrownBy(() -> paymentService.refundPayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot refund");
    }

    @Test
    void refundPayment_ThrowsWhenNotCaptured() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.refundPayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot refund payment in state");
    }

    @Test
    void failPayment_Success() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));

        Payment result = paymentService.failPayment("pi_test", 1L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void failPayment_ThrowsWhenCaptured() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.failPayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail payment in state");
    }


    @Test
    void authorizePayment_ThrowsWhenNotOwner() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.authorizePayment("pi_test", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found or access denied");
    }

    @Test
    void capturePayment_ThrowsWhenNotOwner() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.capturePayment("pi_test", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found or access denied");
    }

    @Test
    void capturePayment_ThrowsWhenAlreadyCaptured() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.capturePayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot capture payment in state");
    }

    @Test
    void capturePayment_ThrowsWhenOrderNotPending() {
        Order shippedOrder = new Order();
        shippedOrder.setId(1L);
        shippedOrder.setUser(user);
        shippedOrder.setStatus(OrderStatus.SHIPPED);
        shippedOrder.setTotal(new BigDecimal("100.00"));

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setIntentId("pi_test");
        payment.setOrder(shippedOrder);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(shippedOrder));

        assertThatThrownBy(() -> paymentService.capturePayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot capture payment for order in status");
    }

    @Test
    void refundPayment_ThrowsWhenNotOwner() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.refundPayment("pi_test", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found or access denied");
    }

    @Test
    void refundPayment_ThrowsWhenAlreadyRefunded() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.refundPayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot refund payment in state");
    }

    @Test
    void refundPayment_ThrowsWhenOrderShipped() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        Order shippedOrder = new Order();
        shippedOrder.setId(1L);
        shippedOrder.setStatus(OrderStatus.SHIPPED);
        shippedOrder.setUser(user);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(shippedOrder));

        assertThatThrownBy(() -> paymentService.refundPayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot refund");
    }

    @Test
    void failPayment_ThrowsWhenRefunded() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.failPayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail payment in state");
    }

    @Test
    void failPayment_ThrowsWhenNotOwner() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.failPayment("pi_test", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found or access denied");
    }

    @Test
    void getPaymentByOrderId_ThrowsWhenNotFound() {
        when(paymentRepository.findByOrderId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByOrderId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment not found");
    }



    @Test
    void authorizePayment_ThrowsWhenAlreadyAuthorized() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.authorizePayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot authorize payment in state");
    }

    @Test
    void failPayment_SucceedsWhenAlreadyFailed() {
        // failPayment does not throw for already failed payments (idempotent)
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.FAILED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));

        Payment result = paymentService.failPayment("pi_test", 1L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void capturePayment_ThrowsWhenOrderCancelled() {
        Order cancelledOrder = new Order();
        cancelledOrder.setId(1L);
        cancelledOrder.setUser(user);
        cancelledOrder.setStatus(OrderStatus.CANCELED);
        cancelledOrder.setTotal(new BigDecimal("100.00"));

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setIntentId("pi_test");
        payment.setOrder(cancelledOrder);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(cancelledOrder));

        assertThatThrownBy(() -> paymentService.capturePayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot capture payment for order in status");
    }

    @Test
    void refundPayment_SucceedsWhenOrderCancelled() {
        // Current implementation allows refund for CANCELLED orders
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setIntentId("pi_test");
        payment.setOrder(order);

        Order cancelledOrder = new Order();
        cancelledOrder.setId(1L);
        cancelledOrder.setStatus(OrderStatus.CANCELED);
        cancelledOrder.setUser(user);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(cancelledOrder));

        Payment result = paymentService.refundPayment("pi_test", 1L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void capturePayment_ThrowsWhenOrderExpired() {
        Order expiredOrder = new Order();
        expiredOrder.setId(1L);
        expiredOrder.setUser(user);
        expiredOrder.setStatus(OrderStatus.EXPIRED);
        expiredOrder.setTotal(new BigDecimal("100.00"));

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setIntentId("pi_test");
        payment.setOrder(expiredOrder);

        when(paymentRepository.findByIntentIdWithLock("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(expiredOrder));

        assertThatThrownBy(() -> paymentService.capturePayment("pi_test", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot capture payment for order in status");
    }

}