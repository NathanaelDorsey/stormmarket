package com.nathanaeldorsey.stormmarket.mysql;

import static org.assertj.core.api.Assertions.assertThat;

import com.nathanaeldorsey.stormmarket.order.OrderItemRepository;
import com.nathanaeldorsey.stormmarket.product.*;
import com.nathanaeldorsey.stormmarket.user.Role;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

@SpringBootTest
@Testcontainers
@ActiveProfiles("mysql-integration")
class MySqlIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("stormmarket_test")
            .withUsername("stormmarket")
            .withPassword("stormmarket");

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

        productRepository.saveAllAndFlush(
                java.util.List.of(laptop, phone, inactiveLaptop));

        ProductSearchRequest request = new ProductSearchRequest();
        request.setName("Lap");
        request.setInStock(true);

        var result = productService.searchProducts(request, Pageable.unpaged());

        assertThat(result.getContent())
                .extracting(ProductResponse::getName)
                .containsExactly("Laptop Pro");
    }
}

