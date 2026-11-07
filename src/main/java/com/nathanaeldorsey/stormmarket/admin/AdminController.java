package com.nathanaeldorsey.stormmarket.admin;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
/**
 * HTTP entry point for admin user management.
 * All endpoints require the ADMIN role. Allows updating user roles,
 * activating/deactivating users, and listing or retrieving user profiles.
 */
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    @PutMapping("/{userId:\\d+}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request,
            Authentication authentication) {
        User admin = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        adminService.updateUserRole(userId, request.getRole(), admin.getId());

        return ResponseEntity.noContent().build();
    }

    /**
     * Activates or deactivates a user.
     *
     * @param userId      the user to activate/deactivate
     * @param active      whether the user should be active
     * @param authentication the authenticated admin
     * @return 204 No Content on success
     * @throws ResourceNotFoundException if the admin or target user does not exist
     */
    @PatchMapping("/{userId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> setUserActive(
            @PathVariable Long userId, @RequestParam boolean active, Authentication authentication) {
        User admin = userRepository.findByUsername(authentication.getName()).orElseThrow();
        adminService.setUserActive(userId, active, admin.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all users.
     *
     * @param authentication the authenticated admin
     * @return list of all user responses
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/{userId:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponse getUserById(@PathVariable Long userId) {

        return adminService.getUserById(userId);
    }
}
