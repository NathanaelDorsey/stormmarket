package com.nathanaeldorsey.stormmarket.category;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = CategoryController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.web
                    .OAuth2ResourceServerWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void createCategoryReturnsCreatedCategory() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Electronics");

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Electronics"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void createCategoryWithInvalidNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void getAllCategoriesReturnsList() throws Exception {
        CategoryResponse cat1 = new CategoryResponse();
        cat1.setId(1L);
        cat1.setName("Electronics");

        CategoryResponse cat2 = new CategoryResponse();
        cat2.setId(2L);
        cat2.setName("Books");

        when(categoryService.getAllCategories()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Books"));
    }

    @Test
    void getCategoryByIdReturnsCategory() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Electronics");

        when(categoryService.getCategoryById(1L)).thenReturn(response);

        mockMvc.perform(get("/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategoryByIdWhenNotFoundReturns404() throws Exception {
        when(categoryService.getCategoryById(999L))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(get("/v1/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found"))
                .andExpect(jsonPath("$.path").value("/v1/categories/999"));
    }

    @Test
    void updateCategoryReturnsUpdatedCategory() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Updated Electronics");

        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Electronics"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Electronics"));
    }

    @Test
    void updateCategoryWhenNotFoundReturns404() throws Exception {
        when(categoryService.updateCategory(eq(999L), any(CategoryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(put("/v1/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "New Name"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    void deleteCategoryReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/v1/categories/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCategoryWhenNotFoundReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found"))
                .when(categoryService).deleteCategory(999L);

        mockMvc.perform(delete("/v1/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    void invalidRequestBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not valid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingRequiredFieldReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Missing name"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }
}