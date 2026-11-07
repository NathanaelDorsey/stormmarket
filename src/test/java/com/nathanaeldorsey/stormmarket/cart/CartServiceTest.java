package com.nathanaeldorsey.stormmarket.cart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.Role;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
/**
 * Tests for CartService shopping cart business logic.
 * Covers item management, quantity boundaries, stock checks, and totals.
 */

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartMapper cartMapper;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(
                cartRepository, cartItemRepository, productRepository, userRepository, cartMapper);
    }

    @Test
    void getCartReturnsEmptyCartForNewUser() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart emptyCart = new Cart();
        emptyCart.setId(1L);
        emptyCart.setUser(user);

        CartResponse response = new CartResponse();
        response.setId(1L);
        response.setItems(java.util.List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);
        when(cartMapper.toResponse(emptyCart)).thenReturn(response);

        CartResponse result = cartService.getCart(1L);

        assertNotNull(result);
        assertEquals(0, result.getItems().size());
    }

    @Test
    void addProductToCartSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(10);
        product.setActive(true);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addProductToCart(1L, 10L, 2);

        verify(cartItemRepository).findByCartIdAndProductId(1L, 10L);
        verify(cartRepository).save(cart);

        // Assert the saved item has the correct product and quantity
        assertEquals(1, cart.getItems().size());
        assertEquals(product, cart.getItems().get(0).getProduct());
        assertEquals(Integer.valueOf(2), cart.getItems().get(0).getQuantity());
    }

    @Test
    void addProductToCartRejectsNonExistentProduct() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.addProductToCart(1L, 999L, 1));
    }

    @Test
    void addProductToCartRejectsInactiveProduct() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product = new Product();
        product.setId(10L);
        product.setStockQuantity(10);
        product.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> cartService.addProductToCart(1L, 10L, 1));
    }

    @Test
    void addProductToCartRejectsOutOfStockProduct() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product = new Product();
        product.setId(10L);
        product.setStockQuantity(0);
        product.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> cartService.addProductToCart(1L, 10L, 1));
    }

    @Test
    void addProductToCartRejectsQuantityLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addProductToCart(1L, 10L, 0));
    }

    @Test
    void addProductToCartRejectsQuantityExceedingStock() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product = new Product();
        product.setId(10L);
        product.setStockQuantity(5);
        product.setActive(true);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> cartService.addProductToCart(1L, 10L, 10));
    }

    @Test
    void addSameProductTwiceIncreasesQuantity() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(10);
        product.setActive(true);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setQuantity(2);
        existingItem.setProduct(product);
        existingItem.setCart(cart);
        cart.getItems().add(existingItem);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(existingItem));
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.addProductToCart(1L, 10L, 3);

        assertEquals(5, existingItem.getQuantity());
    }

    @Test
    void updateProductQuantitySuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product = new Product();
        product.setId(10L);
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

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(item));

        cartService.updateProductQuantity(1L, 10L, 5);

        assertEquals(5, item.getQuantity());
    }

    @Test
    void updateProductQuantityRejectsInactiveProduct() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product = new Product();
        product.setId(10L);
        product.setStockQuantity(10);
        product.setActive(false);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(2);
        item.setProduct(product);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> cartService.updateProductQuantity(1L, 10L, 5));
    }

    @Test
    void updateProductQuantityRejectsExceedingStock() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product = new Product();
        product.setId(10L);
        product.setStockQuantity(3);
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

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> cartService.updateProductQuantity(1L, 10L, 5));
    }

    @Test
    void removeProductFromCartSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product = new Product();
        product.setId(10L);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(2);
        item.setProduct(product);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(item));

        cartService.removeProductFromCart(1L, 10L);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void removeProductFromCartThrowsWhenProductNotInCart() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeProductFromCart(1L, 999L));
    }

    @Test
    void clearCartRemovesAllItems() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Product product1 = new Product();
        product1.setId(10L);

        Product product2 = new Product();
        product2.setId(20L);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        CartItem item1 = new CartItem();
        item1.setId(1L);
        item1.setQuantity(2);
        item1.setProduct(product1);
        item1.setCart(cart);

        CartItem item2 = new CartItem();
        item2.setId(2L);
        item2.setQuantity(3);
        item2.setProduct(product2);
        item2.setCart(cart);

        cart.getItems().add(item1);
        cart.getItems().add(item2);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.clearCart(1L);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void getCartThrowsForInactiveUser() {
        User user = new User();
        user.setId(1L);
        user.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> cartService.getCart(1L));
    }

    @Test
    void addProductToCartThrowsForInactiveUser() {
        User user = new User();
        user.setId(1L);
        user.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> cartService.addProductToCart(1L, 10L, 1));
    }
}