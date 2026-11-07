package com.nathanaeldorsey.stormmarket.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Request DTO for creating or updating a product. Defines the fields a client may send.
 * The id is omitted because the database assigns it. Jakarta validation annotations
 * enforce required fields, size limits, and positive numeric values.
 *
 * categoryIds holds the ids of categories to associate with the product.
 */
public class ProductRequest {
    // id left out as database provides it
    // Input-Validation rules provided by Jakarta bean validation
    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    private Integer stockQuantity;

    @Size(max = 500)
    private String imageUrl;
    // For category relationship
    private Set<@NotNull @Positive Long> categoryIds;

    // For import: active status (defaults to null - preserve existing if not provided)
    private Boolean active;
}
