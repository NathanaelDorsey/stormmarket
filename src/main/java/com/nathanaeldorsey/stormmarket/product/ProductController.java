package com.nathanaeldorsey.stormmarket.product;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController // Class handles HTTP requests and returned data is the response
@RequestMapping("/products") // Maps get request of /products to method
public class ProductController {
    // Product Controller has-a ProductService (Controller needs service to decide what should happen)
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /* CREATE */
    @PostMapping
    // @Valid used tells Spring to enforce rules
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    /* READ */
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(
            @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return productService.searchProducts(request, pageable);
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ProductResponse> searchProductsForAdmin(
            @ModelAttribute ProductSearchRequest request,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        return productService.searchProductsForAdmin(request, pageable);
    }

    @GetMapping("/{id:\\d+}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/admin/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse getProductByIdForAdmin(@PathVariable Long id) {
        return productService.getProductByIdForAdmin(id);
    }

    /* UPDATE */
    @PutMapping("/{id:\\d+}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @PatchMapping("/admin/{id:\\d+}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse setProductActive(@PathVariable Long id, @RequestParam boolean active) {

        return productService.setProductActive(id, active);
    }

    /* DELETE */
    @DeleteMapping("/{id:\\d+}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    /* CATEGORIES */
    @PostMapping("/{productId}/categories/{categoryId}")
    public ProductResponse addCategory(@PathVariable Long productId, @PathVariable Long categoryId) {
        return productService.addCategory(productId, categoryId);
    }

    @DeleteMapping("/{productId}/categories/{categoryId}")
    public ProductResponse removeCategory(@PathVariable Long productId, @PathVariable Long categoryId) {
        return productService.removeCategory(productId, categoryId);
    }
}
