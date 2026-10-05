package com.nathanaeldorsey.stormmarket.config;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class SecurityConfig {
    @Bean // Tells Spring, to create object and manage as bean
    public PasswordEncoder passwordEncoder(){
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
