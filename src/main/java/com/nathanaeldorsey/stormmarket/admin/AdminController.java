package com.nathanaeldorsey.stormmarket.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PutMapping("/{userId:\\d+}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request){
        adminService.updateUserRole(userId, request.getRole());

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }
    @GetMapping("/{userId:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponse getUserById(
            @PathVariable Long userId) {

        return adminService.getUserById(userId);
    }

}
