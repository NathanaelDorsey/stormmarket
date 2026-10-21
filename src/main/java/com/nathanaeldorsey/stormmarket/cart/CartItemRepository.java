package com.nathanaeldorsey.stormmarket.cart;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteByProductId(Long productId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
