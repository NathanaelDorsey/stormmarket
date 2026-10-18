package com.nathanaeldorsey.stormmarket.category;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    @NotBlank
    private String name;
    // For product relationship
    private Set<Long> productIds;
}
