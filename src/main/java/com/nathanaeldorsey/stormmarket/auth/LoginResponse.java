package com.nathanaeldorsey.stormmarket.auth;

import com.nathanaeldorsey.stormmarket.user.UserResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Response to a successful login: contains the JWT access token and the user profile.
 */
public class LoginResponse {
    private String token;
    private UserResponse user;
}
