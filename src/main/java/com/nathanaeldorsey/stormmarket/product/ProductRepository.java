package com.nathanaeldorsey.stormmarket.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


// Extends JpaRepository to receive common methods for persistence (storing/retrieving objects from database)
public interface ProductRepository extends
        JpaRepository <Product, Long>,
        JpaSpecificationExecutor<Product> {
}
