package com.nathanaeldorsey.stormmarket.auth;

import com.nathanaeldorsey.stormmarket.user.UserRequest;
import com.nathanaeldorsey.stormmarket.user.UserResponse;
import com.nathanaeldorsey.stormmarket.user.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
/**
 * HTTP entry point for user registration and login.
 * Registration creates a new user with a hashed password; login validates credentials
 * and returns a signed JWT access token plus the user profile.
 * These endpoints are publicly accessible (no authentication required).
 */
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Registers a new user with the given details.
     *
     * @param request the user registration details (username, email, password, etc.)
     * @return the created user response
     * @throws ResourceAlreadyExistsException if the username or email is already taken
     */
    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return userService.registerUser(request);
    }

    /**
     * Authenticates a user with username and password.
     *
     * @param request the login credentials (username and password)
     * @return the login response containing the JWT access token and user profile
     * @throws BadCredentialsException if the username or password is invalid
     * @throws DisabledException       if the user account is deactivated
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
