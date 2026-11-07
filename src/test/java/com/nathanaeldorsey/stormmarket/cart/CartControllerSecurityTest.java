package com.nathanaeldorsey.stormmarket.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        controllers = CartController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration.class,
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration.class
        })
@AutoConfigureMockMvc
class CartControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

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
    void unauthenticatedGetCartReturns401() throws Exception {
        mockMvc.perform(get("/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedPostCartReturns401() throws Exception {
        mockMvc.perform(post("/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":10,\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedPutCartReturns401() throws Exception {
        mockMvc.perform(put("/v1/cart/items/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedDeleteCartItemReturns401() throws Exception {
        mockMvc.perform(delete("/v1/cart/items/10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedDeleteCartReturns401() throws Exception {
        mockMvc.perform(delete("/v1/cart"))
                .andExpect(status().isUnauthorized());
    }
}
