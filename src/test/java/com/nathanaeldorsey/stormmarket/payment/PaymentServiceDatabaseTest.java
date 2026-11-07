package com.nathanaeldorsey.stormmarket.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nathanaeldorsey.stormmarket.order.Order;
import com.nathanaeldorsey.stormmarket.order.OrderItem;
import com.nathanaeldorsey.stormmarket.order.OrderRepository;
import com.nathanaeldorsey.stormmarket.order.OrderService;
import com.nathanaeldorsey.stormmarket.order.OrderStatus;
import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.Role;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Database-backed tests for PaymentService using the embedded H2 test database.
 *
 * Covers the Greptile P2 finding "Payment rules lack tests":
 * - Stock cleanup on capture (stockQuantity actually decreases)
 * - Stock cleanup on refund (stockQuantity actually increases)
 * - Pessimistic locking serializes concurrent capture/refund on the same payment
 * - SHIPPED/DELIVERED orders cannot be refunded (database-backed)
 */
@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceDatabaseTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private User user;
    private Product product;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername("dbtestuser");
        user.setPassword("password");
        user.setEmail("dbtest@example.com");
        user.setRole(Role.USER);
        user = userRepository.saveAndFlush(user);

        product = new Product();
        product.setName("DB Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(10);
        product.setReservedStock(0);
        product.setActive(true);
        product = productRepository.saveAndFlush(product);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Order savePendingOrderWithItem(int quantity) {
        Order order = new Order();
        order.setUser(user);
        order.setTotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setStatus(OrderStatus.PENDING);
        order.setStockDeducted(false);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        order.addItem(item);

        return orderRepository.saveAndFlush(order);
    }

    private Payment savePayment(Order order, PaymentStatus status, String intentId) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setIntentId(intentId);
        payment.setProvider("mock");
        payment.setAmount(order.getTotal());
        payment.setCurrency("USD");
        payment.setStatus(status);
        return paymentRepository.saveAndFlush(payment);
    }

    /* ========================================================================
       Stock cleanup on capture
       ======================================================================== */

    @Test
    void capturePaymentDeductsProductStockFromDatabase() {
        // Given: a PENDING order with an item and an AUTHORIZED payment
        order = savePendingOrderWithItem(3);
        payment = savePayment(order, PaymentStatus.AUTHORIZED, "pi_capture_stock");

        int stockBefore = product.getStockQuantity(); // 10

        // When: payment is captured
        Payment result = paymentService.capturePayment("pi_capture_stock", user.getId());

        // Then: payment and order transition correctly
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        Order refreshedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(refreshedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(refreshedOrder.isStockDeducted()).isTrue();

        // And: product stock is deducted in the database
        Product refreshedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(refreshedProduct.getStockQuantity()).isEqualTo(stockBefore - 3);
    }

    /* ========================================================================
       Stock cleanup on refund
       ======================================================================== */

    @Test
    void refundPaymentRestoresProductStockToDatabase() {
        // Given: a CONFIRMED order with stock deducted and a CAPTURED payment
        order = savePendingOrderWithItem(2);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setStockDeducted(true);
        order = orderRepository.saveAndFlush(order);

        payment = savePayment(order, PaymentStatus.CAPTURED, "pi_refund_stock");

        int stockBefore = product.getStockQuantity(); // 10 (not yet deducted in this scenario)

        // Simulate stock having been deducted by capture
        product.setStockQuantity(stockBefore - 2);
        product = productRepository.saveAndFlush(product);

        // When: payment is refunded
        Payment result = paymentService.refundPayment("pi_refund_stock", user.getId());

        // Then: payment and order transition correctly
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        Order refreshedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(refreshedOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(refreshedOrder.isStockDeducted()).isFalse();

        // And: product stock is restored in the database
        Product refreshedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(refreshedProduct.getStockQuantity()).isEqualTo(stockBefore);
    }

    /* ========================================================================
       Database-backed locking: concurrent access is serialized
       ======================================================================== */

    @Test
    void concurrentRefundCallsAreSerializedByPessimisticLock() throws Exception {
        // Given: a CONFIRMED order with stock deducted and a CAPTURED payment
        order = savePendingOrderWithItem(1);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setStockDeducted(true);
        order = orderRepository.saveAndFlush(order);

        payment = savePayment(order, PaymentStatus.CAPTURED, "pi_concurrent_lock");

        int threadCount = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        AtomicReference<Throwable> firstError = new AtomicReference<>();
        AtomicReference<Throwable> secondError = new AtomicReference<>();
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            AtomicReference<Throwable> errorRef = (i == 0) ? firstError : secondError;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    paymentService.refundPayment("pi_concurrent_lock", user.getId());
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    errorRef.set(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertThat(completed).as("Concurrent refund calls did not complete within timeout").isTrue();

        // Exactly one should succeed (the one that acquired the lock first),
        // the other should fail because the payment is already REFUNDED.
        assertThat(successCount.get()).isEqualTo(1);

        List<Throwable> errors = Stream.of(firstError.get(), secondError.get())
                .filter(Objects::nonNull)
                .toList();
        assertThat(errors).filteredOn(t -> t != null)
                .allMatch(t -> t instanceof IllegalStateException
                        && t.getMessage().contains("Cannot refund payment in state"));

        // Final state: payment is REFUNDED, order is CANCELED
        Payment refreshedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(refreshedPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

        Order refreshedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(refreshedOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    /* ========================================================================
       SHIPPED/DELIVERED orders cannot be refunded (database-backed)
       ======================================================================== */

    @Test
    void refundPaymentRejectsShippedOrderFromDatabase() {
        // Given: a SHIPPED order with stock deducted and a CAPTURED payment
        order = savePendingOrderWithItem(1);
        order.setStatus(OrderStatus.SHIPPED);
        order.setStockDeducted(true);
        order = orderRepository.saveAndFlush(order);

        payment = savePayment(order, PaymentStatus.CAPTURED, "pi_shipped_no_refund");

        // When / Then: refund is rejected for shipped orders
        assertThatThrownBy(() -> paymentService.refundPayment("pi_shipped_no_refund", user.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot refund SHIPPED order without return authorization");
    }

    @Test
    void refundPaymentRejectsDeliveredOrderFromDatabase() {
        // Given: a DELIVERED order with stock deducted and a CAPTURED payment
        order = savePendingOrderWithItem(1);
        order.setStatus(OrderStatus.DELIVERED);
        order.setStockDeducted(true);
        order = orderRepository.saveAndFlush(order);

        payment = savePayment(order, PaymentStatus.CAPTURED, "pi_delivered_no_refund");

        // When / Then: refund is rejected for delivered orders
        assertThatThrownBy(() -> paymentService.refundPayment("pi_delivered_no_refund", user.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot refund DELIVERED order without return authorization");
    }
}
