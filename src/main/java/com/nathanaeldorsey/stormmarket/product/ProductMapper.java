package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.category.Category;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component // Tells Spring, class is used for configuration
public class ProductMapper {
    public ProductResponse toResponse(Product product){
        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setImageUrl(product.getImageUrl());
        response.setCategoryIds(
                product.getCategories() // Get all categories for product
                        .stream() // Turn to stream for 1-by-1 processing
                        .map(Category::getId) // For each Category map to return of getId instead
                        .collect(Collectors.toSet()) // Collect stream back to Set
        );

        return response;
    }
}
