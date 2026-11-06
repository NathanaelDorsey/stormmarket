package com.nathanaeldorsey.stormmarket.admin;

import com.nathanaeldorsey.stormmarket.user.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * Request to update a user's role. Requires a non-null role value.
 */
public class UpdateUserRoleRequest {
    @NotNull
    private Role role;
}
