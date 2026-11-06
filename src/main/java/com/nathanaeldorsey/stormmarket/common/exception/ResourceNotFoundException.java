package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Thrown when a requested entity (user, product, cart, category, order) is not found.
 * Mapped to HTTP 404 by the global exception handler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
