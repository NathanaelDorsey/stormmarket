package com.nathanaeldorsey.stormmarket.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void publicAuthEndpointsAreAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .with(anonymous())
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanAccessProtectedAdminProductSearchEndpoint() throws Exception {
        mockMvc.perform(get("/products/admin/search")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserCanAccessPublicProductsEndpoint() throws Exception {
        mockMvc.perform(get("/products").with(anonymous()))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserCannotAccessAdminProductSearch() throws Exception {
        mockMvc.perform(get("/products/admin/search").with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUserCannotAccessAdminProductSearch() throws Exception {
        mockMvc.perform(get("/products/admin/search")
                        .with(user("customer").roles("USER")))
                .andExpect(status().isForbidden());
    }


    @Test
    void securityConfigurationLoads() {
        assertThat(applicationContext.getBean(SecurityFilterChain.class))
                .isNotNull();
    }
}
