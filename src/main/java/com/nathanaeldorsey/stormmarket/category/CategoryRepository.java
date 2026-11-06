package com.nathanaeldorsey.stormmarket.category;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for category entities. Provides standard JPA CRUD operations.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {}
