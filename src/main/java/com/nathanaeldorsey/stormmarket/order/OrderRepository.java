package com.nathanaeldorsey.stormmarket.order;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for order entities. Provides standard CRUD plus lookups by user (ordered
 * by creation descending), all orders paginated, and orders filtered by status.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds all orders for a user, newest first.
     *
     * @param userId the user whose orders to find
     * @return list of orders
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds all orders paginated, newest first.
     *
     * @param pageable the pagination request
     * @return page of orders
     */
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Finds orders by status, paginated, newest first.
     *
     * @param status   the order status to filter by
     * @param pageable the pagination request
     * @return page of orders
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);
}
