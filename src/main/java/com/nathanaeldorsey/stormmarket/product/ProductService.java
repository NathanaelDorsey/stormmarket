package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.cart.CartItemRepository;
import com.nathanaeldorsey.stormmarket.category.Category;
import com.nathanaeldorsey.stormmarket.category.CategoryRepository;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.order.OrderItemRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    // Product Service has-a Product Repository (Service needs Repository to access database)
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private static final String PRODUCT_NOT_FOUND = "Product not found";

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductMapper productMapper,
            OrderItemRepository orderItemRepository,
            CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll(ProductSpecification.isActive()).stream()
                .map(productMapper::toResponse)
                .toList();
    }
    // findById(id) returns Optional<Product>: product or empty. .orElseThrow throws on empty case
    public ProductResponse getProductById(Long id) {
        return getProduct(id, false);
    }

    public ProductResponse getProductByIdForAdmin(Long id) {
        return getProduct(id, true);
    }

    private ProductResponse getProduct(Long id, boolean includeInactive) {
        Product product = productRepository
                .findById(id)
                .filter(p -> includeInactive || p.isActive())
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));
        if (!includeInactive && !product.isActive()) {
            throw new ResourceNotFoundException(PRODUCT_NOT_FOUND);
        }
        return productMapper.toResponse(product);
    }
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        Specification<Product> specification = buildSearchSpecification(request).and(ProductSpecification.isActive());
        return productRepository.findAll(specification, pageable).map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProductsForAdmin(ProductSearchRequest request, Pageable pageable) {

        Specification<Product> specification =
                buildSearchSpecification(request).and(ProductSpecification.activeStatus(request.getActive()));

        return productRepository.findAll(specification, pageable).map(productMapper::toResponse);
    }

    // DTO (ProductRequest) used to create entity
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();

        updateProductFields(product, request);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        updateProductFields(product, request);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    public ProductResponse setProductActive(Long id, boolean active) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        product.setActive(active);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    public ProductResponse addCategory(Long productId, Long categoryId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.addCategory(category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    public ProductResponse removeCategory(Long productId, Long categoryId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.removeCategory(category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product =
                productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        if (orderItemRepository.existsByProductId(id)) {
            throw new IllegalStateException("Cannot delete a product that appears in order history");
        }

        cartItemRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    private void updateProductFields(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
    }

    private Specification<Product> buildSearchSpecification(ProductSearchRequest request) {
        return Specification.where(ProductSpecification.nameContains(request.getName()))
                .and(ProductSpecification.priceBetween(request.getMinPrice(), request.getMaxPrice()))
                .and(ProductSpecification.hasCategory(request.getCategoryId()))
                .and(ProductSpecification.hasCategoryName(request.getCategoryName()))
                .and(ProductSpecification.inStock(request.getInStock()));
    }
}
