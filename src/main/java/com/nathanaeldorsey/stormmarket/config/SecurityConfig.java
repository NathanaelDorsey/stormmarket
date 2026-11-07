package com.nathanaeldorsey.stormmarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
/**
 * Configures Spring Security for the application.
 *
 * Sets up Argon2 password hashing, JWT authentication (converting the "role" claim to
 * ROLE_* authorities), and a stateless session filter chain. Public access is granted
 * to /v1/auth/** and GET /v1/products/** and GET /v1/categories/**; all other endpoints require
 * authentication, with product/admin and category mutating endpoints restricted to ADMIN.
 */
public class SecurityConfig {
    private static final String AUTH_PATH = "/v1/auth/**";
    private static final String PRODUCTS_PATH = "/v1/products/**";
    private static final String CATEGORIES_PATH = "/v1/categories/**";
    private static final String ADMIN_ROLE = "ADMIN";

    @Bean // Tells Spring, to create object and manage as bean
    public PasswordEncoder passwordEncoder() { // Component for hashing passwords
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Maps JWT role to Spring Security Authority
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) {
        // rules for handling incoming requests
        // CSRF is disabled because this API uses stateless JWT bearer-token authentication.
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Disable csrf as using bearer token
                .authorizeHttpRequests(auth -> auth // configure request permissions
                        .requestMatchers(AUTH_PATH)
                        .permitAll() // explicit permit all requests to /v1/auth
                        // Admin product endpoints require the ADMIN role.
                        .requestMatchers("/v1/products/admin/**")
                        .hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.GET, CATEGORIES_PATH)
                        .permitAll() // permit all GET requests to categories
                        .requestMatchers(HttpMethod.GET, PRODUCTS_PATH)
                        .permitAll() // permit all GET requests to products
                        // Guest checkout - permit anonymous POST /v1/orders
                        .requestMatchers(HttpMethod.POST, "/v1/orders")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, PRODUCTS_PATH, CATEGORIES_PATH)
                        .hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT, PRODUCTS_PATH, CATEGORIES_PATH)
                        .hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, PRODUCTS_PATH, CATEGORIES_PATH)
                        .hasRole(ADMIN_ROLE)
                        // Uploaded product images are public.
                        .requestMatchers(HttpMethod.GET, "/v1/files/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated()) // all others must be authenticated
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter)) // enable Spring Security JWT authentication
                        );
        return http.build(); // returns SecurityFilterChain bean for processing requests
    }
}
