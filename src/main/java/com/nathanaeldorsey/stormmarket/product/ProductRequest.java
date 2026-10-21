package com.nathanaeldorsey.stormmarket.product;

import jakarta.validation.constraints.*;
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
}
