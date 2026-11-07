package com.nathanaeldorsey.stormmarket.order;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Finds PENDING orders created before the given cutoff time, for expiration.
     *
     * @param status   the order status to filter by
     * @param cutoff   the cutoff time
     * @return list of expired orders
     */
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoff);

    /**
     * Checks if an order exists for the given user.
     *
     * @param orderId the order ID
     * @param userId the user ID
     * @return true if the order exists and belongs to the user
     */
    boolean existsByIdAndUserId(Long orderId, Long userId);

    /**
     * Finds PENDING orders created before the given cutoff time with pessimistic lock.
     * Used by the expiration job to prevent race conditions with payment capture.
     *
     * @param status   the order status to filter by
     * @param cutoff   the cutoff time
     * @return list of expired orders
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :cutoff")
    List<Order> findByStatusAndCreatedAtBeforeWithLock(@Param("status") OrderStatus status, @Param("cutoff") LocalDateTime cutoff);

    /**
     * Finds an order by ID with pessimistic write lock.
     * Used by payment capture to prevent race conditions with order cancellation.
     *
     * @param id the order ID
     * @return the locked order, or empty if not found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);
}
