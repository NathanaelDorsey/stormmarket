package com.nathanaeldorsey.stormmarket.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


// Extends JpaRepository to receive common methods for persistence (storing/retrieving objects from database)
public interface ProductRepository extends
        JpaRepository <Product, Long>,
        JpaSpecificationExecutor<Product> {

    // check and deduction atomic preventing double deductions
    @Modifying
    @Query("""
    UPDATE Product p
    SET p.stockQuantity = p.stockQuantity - :quantity
    WHERE p.id = :productId
      AND p.stockQuantity >= :quantity
    """)
    int deductStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity
    );

}
