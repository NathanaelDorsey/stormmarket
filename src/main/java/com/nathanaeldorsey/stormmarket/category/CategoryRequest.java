package com.nathanaeldorsey.stormmarket.category;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CategoryRequest {
    @NotBlank
    private String name;
    // For product relationship
    private Set<Long> productIds;

}
