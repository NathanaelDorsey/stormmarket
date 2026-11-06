package com.nathanaeldorsey.stormmarket.user;

import org.springframework.stereotype.Component;

@Component
/**
 * Maps user entities to response DTOs, omitting the password hash.
 */
public class UserMapper {
    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();

        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAddress(user.getAddress());
        response.setRole(user.getRole());

        return response;
    }
}
