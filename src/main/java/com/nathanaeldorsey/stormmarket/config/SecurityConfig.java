package com.nathanaeldorsey.stormmarket.config;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class SecurityConfig {
    @Bean // Tells Spring, to create object and manage as bean
    public PasswordEncoder passwordEncoder(){ // Component for hashing passwords
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) // rules for handling incoming requests
            throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable csrf as using bearer token
                .authorizeHttpRequests(auth -> auth // configure request permissions
                        .requestMatchers("/auth/**").permitAll()  // explicit permit all requests to /auth
                        .anyRequest().authenticated()) // all others must be authenticated
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()) // enable Spring Security JWT authentication
                );
        return http.build(); // returns SecurityFilterChain bean for processing requests
    }
}
