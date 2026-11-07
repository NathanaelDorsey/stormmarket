package com.nathanaeldorsey.stormmarket.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = OrderController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration.class,
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration.class
        })
@AutoConfigureMockMvc
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.nathanaeldorsey.stormmarket.order.OrderService orderService;

    @MockitoBean
    private com.nathanaeldorsey.stormmarket.user.UserRepository userRepository;

    @Configuration
    @EnableWebSecurity
    static class SecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(ex -> ex.authenticationEntryPoint(
                            (request, response, authException) -> response.setStatus(
                                    jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED)));
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
    void unauthenticatedPostOrdersReturns401() throws Exception {
        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
