package com.nathanaeldorsey.stormmarket.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.user.Role;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
/**
 * Tests for AdminService administrative user management logic.
 * Covers role changes, activation/deactivation, and admin protection rules.
 */

class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminRoleLockRepository adminRoleLockRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, adminRoleLockRepository);
    }

    @Test
    void updateUserRoleSuccessfully() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setUsername("target");
        targetUser.setRole(Role.USER);

        when(adminRoleLockRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(new AdminRoleLock()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        adminService.updateUserRole(2L, Role.ADMIN, 1L);

        assertEquals(Role.ADMIN, targetUser.getRole());
        verify(userRepository).findById(2L);
    }

    @Test
    void updateUserRoleRejectsSelfDemotion() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        when(adminRoleLockRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(new AdminRoleLock()));

        assertThrows(IllegalStateException.class, () -> adminService.updateUserRole(1L, Role.USER, 1L));
    }

    @Test
    void updateUserRoleRejectsRemovingLastAdmin() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setRole(Role.ADMIN);

        when(adminRoleLockRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(new AdminRoleLock()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.countActiveByRole(Role.ADMIN)).thenReturn(1L); // Only 1 admin

        assertThrows(IllegalStateException.class, () -> adminService.updateUserRole(2L, Role.USER, 1L));
    }

    @Test
    void updateUserRoleThrowsWhenUserNotFound() {
        when(adminRoleLockRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(new AdminRoleLock()));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.updateUserRole(999L, Role.ADMIN, 1L));
    }

    @Test
    void updateUserRoleThrowsWhenLockNotFound() {
        when(adminRoleLockRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> adminService.updateUserRole(2L, Role.ADMIN, 1L));
    }

    @Test
    void setUserActiveSuccessfully() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setActive(true);
        targetUser.setRole(Role.USER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        adminService.setUserActive(2L, false, 1L);

        assertFalse(targetUser.isActive());
        verify(userRepository).findById(2L);
    }

    @Test
    void setUserActiveRejectsSelfDeactivation() {
        User admin = new User();
        admin.setId(1L);
        admin.setActive(true);
        admin.setRole(Role.ADMIN);

        assertThrows(IllegalStateException.class, () -> adminService.setUserActive(1L, false, 1L));
    }

    @Test
    void setUserActiveRejectsDeactivatingLastAdmin() {
        User admin = new User();
        admin.setId(1L);
        admin.setActive(true);
        admin.setRole(Role.ADMIN);

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setActive(true);
        targetUser.setRole(Role.ADMIN);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userRepository.countActiveByRole(Role.ADMIN)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> adminService.setUserActive(2L, false, 1L));
    }

    @Test
    void setUserActiveThrowsWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.setUserActive(999L, false, 1L));
    }

    @Test
    void getAllUsersReturnsList() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setEmail("admin@example.com");
        user1.setRole(Role.ADMIN);
        user1.setActive(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user");
        user2.setEmail("user@example.com");
        user2.setRole(Role.USER);
        user2.setActive(true);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<AdminUserResponse> result = adminService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("admin", result.get(0).getUsername());
        assertEquals(Role.ADMIN, result.get(0).getRole());
        assertEquals("user", result.get(1).getUsername());
        assertEquals(Role.USER, result.get(1).getRole());
    }

    @Test
    void getUserByIdReturnsUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
        user.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserResponse result = adminService.getUserById(1L);

        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals(Role.USER, result.getRole());
        assertTrue(result.isActive());
    }

    @Test
    void getUserByIdThrowsWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getUserById(999L));
    }
}