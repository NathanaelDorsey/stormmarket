package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Thrown when an uploaded file fails validation (empty, too large, wrong type,
 * or unparseable). Converted to a 400 Bad Request by the global exception handler
 * using the exception message, so clients receive a specific, user-facing reason.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
