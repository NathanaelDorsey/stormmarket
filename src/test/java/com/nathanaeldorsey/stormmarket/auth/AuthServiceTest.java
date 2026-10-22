package com.nathanaeldorsey.stormmarket.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nathanaeldorsey.stormmarket.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, userMapper, jwtService);
    }

    @Test
    void loginReturnsTokenAndUserWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret123");

        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        user.setActive(true);

        UserResponse userResponse = new UserResponse();
        userResponse.setUsername("alice");

        when(userRepository.findByUsername("alice")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("alice", response.getUser().getUsername());
        verify(userRepository).findByUsername("alice");
    }

    @Test
    void loginThrowsBadCredentialsWhenUsernameDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setUsername("missing");
        request.setPassword("secret123");

        when(userRepository.findByUsername("missing")).thenReturn(java.util.Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginThrowsBadCredentialsWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("wrong");

        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setActive(true);

        when(userRepository.findByUsername("alice")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginThrowsDisabledExceptionWhenUserIsInactive() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret123");

        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setActive(false);

        when(userRepository.findByUsername("alice")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);

        assertThrows(DisabledException.class, () -> authService.login(request));
    }
}
