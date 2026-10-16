package com.nathanaeldorsey.stormmarket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean // Tells Spring, to create object and manage as bean
    public PasswordEncoder passwordEncoder(){ // Component for hashing passwords
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        // Maps JWT role to Spring Security Authority
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter
                = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {
        // rules for handling incoming requests
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable csrf as using bearer token
                .authorizeHttpRequests(auth -> auth // configure request permissions
                        .requestMatchers("/auth/**").permitAll()  // explicit permit all requests to /auth
                        .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**")
                        .permitAll() // permit all get requests to products and categories
                        .requestMatchers(HttpMethod.POST, "/products/**", "/categories/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**", "/categories/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**", "/categories/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()) // all others must be authenticated
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)) // enable Spring Security JWT authentication
                );
        return http.build(); // returns SecurityFilterChain bean for processing requests
    }
}
