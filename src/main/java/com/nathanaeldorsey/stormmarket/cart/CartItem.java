package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints =
                @UniqueConstraint( // 1 product per cart (increment qty)
                        columnNames = {"cart_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY) // dont auto fetch all carts
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY) // dont auto fetch all products
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;
}
