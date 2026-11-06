package com.nathanaeldorsey.stormmarket.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.user.Role;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = AdminController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.web
                    .OAuth2ResourceServerWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private UserRepository userRepository;

    private Authentication createAuth(String username) {
        return new UsernamePasswordAuthenticationToken(username, null, List.of());
    }

    @Test
    void updateUserRoleCallsService() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(put("/admin/users/2/role")
                        .principal(createAuth("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(adminService).updateUserRole(eq(2L), eq(Role.ADMIN), eq(1L));
    }

    @Test
    void updateUserRoleWithInvalidRoleReturnsBadRequest() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(put("/admin/users/2/role")
                        .principal(createAuth("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setUserActiveCallsService() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(patch("/admin/users/2/active")
                        .principal(createAuth("admin"))
                        .param("active", "false"))
                .andExpect(status().isNoContent());

        verify(adminService).setUserActive(eq(2L), eq(false), eq(1L));
    }

    @Test
    void getAllUsersCallsService() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        AdminUserResponse user1 = new AdminUserResponse();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setEmail("admin@example.com");
        user1.setRole(Role.ADMIN);
        user1.setActive(true);

        AdminUserResponse user2 = new AdminUserResponse();
        user2.setId(2L);
        user2.setUsername("user");
        user2.setEmail("user@example.com");
        user2.setRole(Role.USER);
        user2.setActive(true);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(adminService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/admin/users")
                        .principal(createAuth("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].username").value("user"));
    }

    @Test
    void getUserByIdCallsService() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        AdminUserResponse response = new AdminUserResponse();
        response.setId(2L);
        response.setUsername("target");
        response.setEmail("target@example.com");
        response.setRole(Role.USER);
        response.setActive(true);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(adminService.getUserById(2L)).thenReturn(response);

        mockMvc.perform(get("/admin/users/2")
                        .principal(createAuth("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("target"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void updateUserRoleSelfDemotionReturnsConflict() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        doThrow(new IllegalStateException("Administrators cannot remove their own admin role"))
                .when(adminService).updateUserRole(eq(1L), eq(Role.USER), eq(1L));

        mockMvc.perform(put("/admin/users/1/role")
                        .principal(createAuth("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "USER"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Administrators cannot remove their own admin role"));
    }

    @Test
    void setUserActiveSelfDeactivationReturnsConflict() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        doThrow(new IllegalStateException("You cannot deactivate your own account"))
                .when(adminService).setUserActive(eq(1L), eq(false), eq(1L));

        mockMvc.perform(patch("/admin/users/1/active")
                        .principal(createAuth("admin"))
                        .param("active", "false"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You cannot deactivate your own account"));
    }
}