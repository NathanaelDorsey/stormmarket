package com.nathanaeldorsey.stormmarket.auth;

import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.util.function.Predicate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class ActiveUserJwtValidator implements Predicate<Jwt> {
    private final UserRepository userRepository;

    public ActiveUserJwtValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean test(Jwt jwt) {
        return userRepository
                .findByUsername(jwt.getSubject())
                .filter(User::isActive)
                .isPresent();
    }
}
