package com.nathanaeldorsey.stormmarket.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Request to log in: requires a non-blank username and password.
 */
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
