package com.nathanaeldorsey.stormmarket.category;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Response DTO for a category: id, name, and ids of active products in the category.
 */
public class CategoryResponse {

    private Long id;
    private String name;
    private Set<Long> productIds;
}
