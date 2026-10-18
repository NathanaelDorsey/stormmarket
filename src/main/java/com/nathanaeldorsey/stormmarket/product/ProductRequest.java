package com.nathanaeldorsey.stormmarket.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// DTO (Data-Transfer-Object) - defines what the client is allowed to send to the API/Application
public class ProductRequest {
    // id left out as database provides it
    // Input-Validation rules provided by Jakarta bean validation
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
