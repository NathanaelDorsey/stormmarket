package com.nathanaeldorsey.stormmarket.category;

import com.nathanaeldorsey.stormmarket.product.Product;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setProductIds(
                category.getProducts()
                        .stream()
                        .map(Product::getId)
                        .collect(Collectors.toSet())
        );

        return response;
    }
}

