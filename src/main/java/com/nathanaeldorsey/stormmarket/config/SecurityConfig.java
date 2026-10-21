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
public class SecurityConfig {
    private static final String PRODUCTS_PATH = "/products/**";
    private static final String CATEGORIES_PATH = "/categories/**";
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
                        .requestMatchers("/auth/**")
                        .permitAll() // explicit permit all requests to /auth
                        .requestMatchers(HttpMethod.GET, CATEGORIES_PATH)
                        .permitAll() // permit all GET requests to categories
                        .requestMatchers(HttpMethod.GET, PRODUCTS_PATH)
                        .authenticated() // require authentication for product reads
                        .requestMatchers(HttpMethod.POST, PRODUCTS_PATH, CATEGORIES_PATH)
                        .hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT, PRODUCTS_PATH, CATEGORIES_PATH)
                        .hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, PRODUCTS_PATH, CATEGORIES_PATH)
                        .hasRole(ADMIN_ROLE)
                        .anyRequest()
                        .authenticated()) // all others must be authenticated
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter)) // enable Spring Security JWT authentication
                        );
        return http.build(); // returns SecurityFilterChain bean for processing requests
    }
}
