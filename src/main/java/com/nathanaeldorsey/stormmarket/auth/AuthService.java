package com.nathanaeldorsey.stormmarket.auth;

import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserMapper;
import com.nathanaeldorsey.stormmarket.user.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       JwtService jwtService){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        if(!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())){
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(userMapper.toResponse(user));

        return response;
    }

}
