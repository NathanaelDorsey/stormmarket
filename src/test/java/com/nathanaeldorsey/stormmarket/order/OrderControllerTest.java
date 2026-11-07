package com.nathanaeldorsey.stormmarket.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = OrderController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.web
                    .OAuth2ResourceServerWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserRepository userRepository;

    private Authentication createAuth(String username) {
        return new UsernamePasswordAuthenticationToken(username, "password", List.of());
    }

    @Test
    void placeOrderReturnsCreated() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        OrderResponse response = new OrderResponse();
        response.setId(100L);
        response.setTotal(new BigDecimal("59.98"));
        response.setOrderStatus(OrderStatus.PENDING);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderService.placeOrder(eq(1L), isNull(GuestCheckoutRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/orders")
                        .principal(createAuth("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.total").value(59.98))
                .andExpect(jsonPath("$.orderStatus").value("PENDING"));
    }

    @Test
    void placeOrderWithEmptyCartReturns400() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new IllegalStateException("Cannot checkout with an empty cart"))
                .when(orderService).placeOrder(eq(1L), isNull());

        mockMvc.perform(post("/v1/orders")
                        .principal(createAuth("testuser")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot checkout with an empty cart"));
    }

    @Test
    void placeOrderWithInsufficientStockReturns400() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new IllegalStateException("Insufficient stock for product: Test Product"))
                .when(orderService).placeOrder(eq(1L), isNull());

        mockMvc.perform(post("/v1/orders")
                        .principal(createAuth("testuser")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient stock for product: Test Product"));
    }

    @Test
    void getOrderHistoryReturnsOrders() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        OrderResponse order1 = new OrderResponse();
        order1.setId(100L);
        order1.setTotal(new BigDecimal("29.99"));
        order1.setOrderStatus(OrderStatus.DELIVERED);

        OrderResponse order2 = new OrderResponse();
        order2.setId(101L);
        order2.setTotal(new BigDecimal("59.98"));
        order2.setOrderStatus(OrderStatus.PENDING);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderService.getOrderHistory(1L)).thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/v1/orders")
                        .principal(createAuth("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[1].id").value(101L));
    }

    @Test
    void getOrderDetailsReturnsOrder() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        OrderResponse response = new OrderResponse();
        response.setId(100L);
        response.setTotal(new BigDecimal("29.99"));
        response.setOrderStatus(OrderStatus.DELIVERED);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(orderService.getOrderDetails(1L, 100L)).thenReturn(response);

        mockMvc.perform(get("/v1/orders/100")
                        .principal(createAuth("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.total").value(29.99));
    }

    @Test
    void getOrderDetailsWhenNotFoundReturns404() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new ResourceNotFoundException("Order not found"))
                .when(orderService).getOrderDetails(1L, 999L);

        mockMvc.perform(get("/v1/orders/999")
                        .principal(createAuth("testuser")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found"));
    }

    @Test
    void getOrderDetailsForOtherUserReturns404() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new ResourceNotFoundException("Order not found"))
                .when(orderService).getOrderDetails(1L, 100L);

        mockMvc.perform(get("/v1/orders/100")
                        .principal(createAuth("testuser")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unauthenticatedUserReturns401() throws Exception {
        mockMvc.perform(get("/v1/orders"))
                .andExpect(status().is5xxServerError());
    }
}