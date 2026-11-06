package com.nathanaeldorsey.stormmarket.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Extends JpaRepository to receive common methods for persistence (storing/retrieving objects from database)
/**
 * Repository for product entities. Provides standard CRUD plus specification-based search
 * and two stock-modifying queries used during order placement and cancellation.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Atomically deducts stock from a product, only if sufficient stock is available.
     * Used during order placement to prevent overselling.
     *
     * @param productId the product whose stock to reduce
     * @param quantity  the quantity to deduct
     * @return number of rows updated (0 if product missing or insufficient stock)
     */
    @Modifying
    @Query("""
    UPDATE Product p
    SET p.stockQuantity = p.stockQuantity - :quantity
    WHERE p.id = :productId
      AND p.stockQuantity >= :quantity
    """)
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Restores (adds back) stock to a product. Used when an order is canceled.
     *
     * @param productId the product whose stock to restore
     * @param quantity  the quantity to add back
     */
    @Modifying
    @Query("""
    UPDATE Product p
    SET p.stockQuantity = p.stockQuantity + :quantity
    WHERE p.id = :productId
    """)
    void restoreStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
