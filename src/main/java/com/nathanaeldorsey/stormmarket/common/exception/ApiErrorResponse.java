package com.nathanaeldorsey.stormmarket.common.exception;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standard error response body returned by the global exception handler.
 *
 * Contains timestamp, HTTP status code, error reason phrase, message, request path,
 * and optional validation field errors (field name -> error message). Validation error
 * maps are unmodifiable to prevent caller mutation.
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors) {

    public ApiErrorResponse {
        if (validationErrors != null) {
            validationErrors = Collections.unmodifiableMap(new LinkedHashMap<>(validationErrors));
        }
    }

    public Map<String, String> validationErrors() {
        return validationErrors == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(validationErrors));
    }
}
