package com.nathanaeldorsey.stormmarket.payment;

/**
 * Lifecycle states of a payment.
 *
 * Flow: PENDING -> AUTHORIZED -> CAPTURED -> REFUNDED,
 * with FAILED as a terminal state reachable from PENDING or AUTHORIZED.
 */
public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    CAPTURED,
    REFUNDED,
    FAILED
}
