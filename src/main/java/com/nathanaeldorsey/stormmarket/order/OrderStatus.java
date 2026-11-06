package com.nathanaeldorsey.stormmarket.order;

/**
 * Lifecycle states of an order. Transitions follow: PENDING -> CONFIRMED -> SHIPPED -> DELIVERED,
 * with CANCELED reachable from PENDING, CONFIRMED, or SHIPPED.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELED
}
