package com.nathanaeldorsey.stormmarket.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to add a product to a cart: product id and quantity. Both fields are required
 * and quantity must be positive.
 */
@Getter
@Setter
public class AddToCartRequest {
    @NotNull
    @Positive
    private Long productId;

    @NotNull
    @Positive
    private Integer quantity;
}
