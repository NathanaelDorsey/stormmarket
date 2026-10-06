package com.nathanaeldorsey.stormmarket.auth;

import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserMapper;
import com.nathanaeldorsey.stormmarket.user.UserRepository;

import com.nathanaeldorsey.stormmarket.user.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserResponse login(LoginRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        if(!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())){
            throw new RuntimeException("Invalid username or password");
        }
        return userMapper.toResponse(user);
    }

}
