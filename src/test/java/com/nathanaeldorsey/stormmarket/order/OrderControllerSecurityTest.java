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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.nathanaeldorsey.stormmarket.order.OrderService orderService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            // Mirrors the real SecurityConfig rules
            http.csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/v1/auth/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/v1/products/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/v1/categories/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/v1/orders").permitAll() // guest checkout
                            .requestMatchers(HttpMethod.POST, "/v1/products/**", "/v1/categories/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/v1/products/**", "/v1/categories/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/v1/products/**", "/v1/categories/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/v1/files/**").permitAll()
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> {
                        }));
            return http.build();
        }
    }

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
        // It may return 400 (missing body) or 201, but never 401
        var result = mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, result.getResponse().getStatus(),
                "POST /v1/orders should be permitAll for guests, not return 401");
    }

    @Test
    void guestCheckoutPermitAll() throws Exception {
        // POST /v1/orders without auth should NOT return 401 (it's permitAll)
        // It may return 400 for missing body, but not 401
        var result = mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertNot401(result.getResponse().getStatus());
    }

    private void assertNot401(int status) {
        org.junit.jupiter.api.Assertions.assertNotEquals(401, status,
                "Guest checkout should be permitAll, not return 401");
    }
}
