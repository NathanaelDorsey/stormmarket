package com.nathanaeldorsey.stormmarket.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nathanaeldorsey.stormmarket.auth.AuthService;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceAlreadyExistsException;
import com.nathanaeldorsey.stormmarket.user.UserRequest;
import com.nathanaeldorsey.stormmarket.user.UserResponse;
import com.nathanaeldorsey.stormmarket.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.public-key-location",
                () -> "classpath:jwt/public-key.pem");
        registry.add("spring.security.oauth2.resourceserver.jwt.private-key-location",
                () -> "classpath:jwt/private-key.pem");
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    private String validJson() {
        return """
            {
              "firstName": "John",
              "lastName": "Doe",
              "username": "johndoe",
              "password": "password123",
              "email": "john.doe@example.com"
            }
            """;
    }

    private String invalidJson() {
        return """
            {
              "firstName": "",
              "lastName": "",
              "username": "",
              "password": "short",
              "email": "not-an-email"
            }
            """;
    }

    private String duplicateUsernameJson() {
        return """
            {
              "firstName": "Jane",
              "lastName": "Smith",
              "username": "existing",
              "password": "password123",
              "email": "jane@example.com"
            }
            """;
    }

    private String duplicateEmailJson() {
        return """
            {
              "firstName": "Jane",
              "lastName": "Smith",
              "username": "newuser",
              "password": "password123",
              "email": "existing@example.com"
            }
            """;
    }

    @Test
    void registerReturnsCreatedUserWithoutPassword() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setUsername("johndoe");
        response.setEmail("john.doe@example.com");

        when(userService.registerUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    void registerRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        when(userService.registerUser(any(UserRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Username already exists"));

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateUsernameJson()))
                .andExpect(status().isConflict());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        when(userService.registerUser(any(UserRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateEmailJson()))
                .andExpect(status().isConflict());
    }
}