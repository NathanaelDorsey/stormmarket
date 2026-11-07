package com.nathanaeldorsey.stormmarket.cart;

import com.nathanaeldorsey.stormmarket.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
/**
 * Represents a user's shopping cart. One-to-one with a user, holding a list of cart items.
 * Provides addItem to maintain the bidirectional cart-item relationship.
 *
 * An optional coupon code may be applied to the cart, reducing the cart total by
 * the coupon's discount amount at checkout time.
 */
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    // don't autoload user details when loading a cart
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    // every cart must have an owner and each owner can only have 1 cart
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    // operations on cart also apply to items, removing an item deletes its row in database
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    // updates cart and items when item added to cart maintaining synchronized relationship
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }
}
