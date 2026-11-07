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

    /**
     * Handles unexpected exceptions with a generic error response (no internal details leaked).
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return 500 Internal Server Error with a generic message
     */
    // Handle unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {

        logger.error("Unexpected error occurred while handling request: {}", request.getRequestURI(), ex);
        // Log the actual exception on the server
        // while keeping internal details out of the API response.
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI());
    }

    /**
     * Handles bean validation failures with per-field error details.
     *
     * @param ex      the validation exception
     * @param request the HTTP request
     * @return 400 Bad Request with field error map
     */
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

    /**
     * Handles missing resource exceptions.
     *
     * @param ex      the not found exception
     * @param request the HTTP request
     * @return 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles business rule violations (e.g., insufficient stock, invalid order status).
     *
     * @param ex      the illegal state exception
     * @param request the HTTP request
     * @return 409 Conflict
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles malformed or missing JSON request bodies.
     *
     * @param request the HTTP request
     * @return 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Request body is missing or contains invalid JSON", request.getRequestURI());
    }

    /**
     * Handles invalid path variable or query parameter values.
     *
     * @param ex      the type mismatch exception
     * @param request the HTTP request
     * @return 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidParameter(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + ex.getName(), request.getRequestURI());
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param request the HTTP request
     * @return 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request argument", request.getRequestURI());
    }

    /**
     * Handles concurrent modification detected via optimistic locking.
     *
     * @param ex      the locking failure exception
     * @param request the HTTP request
     * @return 409 Conflict
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Concurrent modification detected. Please refresh and try again.",
                request.getRequestURI());
    }

    /**
     * Handles duplicate resource exceptions (e.g., duplicate username or email).
     *
     * @param ex      the already exists exception
     * @param request the HTTP request
     * @return 409 Conflict
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex, HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles invalid image file uploads.
     *
     * @param ex      the invalid image exception
     * @param request the HTTP request
     * @return 400 Bad Request
     */
    @ExceptionHandler(InvalidImageFileException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidImageFile(
            InvalidImageFileException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles invalid file uploads.
     *
     * @param ex      the invalid file exception
     * @param request the HTTP request
     * @return 400 Bad Request
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFile(
            InvalidFileException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Handles file storage failures.
     *
     * @param ex      the file storage exception
     * @param request the HTTP request
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiErrorResponse> handleFileStorage(FileStorageException ex, HttpServletRequest request) {
        logger.error("File storage error while handling request: {}", request.getRequestURI(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store or retrieve the uploaded file", request.getRequestURI());
    }

    /**
     * Handles uploads exceeding the maximum file size (5MB).
     *
     * @param ex      the size exceeded exception
     * @param request the HTTP request
     * @return 400 Bad Request
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, "File exceeds maximum size of 5MB", request.getRequestURI());
    }

    /**
     * Handles missing required request parts (e.g., missing file upload).
     *
     * @param ex      the missing part exception
     * @param request the HTTP request
     * @return 400 Bad Request
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestPart(
            MissingServletRequestPartException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Required request part 'file' is missing", request.getRequestURI());
    }

    /**
     * Handles authorization denials (insufficient role/authority).
     *
     * @param request the HTTP request
     * @return 403 Forbidden
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedException(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI());
    }

    /**
     * Handles invalid login credentials.
     *
     * @param request the HTTP request
     * @return 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", request.getRequestURI());
    }

    /**
     * Handles deactivated user accounts.
     *
     * @param request the HTTP request
     * @return 401 Unauthorized
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabledAccount(HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Account is disabled", request.getRequestURI());
    }
}
