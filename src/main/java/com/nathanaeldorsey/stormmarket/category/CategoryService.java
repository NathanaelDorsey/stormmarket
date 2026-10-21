package com.nathanaeldorsey.stormmarket.category;

import com.nathanaeldorsey.stormmarket.product.Product;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow();

        return categoryMapper.toResponse(category);
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();

        updateCategoryFields(category, request);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id).orElseThrow();

        updateCategoryFields(category, request);

        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(updatedCategory);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow();
        for (Product product : category.getProducts()) {
            product.removeCategory(category);
        }
        categoryRepository.deleteById(id);
    }

    private void updateCategoryFields(Category category, CategoryRequest request) {
        category.setName(request.getName());
    }
}
