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

    /**
     * Reserves stock for a product (increments reservedStock) atomically.
     * Used when an order is placed to hold stock until payment is captured.
     * Only succeeds if sufficient stock is available (stockQuantity >= reservedStock + quantity).
     *
     * @param productId the product whose stock to reserve
     * @param quantity  the quantity to reserve
     * @return number of rows updated (0 if product missing or insufficient stock)
     */
    @Modifying
    @Query("""
    UPDATE Product p
    SET p.reservedStock = p.reservedStock + :quantity
    WHERE p.id = :productId
      AND p.stockQuantity >= p.reservedStock + :quantity
    """)
    int reserveStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Releases reserved stock for a product (decrements reservedStock).
     * Used when an order is cancelled or expires before payment capture.
     *
     * @param productId the product whose reserved stock to release
     * @param quantity  the quantity to release
     */
    @Modifying
    @Query("""
    UPDATE Product p
    SET p.reservedStock = p.reservedStock - :quantity
    WHERE p.id = :productId
    """)
    void releaseReservedStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Transfers reserved stock to actual stock deduction.
     * Called when payment is captured: deducts from stockQuantity and decrements reservedStock.
     *
     * @param productId the product whose stock to capture
     * @param quantity  the quantity to capture
     * @return number of rows updated (0 if product missing or insufficient stock)
     */
    @Modifying
    @Query("""
    UPDATE Product p
    SET p.stockQuantity = p.stockQuantity - :quantity,
        p.reservedStock = p.reservedStock - :quantity
    WHERE p.id = :productId
      AND p.stockQuantity >= :quantity
      AND p.reservedStock >= :quantity
    """)
    int captureStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
