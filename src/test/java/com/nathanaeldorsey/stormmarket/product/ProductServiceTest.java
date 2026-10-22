package com.nathanaeldorsey.stormmarket.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nathanaeldorsey.stormmarket.cart.CartItemRepository;
import com.nathanaeldorsey.stormmarket.category.CategoryRepository;
import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.order.OrderItemRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(
                productRepository, categoryRepository, productMapper, orderItemRepository, cartItemRepository);
    }

    @Test
    void getAllProductsReturnsOnlyActiveProducts() {
        Product activeProduct = new Product();
        activeProduct.setId(1L);
        activeProduct.setName("Widget");
        activeProduct.setActive(true);
        Product inactiveProduct = new Product();
        inactiveProduct.setId(2L);
        inactiveProduct.setName("Hidden");
        inactiveProduct.setActive(false);

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Widget");

        when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(activeProduct));
        when(productMapper.toResponse(activeProduct)).thenReturn(response);

        List<ProductResponse> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Widget", result.get(0).getName());
    }

    @Test
    void getProductByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void deleteProductRejectsProductsThatAppearInOrderHistory() {
        Product product = new Product();
        product.setId(7L);

        when(productRepository.findById(7L)).thenReturn(java.util.Optional.of(product));
        when(orderItemRepository.existsByProductId(7L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> productService.deleteProduct(7L));
        verify(cartItemRepository, never()).deleteByProductId(anyLong());
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void searchProductsReturnsPageOfMappedResponses() {
        Product product = new Product();
        product.setId(3L);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setActive(true);

        ProductResponse response = new ProductResponse();
        response.setId(3L);
        response.setName("Laptop");

        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductSearchRequest request = new ProductSearchRequest();
        request.setName("Lap");

        var page = productService.searchProducts(request, Pageable.unpaged());

        assertEquals(1, page.getTotalElements());
        assertEquals("Laptop", page.getContent().get(0).getName());
    }
}
