package com.nathanaeldorsey.stormmarket.product;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.web
                    .OAuth2ResourceServerWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void getAllProductsReturnsOkAndProductList() throws Exception {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("A product for testing");
        product.setPrice(new BigDecimal("19.99"));
        product.setStockQuantity(5);
        product.setImageUrl("https://example.com/product.jpg");
        product.setCategoryIds(Set.of(2L));
        product.setActive(true);

        when(productService.getAllProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].description").value("A product for testing"))
                .andExpect(jsonPath("$[0].price").value(19.99))
                .andExpect(jsonPath("$[0].stockQuantity").value(5))
                .andExpect(jsonPath("$[0].imageUrl").value("https://example.com/product.jpg"))
                .andExpect(jsonPath("$[0].categoryIds[0]").value(2))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getProductByIdReturnsOkAndProduct() throws Exception {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("A product for testing");
        product.setPrice(new BigDecimal("19.99"));
        product.setStockQuantity(5);
        product.setImageUrl("https://example.com/product.jpg");
        product.setCategoryIds(Set.of(2L));
        product.setActive(true);

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(19.99));
    }

    @Test
    void getProductByIdWhenProductDoesNotExistReturnsNotFound() throws Exception {
        when(productService.getProductById(999L)).thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found"))
                .andExpect(jsonPath("$.path").value("/products/999"));
    }

    @Test
    void searchProductsReturnsOkAndPaginatedResults() throws Exception {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Mechanical Keyboard");
        product.setPrice(new BigDecimal("49.99"));
        product.setStockQuantity(8);
        product.setCategoryIds(Set.of(2L));
        product.setActive(true);

        Page<ProductResponse> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(productService.searchProducts(
                        org.mockito.ArgumentMatchers.any(ProductSearchRequest.class),
                        org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/products/search")
                        .param("name", "Keyboard")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.content[0].price").value(49.99))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void searchProductsWithInvalidMinPriceReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/products/search").param("minPrice", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("One or more fields have invalid values"))
                .andExpect(jsonPath("$.validationErrors.minPrice").exists())
                .andExpect(jsonPath("$.path").value("/products/search"));
    }
}
