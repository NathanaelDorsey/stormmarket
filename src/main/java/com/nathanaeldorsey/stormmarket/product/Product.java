package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.category.Category;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
/**
 * Represents a product in the catalog. Holds name, description, price, stock quantity,
 * active flag, image URL, and a many-to-many set of categories.
 *
 * Provides bidirectional category management via addCategory and removeCategory.
 */
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Category information
    @ManyToMany // Product can have many Categories/Categories can have many Products
    @JoinTable( // The relationship is stored in product_categories table
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"), // Product Column
            inverseJoinColumns = @JoinColumn(name = "category_id") // Category Column
            )
    private Set<Category> categories = new HashSet<>();

    public void addCategory(Category category) {
        categories.add(category);
        category.getProducts().add(this);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
        category.getProducts().remove(this);
    }
}
