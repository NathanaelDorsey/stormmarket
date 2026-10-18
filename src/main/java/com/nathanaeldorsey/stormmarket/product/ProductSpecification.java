package com.nathanaeldorsey.stormmarket.product;

import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {
    // Describes how to filter products before database returns results
    private ProductSpecification() {}

    public static Specification<Product> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            // make name filter optional
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Product> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            // make price filter optional
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            }
            if (minPrice == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            }
            if (maxPrice == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            }
            return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
        };
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            // Category join can create duplicate rows
            query.distinct(true); // return unique products only
            // make category id filter optional
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.join("categories").get("id"), categoryId);
        };
    }

    public static Specification<Product> inStock(Boolean inStock) {
        return (root, query, criteriaBuilder) -> {
            // make stock filter optional
            if (inStock == null) {
                return criteriaBuilder.conjunction();
            }
            if (inStock) {
                return criteriaBuilder.greaterThan(root.get("stockQuantity"), 0);
            }
            return criteriaBuilder.equal(root.get("stockQuantity"), 0);
        };
    }

    public static Specification<Product> hasCategoryName(String categoryName) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true); // return unique products only
            // make category name filter optional
            if (categoryName == null || categoryName.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.join("categories").get("name")),
                    categoryName.trim().toLowerCase());
        };
    }
}
