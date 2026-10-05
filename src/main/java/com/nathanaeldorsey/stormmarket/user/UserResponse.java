package com.nathanaeldorsey.stormmarket.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
