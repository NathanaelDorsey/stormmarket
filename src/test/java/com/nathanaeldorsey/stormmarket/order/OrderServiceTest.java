package com.nathanaeldorsey.stormmarket.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nathanaeldorsey.stormmarket.cart.Cart;
import com.nathanaeldorsey.stormmarket.cart.CartItem;
import com.nathanaeldorsey.stormmarket.cart.CartRepository;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.Role;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository, cartRepository, userRepository, productRepository, orderMapper);
    }

    @Test
    void placeOrderSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(10);
        product.setActive(true);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(2);
        item.setProduct(product);
        item.setCart(cart);
        cart.getItems().add(item);

        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setUser(user);
        savedOrder.setTotal(new BigDecimal("59.98"));
        savedOrder.setStatus(OrderStatus.PENDING);

        OrderResponse response = new OrderResponse();
        response.setId(100L);
        response.setTotal(new BigDecimal("59.98"));
        response.setOrderStatus(OrderStatus.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.deductStock(10L, 2)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder)).thenReturn(response);

        OrderResponse result = orderService.placeOrder(1L);

        assertEquals(100L, result.getId());
        assertEquals(new BigDecimal("59.98"), result.getTotal());
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        verify(productRepository).deductStock(10L, 2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrderRejectsEmptyCart() {
        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        // Empty items list

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(1L));
    }

    @Test
    void placeOrderRejectsInsufficientStock() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(1);
        product.setActive(true);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(5); // More than stock
        item.setProduct(product);
        item.setCart(cart);
        cart.getItems().add(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.deductStock(10L, 5)).thenReturn(0); // Zero rows = insufficient stock

        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(1L));
    }

    @Test
    void placeOrderRejectsInactiveProduct() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(10);
        product.setActive(false); // Inactive

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(2);
        item.setProduct(product);
        item.setCart(cart);
        cart.getItems().add(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(1L));
    }

    @Test
    void placeOrderClearsCart() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(10);
        product.setActive(true);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(2);
        item.setProduct(product);
        item.setCart(cart);
        cart.getItems().add(item);

        Order savedOrder = new Order();
        savedOrder.setId(100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.deductStock(10L, 2)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        orderService.placeOrder(1L);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void getOrderHistoryReturnsUserOrders() {
        User user = new User();
        user.setId(1L);

        Order order1 = new Order();
        order1.setId(100L);
        order1.setUser(user);
        order1.setTotal(new BigDecimal("29.99"));
        order1.setStatus(OrderStatus.DELIVERED);

        Order order2 = new Order();
        order2.setId(101L);
        order2.setUser(user);
        order2.setTotal(new BigDecimal("59.98"));
        order2.setStatus(OrderStatus.PENDING);

        OrderResponse resp1 = new OrderResponse();
        resp1.setId(100L);
        resp1.setTotal(new BigDecimal("29.99"));
        resp1.setOrderStatus(OrderStatus.DELIVERED);

        OrderResponse resp2 = new OrderResponse();
        resp2.setId(101L);
        resp2.setTotal(new BigDecimal("59.98"));
        resp2.setOrderStatus(OrderStatus.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order1, order2));
        when(orderMapper.toResponse(order1)).thenReturn(resp1);
        when(orderMapper.toResponse(order2)).thenReturn(resp2);

        List<OrderResponse> result = orderService.getOrderHistory(1L);

        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(101L, result.get(1).getId());
    }

    @Test
    void getOrderDetailsReturnsOrderForOwner() {
        User user = new User();
        user.setId(1L);

        Order order = new Order();
        order.setId(100L);
        order.setUser(user);

        OrderResponse response = new OrderResponse();
        response.setId(100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.getOrderDetails(1L, 100L);

        assertEquals(100L, result.getId());
    }

    @Test
    void getOrderDetailsRejectsOtherUsersOrder() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        Order order = new Order();
        order.setId(100L);
        order.setUser(user2); // Owned by user2

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderDetails(1L, 100L));
    }

    @Test
    void getOrderDetailsForAdminReturnsAnyOrder() {
        Order order = new Order();
        order.setId(100L);

        OrderResponse response = new OrderResponse();
        response.setId(100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.getOrderDetailsForAdmin(100L);

        assertEquals(100L, result.getId());
    }

    @Test
    void getOrderDetailsForAdminThrowsWhenNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderDetailsForAdmin(999L));
    }

    @Test
    void updateOrderStatusValidTransitions() {
        Order order = new Order();
        order.setId(100L);
        order.setStatus(OrderStatus.PENDING);

        Order updatedOrder = new Order();
        updatedOrder.setId(100L);
        updatedOrder.setStatus(OrderStatus.CONFIRMED);

        OrderResponse response = new OrderResponse();
        response.setId(100L);
        response.setOrderStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderMapper.toResponse(updatedOrder)).thenReturn(response);

        OrderResponse result = orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, result.getOrderStatus());
    }

    @Test
    void updateOrderStatusRejectsInvalidTransition() {
        Order order = new Order();
        order.setId(100L);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED));
    }

    @Test
    void updateOrderStatusCanceledRestoresStock() {
        Order order = new Order();
        order.setId(100L);
        order.setStatus(OrderStatus.CONFIRMED);

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(3);
        order.getItems().add(item);

        Order updatedOrder = new Order();
        updatedOrder.setId(100L);
        updatedOrder.setStatus(OrderStatus.CANCELED);

        OrderResponse response = new OrderResponse();
        response.setId(100L);
        response.setOrderStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderMapper.toResponse(updatedOrder)).thenReturn(response);

        OrderResponse result = orderService.updateOrderStatus(100L, OrderStatus.CANCELED);

        assertEquals(OrderStatus.CANCELED, result.getOrderStatus());
        verify(productRepository).restoreStock(10L, 3);
    }

    @Test
    void getOrdersForAdminReturnsPaginated() {
        Order order1 = new Order();
        order1.setId(100L);
        order1.setStatus(OrderStatus.PENDING);

        Order order2 = new Order();
        order2.setId(101L);
        order2.setStatus(OrderStatus.CONFIRMED);

        OrderResponse resp1 = new OrderResponse();
        resp1.setId(100L);
        resp1.setOrderStatus(OrderStatus.PENDING);

        OrderResponse resp2 = new OrderResponse();
        resp2.setId(101L);
        resp2.setOrderStatus(OrderStatus.CONFIRMED);

        Page<Order> page = new PageImpl<>(List.of(order1, order2));

        when(orderRepository.findByStatusOrderByCreatedAtDesc(eq(OrderStatus.PENDING), any(Pageable.class)))
                .thenReturn(page);
        when(orderMapper.toResponse(order1)).thenReturn(resp1);
        when(orderMapper.toResponse(order2)).thenReturn(resp2);

        Page<OrderResponse> result = orderService.getOrdersForAdmin(OrderStatus.PENDING, Pageable.unpaged());

        assertEquals(2, result.getTotalElements());
    }
}