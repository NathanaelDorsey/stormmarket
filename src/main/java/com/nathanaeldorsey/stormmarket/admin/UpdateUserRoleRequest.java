package com.nathanaeldorsey.stormmarket.admin;

import com.nathanaeldorsey.stormmarket.user.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {
    @NotNull
    private Role role;
}
