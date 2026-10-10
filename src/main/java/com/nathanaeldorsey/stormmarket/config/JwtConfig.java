package com.nathanaeldorsey.stormmarket.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {
    @Bean
    public RSAPublicKey rsaPublicKey(
            @Value("${STORMMARKET_JWT_PUBLIC_KEY_LOCATION}") String location)
            throws IOException {

        try (InputStream input = Files.newInputStream(
                Path.of(location.replace("file:", "")))) {
            return RsaKeyConverters.x509().convert(input);
        }
    }
    @Bean
    public RSAPrivateKey rsaPrivateKey(
            @Value("${STORMMARKET_JWT_PRIVATE_KEY_LOCATION}") String location)
            throws IOException {

        try (InputStream input = Files.newInputStream(
                Path.of(location.replace("file:", "")))) {
            return RsaKeyConverters.pkcs8().convert(input);
        }
    }
    @Bean
    public JwtEncoder jwtEncoder(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey){
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
