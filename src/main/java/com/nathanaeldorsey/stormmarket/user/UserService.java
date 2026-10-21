package com.nathanaeldorsey.stormmarket.user;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    // Service has Password encoder and user repository
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserResponse registerUser(UserRequest request) {
        // Reject duplicate usernames
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        } // Reject duplicate emails
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        User user = new User();

        updateUserFields(user, request);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    private void updateUserFields(User user, UserRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        user.setRole(Role.USER);
    }
}
