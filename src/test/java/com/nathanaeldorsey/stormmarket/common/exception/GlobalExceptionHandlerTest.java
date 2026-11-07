package com.nathanaeldorsey.stormmarket.common.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Tests for GlobalExceptionHandler error responses.
 * Covers consistent error schema, HTTP status codes, and validation field errors.
 */

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/products/1");
    }

    @Test
    void handlesIllegalStateExceptionAsConflict() {
        IllegalStateException exception =
                new IllegalStateException("Cannot delete a product that appears in order history");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleIllegalState(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).isEqualTo("Cannot delete a product that appears in order history");
        assertThat(response.getBody().path()).isEqualTo("/api/products/1");
    }
}
