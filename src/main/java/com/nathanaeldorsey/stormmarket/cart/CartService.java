package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.product.ProductRepository;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private static final int MAX_CART_ITEM_QUANTITY = 100;
    private static final int MAX_DISTINCT_CART_ITEMS = 50;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartMapper = cartMapper;
    }

    @Transactional // groups database changes into 1 transaction
    public void addProductToCart(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);
        User user = getUser(userId);

        Product product =
                productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Product is out of stock");
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
            cart.addItem(item); // keep cart and items synchronized
        }

        cartRepository.save(cart);
    }

    @Transactional
    public CartResponse getCart(Long userId) {
        User user = getUser(userId);
        // create cart if none to view empty cart
        Cart cart = getOrCreateCart(user);
        return cartMapper.toResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getExistingCart(userId);

        cart.getItems().clear();
    }

    @Transactional
    public void removeProductFromCart(Long userId, Long productId) {
        Cart cart = getExistingCart(userId);

        CartItem item = getCartItem(cart, productId);

        cart.getItems().remove(item); // remove item from collection and database(orphanRemoval = true)
    }

    @Transactional
    public void updateProductQuantity(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);
        Cart cart = getExistingCart(userId);

        CartItem item = getCartItem(cart, productId);

        Product product = item.getProduct();

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
        return cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found"));
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
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private CartItem getCartItem(Cart cart, Long productId) {
        return cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));
    }
}
