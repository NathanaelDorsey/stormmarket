package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.cart.CartItemRepository;
import com.nathanaeldorsey.stormmarket.category.Category;
import com.nathanaeldorsey.stormmarket.category.CategoryRepository;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.order.OrderItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
/**
 * Manages product catalog operations: retrieval, search, creation, updates,
 * activation toggling, category assignment, and deletion.
 *
 * Products that appear in order history cannot be deleted. Search supports
 * filtering by name, category, price range, active state, and stock state.
 */
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private static final String PRODUCT_NOT_FOUND = "Product not found";

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll(ProductSpecification.isActive()).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public List<ProductResponse> getAllProductsForAdmin() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves a product by ID (active products only).
     *
     * @param id the product ID
     * @return the product response
     * @throws ResourceNotFoundException if the product does not exist
     */
    // findById(id) returns Optional<Product>: product or empty. .orElseThrow throws on empty case
    public ProductResponse getProductById(Long id) {
        return getProduct(id, false);
    }

    /**
     * Retrieves a product by ID for admin access (includes inactive products).
     *
     * @param id the product ID
     * @return the product response
     * @throws ResourceNotFoundException if the product does not exist
     */
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

    /**
     * Searches active products by name, category, price range, active state, and stock state.
     *
     * @param request  the search criteria
     * @param pageable the pagination request
     * @return page of matching product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        Specification<Product> specification = buildSearchSpecification(request).and(ProductSpecification.isActive());
        return productRepository.findAll(specification, pageable).map(productMapper::toResponse);
    }

    /**
     * Searches all products (including inactive) for admin by name, category, price range, active state, and stock state.
     *
     * @param request  the search criteria
     * @param pageable the pagination request
     * @return page of matching product responses
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProductsForAdmin(ProductSearchRequest request, Pageable pageable) {

        Specification<Product> specification =
                buildSearchSpecification(request).and(ProductSpecification.activeStatus(request.getActive()));

        return productRepository.findAll(specification, pageable).map(productMapper::toResponse);
    }

    /**
     * Creates a new product from the given request.
     *
     * @param request the product creation request
     * @return the created product response
     */
    // DTO (ProductRequest) used to create entity
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();

        updateProductFields(product, request);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    /**
     * Updates an existing product's fields.
     *
     * @param id     the product ID
     * @param request the updated product data
     * @return the updated product response
     * @throws ResourceNotFoundException if the product does not exist
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product =
                productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        updateProductFields(product, request);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Activates or deactivates a product.
     *
     * @param id    the product ID
     * @param active whether the product should be active
     * @return the updated product response
     * @throws ResourceNotFoundException if the product does not exist
     */
    public ProductResponse setProductActive(Long id, boolean active) {
        Product product =
                productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        product.setActive(active);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Adds a category to a product.
     *
     * @param productId  the product ID
     * @param categoryId the category ID
     * @return the updated product response
     * @throws ResourceNotFoundException if the product or category does not exist
     */
    @Transactional
    public ProductResponse addCategory(Long productId, Long categoryId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.addCategory(category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Removes a category from a product.
     *
     * @param productId  the product ID
     * @param categoryId the category ID
     * @return the updated product response
     * @throws ResourceNotFoundException if the product or category does not exist
     */
    @Transactional
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

    /**
     * Updates a product's image URL.
     *
     * @param id       the product ID
     * @param imageUrl the new image URL
     * @return the updated product response
     * @throws ResourceNotFoundException if the product does not exist
     */
    public ProductResponse updateProductImage(Long id, String imageUrl) {
        Product product =
                productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND));

        product.setImageUrl(imageUrl);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Deletes a product. Rejected if the product appears in order history.
     *
     * @param id the product ID
     * @throws IllegalStateException if the product appears in order history
     * @throws ResourceNotFoundException if the product does not exist
     */
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
        // Handle active status from import
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        // Handle categories from import
        if (request.getCategoryIds() != null) {
            product.getCategories().clear();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
                product.addCategory(category);
            }
        }
    }

    private Specification<Product> buildSearchSpecification(ProductSearchRequest request) {
        return Specification.where(ProductSpecification.nameContains(request.getName()))
                .and(ProductSpecification.priceBetween(request.getMinPrice(), request.getMaxPrice()))
                .and(ProductSpecification.hasCategory(request.getCategoryId()))
                .and(ProductSpecification.hasCategoryName(request.getCategoryName()))
                .and(ProductSpecification.inStock(request.getInStock()));
    }
}
