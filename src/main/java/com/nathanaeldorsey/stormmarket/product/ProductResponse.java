package com.nathanaeldorsey.stormmarket.product;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Response DTO for a product: id, name, description, price, stock quantity, image URL,
 * category ids, and active state.
 */
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Set<Long> categoryIds;
    private boolean active;
}
