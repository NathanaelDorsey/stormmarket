package com.nathanaeldorsey.stormmarket.cart;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for cart item entities.
 * Provides standard CRUD operations plus lookup by cart and product ID.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteByProductId(Long productId);

    /**
     * Finds a cart item by cart ID and product ID.
     *
     * @param cartId the cart ID
     * @param productId the product ID
     * @return the cart item if found, empty otherwise
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
