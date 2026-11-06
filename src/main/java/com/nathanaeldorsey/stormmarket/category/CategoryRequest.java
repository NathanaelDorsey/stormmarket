package com.nathanaeldorsey.stormmarket.category;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Request DTO for creating or updating a category. Requires a non-blank name.
 * productIds holds the ids of products to associate with the category.
 */
public class CategoryRequest {
    @NotBlank
    private String name;
    // For product relationship
    private Set<Long> productIds;
}
