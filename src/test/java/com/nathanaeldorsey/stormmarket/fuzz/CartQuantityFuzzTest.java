package com.nathanaeldorsey.stormmarket.fuzz;

/**
 * Import guide:
 * - net.jqwik.api.Property + net.jqwik.api.ForAll + net.jqwik.api.constraints.IntRange
 *   jqwik property-based testing: generates bounded random integers (@IntRange) to verify
 *   quantity validation without unbounded @Positive values that caused OOM before.
 * - org.mockito.Mockito.mock / when, org.mockito.ArgumentMatchers.any
 *   Mockito: stub CartService's repository and mapper dependencies so we test quantity
 *   validation logic in isolation without a real database.
 * - org.assertj.core.api.Assertions.assertThatThrownBy
 *   AssertJ: assert that invalid quantities throw IllegalArgumentException with expected message.
 * - com.nathanaeldorsey.stormmarket.cart.* / product.* / user.*
 *   Project classes under test: CartService (validates quantities) and its domain types.
 * - java.math.BigDecimal, java.util.Optional
 *   JDK: product price construction and repository mock stubbing.
 */

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nathanaeldorsey.stormmarket.cart.Cart;
import com.nathanaeldorsey.stormmarket.cart.CartRepository;
import com.nathanaeldorsey.stormmarket.cart.CartItemRepository;
import com.nathanaeldorsey.stormmarket.cart.CartMapper;
import com.nathanaeldorsey.stormmarket.cart.CartService;
import com.nathanaeldorsey.stormmarket.product.CouponService;
import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

class CartQuantityFuzzTest {

    private final CartRepository cartRepository = mock(CartRepository.class);
    private final CartItemRepository cartItemRepository = mock(CartItemRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CartMapper cartMapper = mock(CartMapper.class);
    private final CouponService couponService = mock(CouponService.class);

    private final CartService cartService = new CartService(
            cartRepository, cartItemRepository, productRepository, userRepository, cartMapper, couponService);

    @Property(tries = 100)
    void validQuantitiesBetween1And100AreAccepted(
            @ForAll @IntRange(min = 1, max = 100) int quantity) {
        User user = activeUser(1L);
        Product product = activeProduct(10L, 1000);
        Cart cart = cartWithUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenReturn(cart);

        cartService.addProductToCart(1L, 10L, quantity);

        // Assert the saved item has the correct quantity
        assertEquals(1, cart.getItems().size());
        assertEquals(Integer.valueOf(quantity), cart.getItems().get(0).getQuantity());
    }

    @Property(tries = 100)
    void quantitiesAbove100AreRejected(
            @ForAll @IntRange(min = 101, max = 10000) int quantity) {
        assertThatThrownBy(() -> cartService.updateProductQuantity(1L, 10L, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity cannot exceed 100");
    }

    @Property(tries = 100)
    void quantitiesBelow1AreRejected(
            @ForAll @IntRange(min = -1000, max = 0) int quantity) {
        assertThatThrownBy(() -> cartService.updateProductQuantity(1L, 10L, quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be positive");
    }

    private static User activeUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setActive(true);
        return user;
    }

    private static Product activeProduct(Long id, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(stock);
        product.setActive(true);
        return product;
    }

    private static Cart cartWithUser(User user) {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        return cart;
    }
}
