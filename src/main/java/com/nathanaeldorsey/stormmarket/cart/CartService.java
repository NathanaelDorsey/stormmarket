package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.common.exception.Err;
import com.nathanaeldorsey.stormmarket.common.exception.Ok;
import com.nathanaeldorsey.stormmarket.common.exception.Result;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.product.Coupon;
import com.nathanaeldorsey.stormmarket.product.CouponService;
import com.nathanaeldorsey.stormmarket.product.CouponValidationError;
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
     * @return Result.ok(null) on success, Result.err(CartError) on failure
     */
    @Transactional
    public Result<Void, CartError> addProductToCart(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return Result.err(CartError.INVALID_QUANTITY);
        }
        if (quantity > MAX_CART_ITEM_QUANTITY) {
            return Result.err(CartError.INVALID_QUANTITY);
        }

        User user = userRepository
                .findById(userId)
                .filter(User::isActive)
                .orElse(null);
        if (user == null) {
            return Result.err(CartError.USER_NOT_FOUND);
        }

        Product product = productRepository
                .findById(productId)
                .orElse(null);
        if (product == null) {
            return Result.err(CartError.PRODUCT_NOT_FOUND);
        }

        if (product.getStockQuantity() <= 0) {
            return Result.err(CartError.INSUFFICIENT_STOCK);
        }
        if (!product.isActive()) {
            return Result.err(CartError.INACTIVE_PRODUCT);
        }

        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (item == null && cart.getItems().size() >= MAX_DISTINCT_CART_ITEMS) {
            return Result.err(CartError.CART_FULL);
        }

        int currentQuantity = (item == null ? 0 : item.getQuantity());
        if (currentQuantity + quantity > product.getStockQuantity()) {
            return Result.err(CartError.INSUFFICIENT_STOCK);
        }

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = new CartItem();
            item.setProduct(product);
            item.setCart(cart);
            item.setQuantity(quantity);
            cart.addItem(item);
        }

        cartRepository.save(cart);
        return Result.ok(null);
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
        Cart cart = getOrCreateCart(user);
        CartResponse response = cartMapper.toResponse(cart);

        if (cart.getCouponCode() != null && !cart.getCouponCode().isBlank()) {
            Result<Coupon, CouponValidationError> result = couponService.validate(cart.getCouponCode());
            if (result.isOk()) {
                Coupon coupon = ((Ok<Coupon, CouponValidationError>) result).value();
                BigDecimal subtotal = response.getItems().stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
                response.setCouponCode(cart.getCouponCode());
                response.setDiscountAmount(discount);
                response.setTotal(subtotal.subtract(discount));
            } else {
                cart.setCouponCode(null);
                cartRepository.save(cart);
            }
        }

        return response;
    }

    /**
     * Removes all items from the user's cart.
     *
     * @param userId the id of the user whose cart to clear
     * @return Result.ok(null) on success, Result.err(CartError) on failure
     */
    @Transactional
    public Result<Void, CartError> clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return Result.err(CartError.CART_NOT_FOUND);
        }

        cart.getItems().clear();
        return Result.ok(null);
    }

    /**
     * Applies a coupon code to the user's cart. Validates the coupon code and stores
     * it on the cart so the discount is applied when the cart is retrieved or checked out.
     *
     * @param userId   the id of the user whose cart to modify
     * @param code     the coupon code to apply
     * @return Result.ok(null) on success, Result.err(CartError) on failure
     */
    @Transactional
    public Result<Void, CartError> applyCoupon(Long userId, String code) {
        Result<Coupon, CouponValidationError> result = couponService.validate(code);
        if (result.isErr()) {
            return Result.err(mapCouponError(result.getError()));
        }

        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return Result.err(CartError.CART_NOT_FOUND);
        }

        cart.setCouponCode(code);
        cartRepository.save(cart);
        return Result.ok(null);
    }

    private CartError mapCouponError(CouponValidationError error) {
        if (error == CouponValidationError.NOT_FOUND) return CartError.COUPON_INVALID;
        if (error == CouponValidationError.EXPIRED) return CartError.COUPON_EXPIRED;
        if (error == CouponValidationError.USAGE_LIMIT_REACHED) return CartError.COUPON_USAGE_LIMIT_REACHED;
        if (error == CouponValidationError.INACTIVE) return CartError.COUPON_INACTIVE;
        return CartError.COUPON_INVALID;
    }

    /**
     * Removes the applied coupon code from the user's cart.
     *
     * @param userId the id of the user whose cart to modify
     * @return Result.ok(null) on success, Result.err(CartError) on failure
     */
    @Transactional
    public Result<Void, CartError> removeCoupon(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            // Idempotent: no cart means no coupon to remove
            return Result.ok(null);
        }

        cart.setCouponCode(null);
        cartRepository.save(cart);
        return Result.ok(null);
    }

    /**
     * Removes a single product from the user's cart.
     *
     * @param userId   the id of the user whose cart to modify
     * @param productId the id of the product to remove
     * @return Result.ok(null) on success, Result.err(CartError) on failure
     */
    @Transactional
    public Result<Void, CartError> removeProductFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return Result.err(CartError.CART_NOT_FOUND);
        }

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);
        if (item == null) {
            return Result.err(CartError.PRODUCT_NOT_IN_CART);
        }

        cart.getItems().remove(item);
        return Result.ok(null);
    }

    /**
     * Replaces the quantity of a product already in the user's cart.
     *
     * @param userId   the id of the user whose cart to modify
     * @param productId the id of the product to update
     * @param quantity  new quantity; must be positive, at most 100, and within available stock
     * @return Result.ok(null) on success, Result.err(CartError) on failure
     */
    @Transactional
    public Result<Void, CartError> updateProductQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return Result.err(CartError.INVALID_QUANTITY);
        }
        if (quantity > MAX_CART_ITEM_QUANTITY) {
            return Result.err(CartError.INVALID_QUANTITY);
        }

        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return Result.err(CartError.CART_NOT_FOUND);
        }

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);
        if (item == null) {
            return Result.err(CartError.PRODUCT_NOT_IN_CART);
        }

        Product product = item.getProduct();
        if (!product.isActive()) {
            return Result.err(CartError.INACTIVE_PRODUCT);
        }
        if (quantity > product.getStockQuantity()) {
            return Result.err(CartError.INSUFFICIENT_STOCK);
        }

        item.setQuantity(quantity);
        return Result.ok(null);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    private User getUser(Long userId) {
        return userRepository
                .findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("User not found or inactive"));
    }
}