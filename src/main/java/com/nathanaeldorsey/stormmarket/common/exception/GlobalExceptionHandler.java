package com.nathanaeldorsey.stormmarket.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
/**
 * Centralized exception handler for all REST controllers.
 *
 * Converts domain and framework exceptions into consistent ApiErrorResponse bodies
 * with HTTP status, timestamp, message, request path, and optional validation field errors.
 * Unexpected exceptions are logged server-side but return a generic message to clients
 * (no stack traces or internal details leaked).
 */
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String message, String path) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(ZoneId.of("UTC")), status.value(), status.getReasonPhrase(), message, path, null);

        return ResponseEntity.status(status).body(response);
    }

    // Handle unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {

        logger.error("Unexpected error occurred while handling request: {}", request.getRequestURI(), ex);
        // Log the actual exception on the server
        // while keeping internal details out of the API response.
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> validationErrors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(ZoneId.of("UTC")),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields have invalid values",
                request.getRequestURI(),
                validationErrors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Request body is missing or contains invalid JSON", request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidParameter(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + ex.getName(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request argument", request.getRequestURI());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Concurrent modification detected. Please refresh and try again.",
                request.getRequestURI());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidImageFileException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidImageFile(
            InvalidImageFileException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFile(
            InvalidFileException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiErrorResponse> handleFileStorage(FileStorageException ex, HttpServletRequest request) {
        logger.error("File storage error while handling request: {}", request.getRequestURI(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store or retrieve the uploaded file", request.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, "File exceeds maximum size of 5MB", request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestPart(
            MissingServletRequestPartException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Required request part 'file' is missing", request.getRequestURI());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedException(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", request.getRequestURI());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabledAccount(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Account is disabled", request.getRequestURI());
    }
}
