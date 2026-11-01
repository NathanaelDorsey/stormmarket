package com.nathanaeldorsey.stormmarket.auth;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nathanaeldorsey.stormmarket.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerReturnsCreatedUserWithoutPassword() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content("""
                {
                  "firstName": "Jane",
                  "lastName": "Doe",
                  "username": "janedoe",
                  "password": "secret123",
                  "email": "JANE@example.com",
                  "address": "123 Main St",
                  "phone": "1234567890"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content("""
                {
                  "firstName": "",
                  "lastName": "Doe",
                  "username": "janedoe",
                  "password": "secret123",
                  "email": "not-an-email",
                  "address": "123 Main St",
                  "phone": "1234567890"
                }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        String requestBody = """
                {
                  "firstName": "Jane",
                  "lastName": "Doe",
                  "username": "janedoe",
                  "password": "secret123",
                  "email": "jane@example.com",
                  "address": "123 Main St",
                  "phone": "1234567890"
                }
                """;

        mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        String firstRequest = """
                {
                  "firstName": "Jane",
                  "lastName": "Doe",
                  "username": "janedoe",
                  "password": "secret123",
                  "email": "jane@example.com"
                }
                """;

        String secondRequest = """
                {
                  "firstName": "John",
                  "lastName": "Smith",
                  "username": "johnsmith",
                  "password": "secret123",
                  "email": "JANE@example.com"
                }
                """;

        mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content(firstRequest))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content(secondRequest))
                .andExpect(status().isConflict());
    }
}
