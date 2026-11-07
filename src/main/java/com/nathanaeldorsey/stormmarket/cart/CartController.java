package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart")
/**
 * HTTP entry point for shopping-cart operations. Authenticated users can view their cart,
 * add products, update item quantities, remove items, or clear the cart.
 * All endpoints require a valid JWT bearer token and operate on the caller's own cart.
 */
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;
    private static final String USER_NOT_FOUND = "User not found";

    /**
     * Returns the authenticated user's cart with items and total.
     *
     * @param authentication the current authenticated user's security context
     * @return the cart response
     * @throws ResourceNotFoundException if the user does not exist
     */
    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        return cartService.getCart(user.getId());
    }

    /**
     * Adds a product to the authenticated user's cart and returns the updated cart.
     *
     * @param request         the product id and quantity to add
     * @param authentication  the current authenticated user's security context
     * @return the updated cart response
     * @throws ResourceNotFoundException if the user or product does not exist
     * @throws IllegalArgumentException if the quantity or stock constraints are violated
     */
    @PostMapping("/items")
    public CartResponse addProductToCart(@Valid @RequestBody AddToCartRequest request, Authentication authentication) {
        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        cartService.addProductToCart(user.getId(), request.getProductId(), request.getQuantity());
        return cartService.getCart(user.getId());
    }

    /**
     * Updates the quantity of a product in the authenticated user's cart.
     *
     * @param productId       the id of the product to update
     * @param request         the new quantity
     * @param authentication  the current authenticated user's security context
     * @return the updated cart response
     * @throws ResourceNotFoundException if the user or cart item does not exist
     * @throws IllegalArgumentException if the quantity is invalid or exceeds stock
     */
    @PutMapping("/items/{productId:\\d+}")
    public CartResponse updateProductQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {
        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        cartService.updateProductQuantity(user.getId(), productId, request.getQuantity());

        return cartService.getCart(user.getId());
    }

    /**
     * Removes a product from the authenticated user's cart and returns the updated cart.
     *
     * @param productId       the id of the product to remove
     * @param authentication  the current authenticated user's security context
     * @return the updated cart response
     * @throws ResourceNotFoundException if the user or cart item does not exist
     */
    @DeleteMapping("/items/{productId:\\d+}")
    public CartResponse removeProductFromCart(@PathVariable Long productId, Authentication authentication) {
        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        cartService.removeProductFromCart(user.getId(), productId);

        return cartService.getCart(user.getId());
    }

    /**
     * Removes all items from the authenticated user's cart.
     *
     * @param authentication  the current authenticated user's security context
     * @throws ResourceNotFoundException if the user does not exist
     */
    @DeleteMapping
    public void clearCart(Authentication authentication) {
        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        cartService.clearCart(user.getId());
    }
}
