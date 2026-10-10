package com.nathanaeldorsey.stormmarket.product;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController // Class handles HTTP requests and returned data is the response
@RequestMapping("/products") //Maps get request of /products to method
public class ProductController {
    // Product Controller has-a ProductService (Controller needs service to decide what should happen)
    private final ProductService productService;
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    /* CREATE */
    @PostMapping
    // @Valid used tells Spring to enforce rules
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request){
        return productService.createProduct(request);
    }

    /* READ */
    @GetMapping
    public List<ProductResponse> getAllProducts(){
        return productService.getAllProducts();
    }
    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return productService.searchProducts(
                name, minPrice, maxPrice, categoryId, categoryName,inStock, pageable);
    }
    @GetMapping("/{id:\\d+}")
    public ProductResponse getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    /* UPDATE */
    @PutMapping("/{id:\\d+}")
    public ProductResponse updateProduct(@PathVariable Long id,
                                 @Valid @RequestBody ProductRequest request){
        return productService.updateProduct(id, request);
    }

    /* DELETE */
    @DeleteMapping("/{id:\\d+}")
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }

    /* CATEGORIES */
    @PostMapping("/{productId}/categories/{categoryId}")
    public ProductResponse addCategory(
            @PathVariable Long productId,
            @PathVariable Long categoryId) {
        return productService.addCategory(productId,categoryId);
    }
    @DeleteMapping("/{productId}/categories/{categoryId}")
    public ProductResponse removeCategory(
            @PathVariable Long productId,
            @PathVariable Long categoryId) {
        return productService.removeCategory(productId,categoryId);
    }

}
