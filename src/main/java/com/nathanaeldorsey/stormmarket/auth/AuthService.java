package com.nathanaeldorsey.stormmarket.auth;

import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserMapper;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * Handles user authentication: login validation and JWT token generation.
 * Validates credentials against stored user records, checks account activation status,
 * and issues a signed JWT access token upon successful login.
 */
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    /**
     * Authenticates a user and issues a JWT access token.
     *
     * @param request the login credentials (username and password)
     * @return the login response containing the JWT token and mapped user profile
     * @throws BadCredentialsException if the username or password is invalid
     * @throws DisabledException       if the user account is deactivated
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!user.isActive()) {
            throw new DisabledException("Account is deactivated");
        }
        String token = jwtService.generateToken(user);
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(userMapper.toResponse(user));

        return response;
    }
}
