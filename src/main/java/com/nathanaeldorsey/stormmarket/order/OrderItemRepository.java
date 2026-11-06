package com.nathanaeldorsey.stormmarket.order;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for order item entities. Provides standard CRUD plus a check for whether
 * any order item references a given product (used to prevent deleting products in order history).
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Checks whether any order item references the given product.
     *
     * @param productId the product id to check
     * @return true if at least one order item uses this product
     */
    boolean existsByProductId(Long productId);
}
