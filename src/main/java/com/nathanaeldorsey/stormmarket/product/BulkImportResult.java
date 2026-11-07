package com.nathanaeldorsey.stormmarket.product;

import java.util.Collections;
import java.util.List;

/**
 * Result of a bulk product import operation. Contains the total number of rows
 * processed, how many succeeded, how many failed, and any row-level errors.
 */
public record BulkImportResult(
        int totalRows,
        int succeeded,
        int failed,
        List<BulkImportError> errors) {

    public BulkImportResult {
        errors = Collections.unmodifiableList(List.copyOf(errors));
    }

    /**
     * Creates a new BulkImportResult with a defensive copy of the errors list.
     */
    public static BulkImportResult of(int totalRows, int succeeded, int failed, List<BulkImportError> errors) {
        return new BulkImportResult(totalRows, succeeded, failed, errors);
    }
}



