package com.nathanaeldorsey.stormmarket.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Response DTO for a user profile: id, first name, last name, username, email, phone,
 * address, and role. Password is intentionally omitted from the response.
 */
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    // Password left out to stay persistent in entity, not exposing in API response
    private String email;
    private String phone;
    private String address;
    private Role role;
}
