package com.nathanaeldorsey.stormmarket.product;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    // Product Service has-a Product Repository (Service needs Repository to access database)
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository=productRepository;
    }

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }
    // findById(id) returns Optional<Product>: product or empty. .orElseThrow throws on empty case
    public Product getProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow();
    }

    // DTO (ProductRequest) used to create entity
    public Product createProduct(ProductRequest request) {
        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        return productRepository.save(product);
    }
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        return productRepository.save(product);
    }
    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }
}
