package com.nathanaeldorsey.stormmarket.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
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
class CartControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CartController cartController;

    @Test
    void unauthenticatedGetCartReturns401() throws Exception {
        mockMvc.perform(get("/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedPostCartReturns401() throws Exception {
        mockMvc.perform(post("/v1/cart")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedPutCartReturns401() throws Exception {
        mockMvc.perform(put("/v1/cart/1")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedDeleteCartItemReturns401() throws Exception {
        mockMvc.perform(delete("/v1/cart/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedDeleteCartReturns401() throws Exception {
        mockMvc.perform(delete("/v1/cart"))
                .andExpect(status().isUnauthorized());
    }
}