package com.nathanaeldorsey.stormmarket.category;

import com.nathanaeldorsey.stormmarket.product.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository=categoryRepository;
    }

    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }
    public Category getCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow();
    }

    public Category createCategory(CategoryRequest request){
        Category category = new Category();

        category.setName(request.getName());

        return categoryRepository.save(category);
    }
    public Category updateCategory(Long id, Category request){
        Category category = categoryRepository.findById(id)
                .orElseThrow();

        category.setName(request.getName());

        return categoryRepository.save(category);
    }
    public void deleteCategory(Long id){
        categoryRepository.deleteById(id);
    }
}
