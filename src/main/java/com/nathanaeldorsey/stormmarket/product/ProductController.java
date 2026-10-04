package com.nathanaeldorsey.stormmarket.product;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    /* UPDATE */
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id,
                                 @Valid @RequestBody ProductRequest request){
        return productService.updateProduct(id, request);
    }

    /* DELETE */
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }

    /* CATEGORIES */
    @PostMapping("/{productId}/categories/{categoryId}")
    public void addCategory(
            @PathVariable Long productId,
            @PathVariable Long categoryId) {
        productService.addCategory(productId,categoryId);
    }
    @DeleteMapping("/{productId}/categories/{categoryId}")
    public void removeCategory(
            @PathVariable Long productId,
            @PathVariable Long categoryId) {
        productService.removeCategory(productId,categoryId);
    }

}
