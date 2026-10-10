package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.category.Category;
import com.nathanaeldorsey.stormmarket.category.CategoryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    // Product Service has-a Product Repository (Service needs Repository to access database)
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductMapper productMapper){
        this.productRepository=productRepository;
        this.categoryRepository=categoryRepository;
        this.productMapper = productMapper;
    }

    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }
    // findById(id) returns Optional<Product>: product or empty. .orElseThrow throws on empty case
    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow();
        return productMapper.toResponse(product);
    }
    public Page<ProductResponse> searchProducts(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long categoryId,
            String categoryName,
            Boolean inStock,
            Pageable pageable) {
        Specification<Product> specification =
                Specification.where(ProductSpecification.nameContains(name))
                        .and(ProductSpecification.priceBetween(minPrice, maxPrice))
                        .and(ProductSpecification.hasCategory(categoryId))
                        .and(ProductSpecification.hasCategoryName(categoryName))
                        .and(ProductSpecification.inStock(inStock));
        return productRepository.findAll(specification, pageable)
                .map(productMapper::toResponse);
    }

    // DTO (ProductRequest) used to create entity
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();

        updateProductFields(product, request);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow();

        updateProductFields(product, request);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    public ProductResponse addCategory(Long productId, Long categoryId){
        Product product = productRepository.findById(productId)
                .orElseThrow();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow();

        product.addCategory(category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }
    public ProductResponse removeCategory(Long productId, Long categoryId){
        Product product = productRepository.findById(productId)
                .orElseThrow();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow();

        product.removeCategory(category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }
	private void updateProductFields(Product product, ProductRequest request){
		product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
	}

}
