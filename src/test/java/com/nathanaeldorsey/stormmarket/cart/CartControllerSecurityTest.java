package com.nathanaeldorsey.stormmarket.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
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

@WebMvcTest(controllers = CartController.class)
@AutoConfigureMockMvc
class CartControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/v1/auth/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/v1/products/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/v1/categories/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/v1/orders").permitAll()
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
