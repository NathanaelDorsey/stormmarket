package com.nathanaeldorsey.stormmarket.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
// DTO (Data-Transfer-Object) - defines what the client is allowed to send to the API/Application
public class ProductRequest {
    // Input-Validation rules provided by jakarta bean validation
    @NotBlank
    private String name;
    private String description;
    @Positive
    private BigDecimal price;
    @PositiveOrZero
    private Integer stockQuantity;
    private String imageUrl;
    // For category relationship
    private Set<Long> categoryIds;

}
