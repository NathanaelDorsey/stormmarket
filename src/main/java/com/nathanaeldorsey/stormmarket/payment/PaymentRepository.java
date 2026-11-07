package com.nathanaeldorsey.stormmarket.payment;

import com.nathanaeldorsey.stormmarket.order.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Payment entities.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds a payment by its associated order ID.
     *
     * @param orderId the order ID
     * @return the payment, or empty if not found
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Finds a payment by its intent ID.
     *
     * @param intentId the payment intent ID
     * @return the payment, or empty if not found
     */
    Optional<Payment> findByIntentId(String intentId);

    /**
     * Finds a payment by its intent ID with pessimistic lock.
     * Used to prevent concurrent capture/refund operations.
     *
     * @param intentId the payment intent ID
     * @return the payment, or empty if not found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.intentId = :intentId")
    Optional<Payment> findByIntentIdWithLock(@Param("intentId") String intentId);
}
