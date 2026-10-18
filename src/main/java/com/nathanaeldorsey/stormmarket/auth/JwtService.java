package com.nathanaeldorsey.stormmarket.auth;

import com.nathanaeldorsey.stormmarket.user.User;
import java.time.Instant;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(User user) {
        Instant now = Instant.now(); // get current time

        JwtClaimsSet claims = JwtClaimsSet.builder() // claim - info saved in jwt
                .subject(user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600)) // 1 hour from now
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(claims); // put in form Encoder expects

        return jwtEncoder.encode(parameters).getTokenValue(); // returns signed jwt
    }
}
