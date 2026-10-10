package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(
            CartService cartService,
            UserRepository userRepository){
        this.cartService=cartService;
        this.userRepository=userRepository;
    }

    @GetMapping
    public CartResponse getCart(Authentication authentication){
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cartService.getCart(user.getId());
    }

    @PostMapping("/items")
    public CartResponse addProductToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication){
        User user = userRepository.findByUsername(authentication.getName()).
                orElseThrow(() -> new RuntimeException("User not found"));
        cartService.addProductToCart(
                user.getId(),
                request.getProductId(),
                request.getQuantity()
        );
        return cartService.getCart(user.getId());
    }

    @PutMapping("/items/{productId:\\d+}")
    public CartResponse updateProductQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        cartService.updateProductQuantity(
                user.getId(),
                productId,
                request.getQuantity()
        );

        return cartService.getCart(user.getId());
    }

    @DeleteMapping("/items/{productId:\\d+}")
    public CartResponse removeProductFromCart(
            @PathVariable Long productId,
            Authentication authentication){
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        cartService.removeProductFromCart(user.getId(), productId);

        return cartService.getCart(user.getId());
    }

    @DeleteMapping
    public void clearCart(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        cartService.clearCart(user.getId());
    }

}
