package com.nathanaeldorsey.stormmarket.order;

import com.nathanaeldorsey.stormmarket.payment.Payment;
import com.nathanaeldorsey.stormmarket.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
/**
 * Represents a customer order. Holds the owning user, total amount, creation timestamp,
 * order status, and a list of order items.
 *
 * The creation timestamp is set to UTC on persist via onCreate. Provides addItem to
 * maintain the bidirectional order-item relationship.
 * Stock is deducted only when payment is captured (tracked via stockDeducted flag).
 */
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShippingAddress shippingAddress;

    @Column(name = "stock_deducted", nullable = false)
    private boolean stockDeducted = false;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    @PrePersist // call before object persists in database
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.of("UTC"));
    }
}
