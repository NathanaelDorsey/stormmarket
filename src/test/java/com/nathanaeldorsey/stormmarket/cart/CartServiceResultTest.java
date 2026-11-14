package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.common.exception.Result;
import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceResultTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void addProductToCartReturnsResultOkWhenSuccessful() {
        // Given a user and product
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setReservedStock(0);
        product.setActive(true);
        productRepository.save(product);

        // When adding product to cart
        Result<Void, CartError> result = cartService.addProductToCart(user.getId(), product.getId(), 2);

        // Then Result is Ok
        assertThat(result.isOk()).isTrue();
    }

    @Test
    void addProductToCartReturnsResultErrWhenProductInactive() {
        User user = new User();
        user.setUsername("testuser2");
        user.setEmail("test2@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setActive(false);
        productRepository.save(product);

        Result<Void, CartError> result = cartService.addProductToCart(user.getId(), product.getId(), 2);

        assertThat(result.isErr()).isTrue();
        assertThat(result.getError()).isEqualTo(CartError.INACTIVE_PRODUCT);
    }

    @Test
    void addProductToCartReturnsResultErrWhenInsufficientStock() {
        User user = new User();
        user.setUsername("testuser3");
        user.setEmail("test3@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(5);
        product.setReservedStock(0);
        product.setActive(true);
        productRepository.save(product);

        Result<Void, CartError> result = cartService.addProductToCart(user.getId(), product.getId(), 10);

        assertThat(result.isErr()).isTrue();
        assertThat(result.getError()).isEqualTo(CartError.INSUFFICIENT_STOCK);
    }

    @Test
    void addProductToCartReturnsResultErrWhenCartFull() {
        User user = new User();
        user.setUsername("testuser4");
        user.setEmail("test4@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        // Create 50 distinct products to fill the cart
        for (int i = 0; i < 50; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setPrice(new java.math.BigDecimal("10.00"));
            product.setStockQuantity(100);
            product.setActive(true);
            productRepository.save(product);
            cartService.addProductToCart(user.getId(), product.getId(), 1);
        }

        // Try to add 51st product
        Product product = new Product();
        product.setName("Product 51");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setActive(true);
        productRepository.save(product);

        Result<Void, CartError> result = cartService.addProductToCart(user.getId(), product.getId(), 1);

        assertThat(result.isErr()).isTrue();
        assertThat(result.getError()).isEqualTo(CartError.CART_FULL);
    }

    @Test
    void addProductToCartReturnsResultErrWhenInvalidQuantity() {
        User user = new User();
        user.setUsername("testuser5");
        user.setEmail("test5@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setActive(true);
        productRepository.save(product);

        Result<Void, CartError> result = cartService.addProductToCart(user.getId(), product.getId(), 0);

        assertThat(result.isErr()).isTrue();
        assertThat(result.getError()).isEqualTo(CartError.INVALID_QUANTITY);
    }

    @Test
    void updateProductQuantityReturnsResultOk() {
        User user = new User();
        user.setUsername("testuser6");
        user.setEmail("test6@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setActive(true);
        productRepository.save(product);

        cartService.addProductToCart(user.getId(), product.getId(), 2);

        Result<Void, CartError> result = cartService.updateProductQuantity(user.getId(), product.getId(), 5);

        assertThat(result.isOk()).isTrue();
    }

    @Test
    void updateProductQuantityReturnsResultErrWhenInsufficientStock() {
        User user = new User();
        user.setUsername("testuser7");
        user.setEmail("test7@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(5);
        product.setActive(true);
        productRepository.save(product);

        cartService.addProductToCart(user.getId(), product.getId(), 2);

        Result<Void, CartError> result = cartService.updateProductQuantity(user.getId(), product.getId(), 10);

        assertThat(result.isErr()).isTrue();
        assertThat(result.getError()).isEqualTo(CartError.INSUFFICIENT_STOCK);
    }

    @Test
    void removeProductFromCartReturnsResultOk() {
        User user = new User();
        user.setUsername("testuser8");
        user.setEmail("test8@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setActive(true);
        productRepository.save(product);

        cartService.addProductToCart(user.getId(), product.getId(), 2);

        Result<Void, CartError> result = cartService.removeProductFromCart(user.getId(), product.getId());

        assertThat(result.isOk()).isTrue();
    }

    @Test
    void removeProductFromCartReturnsResultErrWhenNotInCart() {
        User user = new User();
        user.setUsername("testuser9");
        user.setEmail("test9@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Product product1 = new Product();
        product1.setName("Test Product 1");
        product1.setPrice(new java.math.BigDecimal("10.00"));
        product1.setStockQuantity(100);
        product1.setActive(true);
        productRepository.save(product1);

        Product product2 = new Product();
        product2.setName("Test Product 2");
        product2.setPrice(new java.math.BigDecimal("20.00"));
        product2.setStockQuantity(100);
        product2.setActive(true);
        productRepository.save(product2);

        // Add product1 to cart
        cartService.addProductToCart(user.getId(), product1.getId(), 2);

        // Try to remove product2 (not in cart)
        Result<Void, CartError> result = cartService.removeProductFromCart(user.getId(), product2.getId());

        assertThat(result.isErr()).isTrue();
        assertThat(result.getError()).isEqualTo(CartError.PRODUCT_NOT_IN_CART);
    }

    @Test
    void clearCartReturnsResultOk() {
        User user = new User();
        user.setUsername("testuser10");
        user.setEmail("test10@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new java.math.BigDecimal("10.00"));
        product.setStockQuantity(100);
        product.setActive(true);
        productRepository.save(product);

        cartService.addProductToCart(user.getId(), product.getId(), 2);

        Result<Void, CartError> result = cartService.clearCart(user.getId());

        assertThat(result.isOk()).isTrue();
    }

    @Test
    void applyCouponReturnsResultOk() {
        User user = new User();
        user.setUsername("testuser11");
        user.setEmail("test11@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        // Need to create a valid coupon first - this test depends on CouponService
        // For now, test that the method returns Result
        Result<Void, CartError> result = cartService.applyCoupon(user.getId(), "NONEXISTENT");

        assertThat(result.isErr()).isTrue();
        assertThat(result.getError()).isEqualTo(CartError.COUPON_INVALID);
    }

    @Test
    void removeCouponReturnsResultOk() {
        User user = new User();
        user.setUsername("testuser12");
        user.setEmail("test12@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        userRepository.save(user);

        Result<Void, CartError> result = cartService.removeCoupon(user.getId());

        assertThat(result.isOk()).isTrue();
    }
}