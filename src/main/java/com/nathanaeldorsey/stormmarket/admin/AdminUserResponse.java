package com.nathanaeldorsey.stormmarket.admin;

import com.nathanaeldorsey.stormmarket.user.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Response DTO for admin user listing: id, username, email, role, and active state.
 */
public class AdminUserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean active;
}
