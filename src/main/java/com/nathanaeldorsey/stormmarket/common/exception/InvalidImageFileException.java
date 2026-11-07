package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Thrown when an uploaded file is not a valid product image (wrong type or too large).
 * Handled by {@link GlobalExceptionHandler} as a 400 Bad Request.
 */
public class InvalidImageFileException extends RuntimeException {

    public InvalidImageFileException(String message) {
        super(message);
    }
}
