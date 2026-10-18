package com.nathanaeldorsey.stormmarket.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$") // only letters and numbers
    private String username;

    @NotBlank
    @Size(min = 8) // min length of 8
    private String password;

    @NotBlank
    @Email
    private String email;

    private String address;

    @Pattern(regexp = "^\\d{10}$") // only 10 numbers
    private String phone;
}
