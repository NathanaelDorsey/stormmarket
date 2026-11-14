package com.nathanaeldorsey.stormmarket.config;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration providing a minimal StormMarketMetrics bean for @WebMvcTest slices.
 */
@TestConfiguration
public class TestObservabilityConfig {

    @Bean
    @Primary
    ObservabilityConfig.StormMarketMetrics stormMarketMetrics() {
        return new ObservabilityConfig.StormMarketMetrics(new SimpleMeterRegistry());
    }
}