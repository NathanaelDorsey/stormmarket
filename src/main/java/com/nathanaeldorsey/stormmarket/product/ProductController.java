package com.nathanaeldorsey.stormmarket.product;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Class handles HTTP requests and returned data is the response
public class ProductController {
    // Product Controller has-a ProductService (Controller needs service to decide what should happen)
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }
    /* CREATE */
    @PostMapping("/products")
    // @Valid used tells Spring to enforce rules
    public Product createProduct(@Valid @RequestBody ProductRequest request){
        return productService.createProduct(request);
    }

    /* READ */
    @GetMapping("/products") //Maps get request of /products to method
    public List<Product> getAllProducts(){
        return productService.getAllProducts();
    }
    @GetMapping("/products/{id}")
    public Product getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    /* UPDATE */
    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id,
                                 @RequestBody ProductRequest request){
        return productService.updateProduct(id, request);
    }

    /* DELETE */
    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
    }

}
