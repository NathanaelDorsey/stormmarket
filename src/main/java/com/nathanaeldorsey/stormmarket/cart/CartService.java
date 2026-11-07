package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.product.Coupon;
import com.nathanaeldorsey.stormmarket.product.CouponService;
import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
/**
 * Manages shopping-cart operations for users: adding products, updating quantities,
 * removing items, clearing carts, and retrieving cart contents with totals.
 *
 * Enforces business rules:
 * - quantity must be positive and not exceed {@value #MAX_CART_ITEM_QUANTITY} per line item
 * - a cart may hold at most {@value #MAX_DISTINCT_CART_ITEMS} different products
 * - requested quantity cannot exceed available stock
 * - inactive products cannot be added to a cart
 * - an optional coupon code may be applied to reduce the cart total
 */
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private final CouponService couponService;
    private static final int MAX_CART_ITEM_QUANTITY = 100;
    private static final int MAX_DISTINCT_CART_ITEMS = 50;
    private static final String PRODUCT_NOT_FOUND = "Product not found";

    /**
     * Adds the given quantity of a product to the user's cart, or increases the quantity
     * if the product is already present.
     *
     * @param userId   the id of the user whose cart receives the item
     * @param productId the id of the product to add
     * @param quantity  quantity to add; must be positive, at most 100, and within available stock
     * @throws ResourceNotFoundException if the user or product does not exist
     * @throws IllegalArgumentException if the product is inactive, out of stock, quantity is invalid,
     *                                  or the cart already holds 50 distinct products
     */
    @Transactional // groups database changes into 1 transaction
    public void addProductToCart(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);
        User user = getUser(userId);

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));
        if (product.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Product is out of stock");
        }
        if (!product.isActive()) {
            throw new IllegalArgumentException("Product is inactive");
        }
        Cart cart = getOrCreateCart(user);
        // check if item in cart
        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);
        if (item == null && cart.getItems().size() >= MAX_DISTINCT_CART_ITEMS) {
            throw new IllegalArgumentException(
                    "Cart cannot contain more than " + MAX_DISTINCT_CART_ITEMS + " different products");
        }
        int currentQuantity = (item == null ? 0 : item.getQuantity());
        // check if quantity added is more than stock
        if (currentQuantity + quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
        // update quantity if item is found in cart
        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // else add item to cart
            item = new CartItem();
            item.setProduct(product);
            item.setCart(cart);
            item.setQuantity(quantity);
            cart.addItem(item); // keep cart and items synchronized
        }

        cartRepository.save(cart);
    }

    /**
     * Retrieves the user's cart, creating an empty cart if none exists yet.
     * If a coupon code is applied, the discount amount is computed and subtracted
     * from the cart total.
     *
     * @param userId the id of the user whose cart to retrieve
     * @return the cart response containing items, total, coupon code, and discount
     * @throws ResourceNotFoundException if the user does not exist or is inactive
     */
    @Transactional
    public CartResponse getCart(Long userId) {
        User user = getUser(userId);
        // create cart if none to view empty cart
        Cart cart = getOrCreateCart(user);
        CartResponse response = cartMapper.toResponse(cart);

        // Apply coupon discount if a coupon code is present
        if (cart.getCouponCode() != null && !cart.getCouponCode().isBlank()) {
            Coupon coupon = couponService.validate(cart.getCouponCode());
            BigDecimal subtotal = response.getItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
            response.setCouponCode(cart.getCouponCode());
            response.setDiscountAmount(discount);
            response.setTotal(subtotal.subtract(discount));
        }

        return response;
    }

    /**
     * Removes all items from the user's cart.
     *
     * @param userId the id of the user whose cart to clear
     * @throws ResourceNotFoundException if the user's cart does not exist
     */
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getExistingCart(userId);

        cart.getItems().clear();
    }

    /**
     * Applies a coupon code to the user's cart. Validates the coupon code and stores
     * it on the cart so the discount is applied when the cart is retrieved or checked out.
     *
     * @param userId   the id of the user whose cart to modify
     * @param code     the coupon code to apply
     * @return the updated cart response with discount applied
     * @throws ResourceNotFoundException if the user's cart does not exist or the coupon is not found
     * @throws IllegalStateException if the coupon is expired or usage limit reached
     */
    @Transactional
    public CartResponse applyCoupon(Long userId, String code) {
        Coupon coupon = couponService.validate(code);
        Cart cart = getExistingCart(userId);
        cart.setCouponCode(code);
        cartRepository.save(cart);
        return getCart(userId);
    }

    /**
     * Removes the applied coupon code from the user's cart.
     *
     * @param userId the id of the user whose cart to modify
     * @return the updated cart response without discount
     * @throws ResourceNotFoundException if the user's cart does not exist
     */
    @Transactional
    public CartResponse removeCoupon(Long userId) {
        Cart cart = getExistingCart(userId);
        cart.setCouponCode(null);
        cartRepository.save(cart);
        return getCart(userId);
    }

    /**
     * Removes a single product from the user's cart.
     *
     * @param userId   the id of the user whose cart to modify
     * @param productId the id of the product to remove
     * @throws ResourceNotFoundException if the user's cart or the product in the cart does not exist
     */
    @Transactional
    public void removeProductFromCart(Long userId, Long productId) {
        Cart cart = getExistingCart(userId);

        CartItem item = getCartItem(cart, productId);

        cart.getItems().remove(item); // remove item from collection and database(orphanRemoval = true)
    }

    /**
     * Replaces the quantity of a product already in the user's cart.
     *
     * @param userId   the id of the user whose cart to modify
     * @param productId the id of the product to update
     * @param quantity  new quantity; must be positive, at most 100, and within available stock
     * @throws ResourceNotFoundException if the user's cart or the product in the cart does not exist
     * @throws IllegalArgumentException if the product is inactive, quantity is invalid, or exceeds stock
     */
    @Transactional
    public void updateProductQuantity(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);
        Cart cart = getExistingCart(userId);

        CartItem item = getCartItem(cart, productId);

        Product product = item.getProduct();
        if (!product.isActive()) {
            throw new IllegalArgumentException("Product is inactive");
        }
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
        item.setQuantity(quantity);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    private Cart getExistingCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > MAX_CART_ITEM_QUANTITY) {
            throw new IllegalArgumentException("Quantity cannot exceed " + MAX_CART_ITEM_QUANTITY);
        }
    }

    private User getUser(Long userId) {
        return userRepository
                .findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("User not found or inactive"));
    }

    private CartItem getCartItem(Cart cart, Long productId) {
        return cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));
    }
}
