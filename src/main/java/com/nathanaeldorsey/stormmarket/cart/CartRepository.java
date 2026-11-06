package com.nathanaeldorsey.stormmarket.cart;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for cart entities. Provides lookup of a cart by its owning user id.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Finds the cart owned by the given user.
     *
     * @param userId the id of the user
     * @return the cart, or empty if none exists
     */
    Optional<Cart> findByUserId(Long userId);
}
