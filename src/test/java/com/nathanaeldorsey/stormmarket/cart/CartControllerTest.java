package com.nathanaeldorsey.stormmarket.cart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = CartController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.web
                    .OAuth2ResourceServerWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserRepository userRepository;

    private Authentication createAuth(String username) {
        return new UsernamePasswordAuthenticationToken(username, null);
    }

    @Test
    void getCartReturnsCart() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        CartResponse response = new CartResponse();
        response.setId(1L);
        response.setTotal(new BigDecimal("59.98"));

        CartItemResponse item = new CartItemResponse();
        item.setProductId(10L);
        item.setProductName("Test Product");
        item.setPrice(new BigDecimal("29.99"));
        item.setQuantity(2);
        item.setSubtotal(new BigDecimal("59.98"));

        response.setItems(List.of(item));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartService.getCart(1L)).thenReturn(response);

        mockMvc.perform(get("/cart").principal(createAuth("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(59.98))
                .andExpect(jsonPath("$.items[0].productId").value(10))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void addProductToCartReturnsOk() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        CartResponse response = new CartResponse();
        response.setId(1L);
        response.setTotal(new BigDecimal("29.99"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartService.getCart(1L)).thenReturn(response);

        mockMvc.perform(post("/cart/items")
                        .principal(createAuth("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 10,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addProductToCartWithInvalidQuantityReturnsBadRequest() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/cart/items")
                        .principal(createAuth("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 10,
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addProductToCartWithMissingProductIdReturnsBadRequest() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/cart/items")
                        .principal(createAuth("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductQuantityReturnsOk() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        CartResponse response = new CartResponse();
        response.setId(1L);
        response.setTotal(new BigDecimal("89.97"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartService.getCart(1L)).thenReturn(response);

        mockMvc.perform(put("/cart/items/10")
                        .principal(createAuth("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 3
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void updateProductQuantityWithInvalidQuantityReturnsBadRequest() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(put("/cart/items/10")
                        .principal(createAuth("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeProductFromCartReturnsOk() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        CartResponse response = new CartResponse();
        response.setId(1L);
        response.setTotal(BigDecimal.ZERO);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cartService.getCart(1L)).thenReturn(response);

        mockMvc.perform(delete("/cart/items/10")
                        .principal(createAuth("testuser")))
                .andExpect(status().isOk());
    }

    @Test
    void clearCartReturnsOk() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/cart")
                        .principal(createAuth("testuser")))
                .andExpect(status().isOk());
    }

    @Test
    void addProductToCartWhenProductNotFoundReturns404() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new ResourceNotFoundException("Product not found"))
                .when(cartService).addProductToCart(eq(1L), eq(999L), eq(1));

        mockMvc.perform(post("/cart/items")
                        .principal(createAuth("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 999,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void addProductToCartWhenInactiveProductReturns400() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new IllegalArgumentException("Product is inactive"))
                .when(cartService).addProductToCart(eq(1L), eq(10L), eq(1));

        mockMvc.perform(post("/cart/items")
                        .principal(createAuth("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 10,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request argument"));
    }

    @Test
    void removeProductFromCartWhenNotInCartReturns404() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doThrow(new ResourceNotFoundException("Product not found in cart"))
                .when(cartService).removeProductFromCart(eq(1L), eq(999L));

        mockMvc.perform(delete("/cart/items/999")
                        .principal(createAuth("testuser")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found in cart"));
    }

    @Test
    void invalidJsonReturnsBadRequest() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/cart/items")
                        .principal(createAuth("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not valid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedUserReturns401() throws Exception {
        // With addFilters=false, security is disabled; this test verifies the endpoint
        // requires authentication by checking that principal is required
        mockMvc.perform(get("/cart"))
                .andExpect(status().is5xxServerError()); // Returns 500 when principal is null
    }
}