package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.common.exception.DomainError;

/**
 * Expected failures for stock operations.
 * All instances are static singletons for zero-allocation hot paths.
 */
public class StockError extends DomainError {

    public static final StockError INSUFFICIENT_STOCK = new StockError("Insufficient stock");
    public static final StockError PRODUCT_NOT_FOUND = new StockError("Product not found");
    public static final StockError RESERVATION_FAILED = new StockError("Reservation failed");
    public static final StockError CAPTURE_FAILED = new StockError("Capture failed");
    public static final StockError RELEASE_FAILED = new StockError("Release failed");
    public static final StockError PRODUCT_INACTIVE = new StockError("Product is inactive");

    private StockError(String message) {
        super(message);
    }
}