package com.nathanaeldorsey.stormmarket.category;

import com.nathanaeldorsey.stormmarket.product.Product;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "categories") // Product already defined/mapped relationship to Set<>categories
    private Set<Product> products = new HashSet<>();
}
