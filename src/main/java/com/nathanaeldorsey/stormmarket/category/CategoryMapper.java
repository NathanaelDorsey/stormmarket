package com.nathanaeldorsey.stormmarket.category;

import com.nathanaeldorsey.stormmarket.product.Product;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setProductIds(category.getProducts().stream()
                .filter(Product::isActive)
                .map(Product::getId)
                .collect(Collectors.toSet()));

        return response;
    }
}
