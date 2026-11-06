package com.nathanaeldorsey.stormmarket.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to update a cart line item quantity. Quantity is required and must be positive.
 */
@Getter
@Setter
public class UpdateCartItemRequest {
    @NotNull
    @Positive
    private Integer quantity;
}
