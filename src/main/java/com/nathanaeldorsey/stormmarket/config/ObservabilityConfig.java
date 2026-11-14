package com.nathanaeldorsey.stormmarket.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Observability configuration for structured logging, metrics, and distributed tracing.
 *
 * Provides:
 * - MDC filter for request correlation (requestId, traceId, spanId)
 * - Custom business metrics
 * - OpenTelemetry tracing configuration
 */
@Configuration
public class ObservabilityConfig {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String SPAN_ID_MDC_KEY = "spanId";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String USER_ROLE_MDC_KEY = "userRole";

    private static final int MAX_REQUEST_ID_LENGTH = 64;
    private static final String REQUEST_ID_PATTERN = "[a-zA-Z0-9\\-]+";

    /**
     * Filter to populate MDC with request correlation IDs.
     * Runs early in the filter chain to ensure all subsequent logs include correlation data.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public OncePerRequestFilter mdcFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {
                try {
                    // Generate or extract request ID (validated to prevent header injection)
                    String requestId = request.getHeader(REQUEST_ID_HEADER);
                    if (requestId == null || requestId.isBlank() || !isValidRequestId(requestId)) {
                        requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                    }
                    MDC.put(REQUEST_ID_MDC_KEY, requestId);
                    // Safe to set header as requestId is validated/generated
                    response.setHeader(REQUEST_ID_HEADER, requestId);

                    // Extract trace/span IDs from tracing headers (W3C trace-context or B3)
                    String traceParent = request.getHeader("traceparent");
                    if (traceParent != null && !traceParent.isBlank()) {
                        // W3C format: version-trace-id-span-id-flags
                        String[] parts = traceParent.split("-");
                        if (parts.length >= 3) {
                            MDC.put(TRACE_ID_MDC_KEY, parts[1]);
                            MDC.put(SPAN_ID_MDC_KEY, parts[2]);
                        }
                    } else {
                        // B3 format
                        String b3TraceId = request.getHeader("X-B3-TraceId");
                        String b3SpanId = request.getHeader("X-B3-SpanId");
                        if (b3TraceId != null) MDC.put(TRACE_ID_MDC_KEY, b3TraceId);
                        if (b3SpanId != null) MDC.put(SPAN_ID_MDC_KEY, b3SpanId);
                    }

                    // Extract user info from security context if authenticated
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.isAuthenticated()) {
                        String username = authentication.getName();
                        if (username != null) {
                            MDC.put(USER_ID_MDC_KEY, username);
                        }

                        List<String> roles = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .filter(StringUtils::hasText)
                                .map(auth -> auth.replace("ROLE_", ""))
                                .collect(Collectors.toList());
                        if (!roles.isEmpty()) {
                            MDC.put(USER_ROLE_MDC_KEY, String.join(",", roles));
                        }
                    }

                    filterChain.doFilter(request, response);
                } finally {
                    // Always clear MDC to prevent leakage between requests
                    MDC.clear();
                }
            }

            private boolean isValidRequestId(String requestId) {
                return requestId.length() <= MAX_REQUEST_ID_LENGTH
                        && requestId.matches(REQUEST_ID_PATTERN);
            }
        };
    }

    /**
     * Custom business metrics for Storm Market.
     */
    @Bean
    public StormMarketMetrics stormMarketMetrics(MeterRegistry registry) {
        return new StormMarketMetrics(registry);
    }

    /**
     * Custom observation registry configuration.
     */
    @Bean
    public ObservationRegistry observationRegistry() {
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        return observationRegistry;
    }

    /**
     * Business metrics for Storm Market domain.
     */
    public static class StormMarketMetrics {
        private final io.micrometer.core.instrument.Counter ordersCreated;
        private final io.micrometer.core.instrument.Counter ordersFailed;
        private final io.micrometer.core.instrument.Counter productsCreated;
        private final io.micrometer.core.instrument.Counter productsUpdated;
        private final io.micrometer.core.instrument.Counter usersRegistered;
        private final io.micrometer.core.instrument.Counter authFailures;
        private final io.micrometer.core.instrument.Timer orderProcessingTime;
        private final io.micrometer.core.instrument.Gauge activeUsers;

        public StormMarketMetrics(MeterRegistry registry) {
            this.ordersCreated = io.micrometer.core.instrument.Counter.builder("stormmarket.orders.created")
                    .description("Total number of orders created")
                    .register(registry);

            this.ordersFailed = io.micrometer.core.instrument.Counter.builder("stormmarket.orders.failed")
                    .description("Total number of failed order attempts")
                    .register(registry);

            this.productsCreated = io.micrometer.core.instrument.Counter.builder("stormmarket.products.created")
                    .description("Total number of products created")
                    .register(registry);

            this.productsUpdated = io.micrometer.core.instrument.Counter.builder("stormmarket.products.updated")
                    .description("Total number of products updated")
                    .register(registry);

            this.usersRegistered = io.micrometer.core.instrument.Counter.builder("stormmarket.users.registered")
                    .description("Total number of users registered")
                    .register(registry);

            this.authFailures = io.micrometer.core.instrument.Counter.builder("stormmarket.auth.failures")
                    .description("Total number of authentication failures")
                    .register(registry);

            this.orderProcessingTime = io.micrometer.core.instrument.Timer.builder("stormmarket.orders.processing.time")
                    .description("Order processing latency")
                    .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                    .register(registry);

            this.activeUsers = io.micrometer.core.instrument.Gauge.builder("stormmarket.users.active",
                    new java.util.concurrent.atomic.AtomicLong(0), java.util.concurrent.atomic.AtomicLong::get)
                    .description("Current number of active users")
                    .register(registry);
        }

        public void incrementOrdersCreated() { ordersCreated.increment(); }
        public void incrementOrdersFailed() { ordersFailed.increment(); }
        public void incrementProductsCreated() { productsCreated.increment(); }
        public void incrementProductsUpdated() { productsUpdated.increment(); }
        public void incrementUsersRegistered() { usersRegistered.increment(); }
        public void incrementAuthFailures() { authFailures.increment(); }
        public io.micrometer.core.instrument.Timer.Sample startOrderProcessingTimer() {
            return io.micrometer.core.instrument.Timer.start();
        }
        public void recordOrderProcessingTime(io.micrometer.core.instrument.Timer.Sample sample) {
            sample.stop(orderProcessingTime);
        }
        public void setActiveUsers(long count) {
            // Gauge reads from the AtomicLong directly
        }
    }
}