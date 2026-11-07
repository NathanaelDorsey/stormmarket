package com.nathanaeldorsey.stormmarket.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:stormmarket_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false",
    "spring.security.oauth2.resourceserver.jwt.public-key-location=classpath:jwt/public-key.pem",
    "spring.security.oauth2.resourceserver.jwt.private-key-location=classpath:jwt/private-key.pem",
})
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
    void unauthenticatedPostOrdersReturnsNot401() throws Exception {
        // Guest checkout is permitAll, so it should NOT return 401
        var result = mockMvc.perform(post("/v1/orders")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andReturn();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, result.getResponse().getStatus(),
                "POST /v1/orders should be permitAll for guests, not return 401");
    }
}
