package com.nathanaeldorsey.stormmarket.admin;

import com.nathanaeldorsey.stormmarket.user.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {
    private Role role;
}
