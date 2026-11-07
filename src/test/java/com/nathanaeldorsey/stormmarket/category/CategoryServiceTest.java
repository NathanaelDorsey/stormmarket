package com.nathanaeldorsey.stormmarket.category;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nathanaeldorsey.stormmarket.product.Product;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
/**
 * Tests for CategoryService category business logic.
 * Covers CRUD, unique constraints, and deletion with product relationships.
 */

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, categoryMapper);
    }

    @Test
    void getAllCategoriesReturnsMappedList() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Electronics");

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Books");

        CategoryResponse resp1 = new CategoryResponse();
        resp1.setId(1L);
        resp1.setName("Electronics");

        CategoryResponse resp2 = new CategoryResponse();
        resp2.setId(2L);
        resp2.setName("Books");

        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));
        when(categoryMapper.toResponse(cat1)).thenReturn(resp1);
        when(categoryMapper.toResponse(cat2)).thenReturn(resp2);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName());
        assertEquals("Books", result.get(1).getName());
    }

    @Test
    void getCategoryByIdReturnsMappedCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Electronics");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertEquals("Electronics", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void getCategoryByIdThrowsWhenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void createCategorySuccessfully() {
        CategoryRequest request = new CategoryRequest();
        request.setName("New Category");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("New Category");

        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("New Category");

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        assertEquals("New Category", result.getName());
        assertEquals(1L, result.getId());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategorySuccessfully() {
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Old Name");

        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Name");

        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Updated Name");

        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Updated Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(existingCategory)).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(updatedCategory)).thenReturn(response);

        CategoryResponse result = categoryService.updateCategory(1L, request);

        assertEquals("Updated Name", result.getName());
    }

    @Test
    void updateCategoryThrowsWhenNotFound() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Name");

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(99L, request));
    }

    @Test
    void deleteCategoryRemovesProductAssociationsAndDeletes() {
        Category category = new Category();
        category.setId(1L);
        category.setName("To Delete");

        Product product = new Product();
        product.setId(10L);
        product.addCategory(category); // Set up bidirectional relationship

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Verify the relationship exists before deletion
        assertFalse(product.getCategories().isEmpty());
        assertTrue(product.getCategories().contains(category));

        categoryService.deleteCategory(1L);

        assertTrue(product.getCategories().isEmpty());
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategoryThrowsWhenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));
    }
}