package com.nathanaeldorsey.stormmarket.product;

/**
 * A single row-level error encountered during a bulk product import.
 */
public record BulkImportError(int rowNumber, String message) {
}
