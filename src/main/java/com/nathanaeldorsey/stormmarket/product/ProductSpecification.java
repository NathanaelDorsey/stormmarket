package com.nathanaeldorsey.stormmarket.product;

import jakarta.persistence.criteria.Path;
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
            Path<BigDecimal> price = root.get("price");
            if (minPrice == null) {
                return criteriaBuilder.lessThanOrEqualTo(price, maxPrice);
            }
            if (maxPrice == null) {
                return criteriaBuilder.greaterThanOrEqualTo(price, minPrice);
            }
            return criteriaBuilder.between(price, minPrice, maxPrice);
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

    public static Specification<Product> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("is_active"));
    }

    public static Specification<Product> activeStatus(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            // make active status filter optional
            if (active == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
}
