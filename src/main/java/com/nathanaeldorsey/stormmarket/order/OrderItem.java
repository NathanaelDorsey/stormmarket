package com.nathanaeldorsey.stormmarket.order;

import com.nathanaeldorsey.stormmarket.product.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String productName; // Storing current name in case of updates to product

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // Storing current price in case of updates to product

    @Column(nullable = false)
    private Integer quantity;
}
