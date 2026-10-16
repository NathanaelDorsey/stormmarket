package com.nathanaeldorsey.stormmarket.admin;

import com.nathanaeldorsey.stormmarket.user.Role;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

    @Transactional
    public  void updateUserRole(Long userId, Role role){
        User user = getUser(userId);
        user.setRole(role);
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    AdminUserResponse response = new AdminUserResponse();
                    createUserResponse(user,response);
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long userId) {
        User user = getUser(userId);
        AdminUserResponse response = new AdminUserResponse();
        createUserResponse(user,response);
        return response;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    private void createUserResponse(User user, AdminUserResponse response){
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
    }
}
