package com.nathanaeldorsey.stormmarket.common.exception;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
