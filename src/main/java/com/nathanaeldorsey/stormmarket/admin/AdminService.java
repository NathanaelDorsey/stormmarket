package com.nathanaeldorsey.stormmarket.admin;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.user.Role;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
/**
 * Provides administrative user management: updating user roles, activating/deactivating
 * accounts, and listing or retrieving user profiles.
 *
 * Role changes are serialized using a shared lock row. An administrator cannot remove
 * their own admin role, deactivate themselves, or remove/deactivate the last remaining
 * active administrator.
 */
public class AdminService {
    private final UserRepository userRepository;
    private final AdminRoleLockRepository adminRoleLockRepository;
    private static final String USER_NOT_FOUND = "User not found";

    /**
     * Updates a user's role. Serialized via a shared lock row.
     * Prevents administrators from removing their own admin role or the last administrator.
     *
     * @param userId     the user whose role to update
     * @param role       the new role
     * @param adminUserId the administrator making the change
     * @throws IllegalStateException if self-demotion or removing the last admin
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Transactional
    public void updateUserRole(Long userId, Role role, Long adminUserId) {
        // Serialize all role changes using the shared lock row.
        adminRoleLockRepository
                .findByIdForUpdate(1L)
                .orElseThrow(() -> new IllegalStateException("Admin role lock row not found"));
        // Prevent an administrator from demoting themselves.
        if (userId.equals(adminUserId) && role != Role.ADMIN) {
            throw new IllegalStateException("Administrators cannot remove their own admin role");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        // Prevent demoting the final administrator.
        if (user.getRole() == Role.ADMIN && role != Role.ADMIN && userRepository.countActiveByRole(Role.ADMIN) <= 1) {
            throw new IllegalStateException("Cannot remove the last administrator");
        }
        user.setRole(role);
    }

    /**
     * Activates or deactivates a user.
     * Prevents administrators from deactivating themselves or the last active administrator.
     *
     * @param userId     the user to activate/deactivate
     * @param active     whether the user should be active
     * @param adminUserId the administrator making the change
     * @throws IllegalStateException if self-deactivation or deactivating the last admin
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Transactional
    public void setUserActive(Long userId, boolean active, Long adminUserId) {
        if (userId.equals(adminUserId) && !active) {
            throw new IllegalStateException("You cannot deactivate your own account");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (user.isActive()
                && !active
                && user.getRole() == Role.ADMIN
                && userRepository.countActiveByRole(Role.ADMIN) <= 1) {
            throw new IllegalStateException("Cannot deactivate the last active administrator");
        }
        user.setActive(active);
    }

    /**
     * Lists all users.
     *
     * @return list of all user responses
     */
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    AdminUserResponse response = new AdminUserResponse();
                    createUserResponse(user, response);
                    return response;
                })
                .toList();
    }

    /**
     * Retrieves a user by ID.
     *
     * @param userId the user ID
     * @return the user response
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long userId) {
        User user = getUser(userId);
        AdminUserResponse response = new AdminUserResponse();
        createUserResponse(user, response);
        return response;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private void createUserResponse(User user, AdminUserResponse response) {
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
    }
}
