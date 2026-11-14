package com.nathanaeldorsey.stormmarket.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// Extends JpaRepository to receive common methods for persistence (storing/retrieving objects from database)
/**
 * Repository for product entities. Provides standard CRUD plus specification-based search
 * and stock-modifying queries used during order placement and cancellation.
 * Stock operations return Result for explicit error handling.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>, ProductRepositoryCustom {
}