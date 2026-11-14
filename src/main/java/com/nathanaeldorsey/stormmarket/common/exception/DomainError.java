package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Base class for all domain-level expected errors.
 * Extends RuntimeException to satisfy Result<E extends Throwable> bound.
 * All domain errors (StockError, CartError, OrderError, PaymentError) extend this.
 */
public abstract class DomainError extends RuntimeException {

    protected DomainError(String message) {
        super(message);
    }
}