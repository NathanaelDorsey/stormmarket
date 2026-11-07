package com.nathanaeldorsey.stormmarket.category;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/categories")
/**
 * HTTP entry point for category management.
 * Public endpoints (GET /categories, GET /categories/{id}) are open to everyone.
 * Create, update, and delete operations require the ADMIN role.
 */
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * Creates a new category.
     *
     * @param request the category creation request
     * @return the created category response
     * @throws ResourceAlreadyExistsException if the category name already exists
     */
    @PostMapping
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    /**
     * Retrieves all categories.
     *
     * @return list of all category responses
     */
    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id:\\d+}")
    public CategoryResponse getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PutMapping("/{id:\\d+}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id:\\d+}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}
