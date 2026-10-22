package com.nathanaeldorsey.stormmarket.mysql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nathanaeldorsey.stormmarket.category.Category;
import com.nathanaeldorsey.stormmarket.category.CategoryRepository;
import com.nathanaeldorsey.stormmarket.order.Order;
import com.nathanaeldorsey.stormmarket.order.OrderItem;
import com.nathanaeldorsey.stormmarket.order.OrderRepository;
import com.nathanaeldorsey.stormmarket.product.*;
import com.nathanaeldorsey.stormmarket.user.Role;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("mysql-integration")
class MySqlIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("stormmarket_test")
            .withUsername("stormmarket")
            .withPassword("stormmarket");

    @Autowired
    private CategoryRepository categoryRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void mysqlContainerStartsAndJpaPersistsUsers() {
        User user = new User();
        user.setFirstName("Mary");
        user.setLastName("Jane");
        user.setUsername("maryjane");
        user.setPassword("secret");
        user.setEmail("mary@example.com");
        user.setRole(Role.USER);

        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findByUsername("maryjane")).isPresent();
    }

    @Test
    void productSearchReturnsOnlyActiveMatchingProductsInStock() {
        Product laptop = new Product();
        laptop.setName("Laptop Pro");
        laptop.setPrice(new BigDecimal("999.99"));
        laptop.setStockQuantity(5);
        laptop.setActive(true);

        Product phone = new Product();
        phone.setName("Smartphone");
        phone.setPrice(new BigDecimal("499.99"));
        phone.setStockQuantity(8);
        phone.setActive(true);

        Product inactiveLaptop = new Product();
        inactiveLaptop.setName("Laptop Hidden");
        inactiveLaptop.setPrice(new BigDecimal("799.99"));
        inactiveLaptop.setStockQuantity(3);
        inactiveLaptop.setActive(false);

        productRepository.saveAllAndFlush(java.util.List.of(laptop, phone, inactiveLaptop));

        ProductSearchRequest request = new ProductSearchRequest();
        request.setName("Lap");
        request.setInStock(true);

        var result = productService.searchProducts(request, Pageable.unpaged());

        assertThat(result.getContent()).extracting(ProductResponse::getName).containsExactly("Laptop Pro");
    }

    @Test
    @Transactional
    void addCategoryPersistsProductCategoryRelationship() {
        Category category = new Category();
        category.setName("Integration Test Category");
        Category savedCategory = categoryRepository.saveAndFlush(category);

        Product product = new Product();
        product.setName("Integration Test Product");
        product.setPrice(new BigDecimal("49.99"));
        product.setStockQuantity(10);
        product.setActive(true);
        Product savedProduct = productRepository.saveAndFlush(product);

        productService.addCategory(savedProduct.getId(), savedCategory.getId());

        // Reload the product so we verify what MySQL persisted.
        productRepository.flush();
        entityManager.clear();

        Product reloadedProduct =
                productRepository.findById(savedProduct.getId()).orElseThrow();

        assertThat(reloadedProduct.getCategories()).extracting(Category::getId).contains(savedCategory.getId());
    }

    @Test
    @Transactional
    void removeCategoryPersistsRelationshipRemoval() {
        Category category = new Category();
        category.setName("Category To Remove");
        Category savedCategory = categoryRepository.saveAndFlush(category);

        Product product = new Product();
        product.setName("Product For Removal Test");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(5);
        product.setActive(true);
        Product savedProduct = productRepository.saveAndFlush(product);

        // Establish the relationship first.
        productService.addCategory(savedProduct.getId(), savedCategory.getId());

        // Remove the relationship through the service.
        productService.removeCategory(savedProduct.getId(), savedCategory.getId());

        // Clear cached entities and reload from MySQL.
        entityManager.flush();
        entityManager.clear();

        Product reloadedProduct =
                productRepository.findById(savedProduct.getId()).orElseThrow();

        Category reloadedCategory =
                categoryRepository.findById(savedCategory.getId()).orElseThrow();

        assertThat(reloadedProduct.getCategories()).isEmpty();

        assertThat(reloadedCategory.getId()).isEqualTo(savedCategory.getId());
    }

    private void deleteProduct(Long productId) {
        productService.deleteProduct(productId);
    }

    @Test
    @Transactional
    void deletingProductInOrderHistoryIsRejected() {
        // Create and persist a user for the order.
        User user = new User();
        user.setFirstName("Order");
        user.setLastName("History");
        user.setUsername("orderhistoryuser");
        user.setEmail("orderhistory@example.com");
        user.setPassword("encoded-password");
        User savedUser = userRepository.saveAndFlush(user);

        // Create and persist the product.
        Product product = new Product();
        product.setName("Product With Order History");
        product.setPrice(new BigDecimal("39.99"));
        product.setStockQuantity(5);
        product.setActive(true);
        Product savedProduct = productRepository.saveAndFlush(product);

        // Create an order that references the product.
        Order order = new Order();
        order.setUser(savedUser);
        order.setTotal(new BigDecimal("39.99"));

        OrderItem item = new OrderItem();
        item.setProduct(savedProduct);
        item.setProductName(savedProduct.getName());
        item.setPrice(savedProduct.getPrice());
        item.setQuantity(1);
        order.addItem(item);

        orderRepository.saveAndFlush(order);

        // Verify that deletion is rejected.
        Long productId = savedProduct.getId();

        assertThatThrownBy(() -> deleteProduct(productId)).isInstanceOf(IllegalStateException.class);

        // Verify that the product still exists.
        assertThat(productRepository.findById(savedProduct.getId())).isPresent();
    }
}
