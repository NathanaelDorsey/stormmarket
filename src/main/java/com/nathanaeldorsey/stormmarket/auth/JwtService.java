package com.nathanaeldorsey.stormmarket.auth;

import com.nathanaeldorsey.stormmarket.user.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    public JwtService(JwtEncoder jwtEncoder){
        this.jwtEncoder=jwtEncoder;
    }

    public String generateToken(User user){
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600)) // 1 hour from now
                .build();

        JwtEncoderParameters parameters =
                JwtEncoderParameters.from(claims);

        return jwtEncoder.encode(parameters)
                .getTokenValue();
    }
}
