package com.nathanaeldorsey.stormmarket.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for order event (audit) entries.
 * Append-only audit log: entries are never modified or deleted.
 */
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    /**
     * Finds all audit events for a given order, ordered by change time descending.
     *
     * @param orderId the order ID
     * @return list of order events, newest first
     */
    List<OrderEvent> findByOrderIdOrderByChangedAtDesc(Long orderId);
}
