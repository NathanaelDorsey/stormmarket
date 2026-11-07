package com.nathanaeldorsey.stormmarket.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.lang.reflect.Method;

class SecurityConfigTest {

    @Test
    void securityFilterChainMethodExists() throws Exception {
        Method method = SecurityConfig.class.getDeclaredMethod("securityFilterChain",
                HttpSecurity.class,
                org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(SecurityFilterChain.class);
    }

    @Test
    void securityConfigHasAuthPath() throws Exception {
        // Verify SecurityConfig defines AUTH_PATH constant for /v1/auth/**
        java.lang.reflect.Field field = SecurityConfig.class.getDeclaredField("AUTH_PATH");
        field.setAccessible(true);
        Object value = field.get(null);
        assertThat(value).isEqualTo("/v1/auth/**");
    }
}
