package com.nathanaeldorsey.stormmarket.common.exception;

/**
 * Thrown when a file cannot be stored or deleted by the {@link
 * com.nathanaeldorsey.stormmarket.file.FileStorageService}.
 * Handled by {@link GlobalExceptionHandler} as a 500 Internal Server Error.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
