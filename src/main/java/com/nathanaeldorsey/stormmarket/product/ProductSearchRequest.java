package com.nathanaeldorsey.stormmarket.product;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchRequest {

    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long categoryId;
    private String categoryName;
    private Boolean inStock;
    private Boolean active;
}
