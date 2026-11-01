package com.nathanaeldorsey.stormmarket.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void registerUserCreatesUserWhenUsernameAndEmailAreUnique() {
        UserRequest request = new UserRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setUsername("janedoe");
        request.setPassword("secret123");
        request.setEmail("JANE@EXAMPLE.COM");
        request.setAddress("123 Main St");
        request.setPhone("1234567890");

        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("janedoe");
        savedUser.setEmail("jane@example.com");
        savedUser.setRole(Role.USER);

        UserResponse response = new UserResponse();
        response.setUsername("janedoe");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals("janedoe", result.getUsername());
        verify(userRepository)
                .save(argThat(user -> "encoded-password".equals(user.getPassword())
                        && "jane@example.com".equals(user.getEmail())
                        && user.getRole() == Role.USER));
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void registerUserRejectsDuplicateUsername() {
        UserRequest request = new UserRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setUsername("janedoe");
        request.setPassword("secret123");
        request.setEmail("jane@example.com");

        when(userRepository.existsByUsername("janedoe")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserRejectsDuplicateEmail() {
        UserRequest request = new UserRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setUsername("janedoe");
        request.setPassword("secret123");
        request.setEmail("JANE@EXAMPLE.COM");

        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
