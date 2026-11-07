package com.nathanaeldorsey.stormmarket.category;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.product.Product;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * Manages product categories: retrieval, creation, updates, and deletion.
 * Deleting a category removes it from all products that reference it.
 */
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private static final String CATEGORY_NOT_FOUND = "Category not found";

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));

        return categoryMapper.toResponse(category);
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();

        updateCategoryFields(category, request);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));

        updateCategoryFields(category, request);

        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(updatedCategory);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
        for (Product product : category.getProducts()) {
            product.removeCategory(category);
        }
        categoryRepository.deleteById(id);
    }

    private void updateCategoryFields(Category category, CategoryRequest request) {
        category.setName(request.getName());
    }
}