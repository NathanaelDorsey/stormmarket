package com.nathanaeldorsey.stormmarket.product;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Request DTO for product search and filtering: name, price range, category, active/in-stock flags.
 * All fields are optional; null fields are ignored by the specification builder.
 */
public class ProductSearchRequest {

    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long categoryId;
    private String categoryName;
    private Boolean inStock;
    private Boolean active;
}
