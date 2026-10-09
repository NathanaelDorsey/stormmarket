package com.nathanaeldorsey.stormmarket.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {
    @Bean
    public JwtEncoder jwtEncoder(
            @Value("${STORMMARKET_JWT_PUBLIC_KEY_LOCATION}") RSAPublicKey publicKey,
            @Value("${STORMMARKET_JWT_PRIVATE_KEY_LOCATION}") RSAPrivateKey privateKey){
        return NimbusJwtEncoder
                .withKeyPair(publicKey,privateKey)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey){
        return NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();
    }
}
