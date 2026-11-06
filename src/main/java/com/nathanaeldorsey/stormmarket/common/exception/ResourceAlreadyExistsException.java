package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Thrown when a create/update would violate uniqueness (duplicate username or email).
 * Mapped to HTTP 409 by the global exception handler.
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
