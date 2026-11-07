package com.nathanaeldorsey.stormmarket.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nathanaeldorsey.stormmarket.order.OrderResponse;
import com.nathanaeldorsey.stormmarket.order.OrderStatus;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:stormmarket_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false",
    "spring.security.oauth2.resourceserver.jwt.public-key-location=classpath:jwt/public-key.pem",
    "spring.security.oauth2.resourceserver.jwt.private-key-location=classpath:jwt/private-key.pem",
})
/**
 * Tests for OrderController security boundaries.
 * Covers authentication and authorization for order endpoints.
 */

class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.nathanaeldorsey.stormmarket.order.OrderService orderService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void unauthenticatedGetOrdersReturns401() throws Exception {
        mockMvc.perform(get("/v1/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedGetOrderDetailsReturns401() throws Exception {
        mockMvc.perform(get("/v1/orders/100"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedPostOrdersAllowsGuestCheckout() throws Exception {
        OrderResponse response = new OrderResponse();
        response.setId(100L);
        response.setTotal(new java.math.BigDecimal("100.00"));
        response.setOrderStatus(OrderStatus.PENDING);

        when(orderService.placeOrder(any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/v1/orders")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@example.com\",\"addressLine1\":\"123 Main St\",\"city\":\"City\",\"state\":\"ST\",\"postalCode\":\"12345\",\"country\":\"USA\",\"shippingMethod\":\"STANDARD\",\"paymentMethod\":\"CREDIT_CARD\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void unauthenticatedPostOrdersWithInvalidDataReturns400() throws Exception {
        // Missing required fields - should return 400 validation error
        mockMvc.perform(post("/v1/orders")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"Doe\",\"email\":\"john@example.com\",\"addressLine1\":\"123 Main St\",\"city\":\"City\",\"state\":\"ST\",\"postalCode\":\"12345\",\"country\":\"USA\",\"shippingMethod\":\"STANDARD\",\"paymentMethod\":\"CREDIT_CARD\"}"))
                .andExpect(status().isBadRequest());
    }
}