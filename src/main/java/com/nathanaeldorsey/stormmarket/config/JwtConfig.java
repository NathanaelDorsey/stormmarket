package com.nathanaeldorsey.stormmarket.config;

import com.nathanaeldorsey.stormmarket.auth.ActiveUserJwtValidator;
import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableScheduling
/**
 * Configures JWT signing and verification keys plus the JWT encoder/decoder beans.
 *
 * Loads RSA public and private keys from PEM files (configurable via properties or
 * environment variables), builds a Nimbus JwtEncoder/decoder pair, and installs a custom
 * JWT validator that rejects tokens for inactive or non-existent users.
 * Enables @Scheduled annotation support for order expiration job.
 */
public class JwtConfig {

    /**
     * Loads the RSA public key from a PEM file.
     *
     * @param location the key file location
     * @return the RSA public key
     * @throws IOException if the key file cannot be read
     */
    @Bean
    public RSAPublicKey rsaPublicKey(
            @Value(
                            "${STORMMARKET_JWT_PUBLIC_KEY_LOCATION:${stormmarket.jwt.public-key-location:classpath:jwt/public-key.pem}}")
                    String location)
            throws IOException {

        Resource resource = resolveResource(location);

        try (InputStream input = resource.getInputStream()) {
            return RsaKeyConverters.x509().convert(input);
        }
    }

    /**
     * Loads the RSA private key from a PEM file.
     *
     * @param location the key file location
     * @return the RSA private key
     * @throws IOException if the key file cannot be read
     */
    @Bean
    public RSAPrivateKey rsaPrivateKey(
            @Value(
                            "${STORMMARKET_JWT_PRIVATE_KEY_LOCATION:${stormmarket.jwt.private-key-location:classpath:jwt/private-key.pem}}")
                    String location)
            throws IOException {

        Resource resource = resolveResource(location);

        try (InputStream input = resource.getInputStream()) {
            return RsaKeyConverters.pkcs8().convert(input);
        }
    }

    private Resource resolveResource(String location) {
        String resourceLocation = location;

        if (location.startsWith("/")) {
            resourceLocation = "file:" + location;
        }

        return new DefaultResourceLoader().getResource(resourceLocation);
    }

    /**
     * Creates a JWT encoder using the RSA key pair.
     *
     * @param publicKey  the RSA public key
     * @param privateKey the RSA private key
     * @return the JWT encoder
     */
    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {

        return NimbusJwtEncoder.withKeyPair(publicKey, privateKey).build();
    }

    /**
     * Creates a JWT decoder that validates tokens and checks user accounts are active.
     *
     * @param publicKey          the RSA public key
     * @param activeUserJwtValidator validates that the token's user is active
     * @return the JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey, ActiveUserJwtValidator activeUserJwtValidator) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        decoder.setJwtValidator(jwt -> {
            OAuth2TokenValidatorResult result = JwtValidators.createDefault().validate(jwt);

            if (result.hasErrors()) {
                return result;
            }

            if (!activeUserJwtValidator.test(jwt)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "User account is inactive or does not exist", null));
            }

            return OAuth2TokenValidatorResult.success();
        });

        return decoder;
    }
}
