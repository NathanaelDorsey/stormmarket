package com.nathanaeldorsey.stormmarket.payment;

import com.nathanaeldorsey.stormmarket.common.exception.DomainError;

/**
 * Expected failures for payment operations.
 * All instances are static singletons for zero-allocation hot paths.
 */
public class PaymentError extends DomainError {

    public static final PaymentError OWNERSHIP_MISMATCH = new PaymentError("Payment not found or access denied");
    public static final PaymentError INVALID_STATE_TRANSITION = new PaymentError("Invalid payment state transition");
    public static final PaymentError PAYMENT_NOT_FOUND = new PaymentError("Payment not found");
    public static final PaymentError ORDER_NOT_PENDING = new PaymentError("Order is not in a pending state");
    public static final PaymentError CANNOT_REFUND_SHIPPED = new PaymentError("Cannot refund shipped or delivered order without return authorization");
    public static final PaymentError PAYMENT_ALREADY_CAPTURED = new PaymentError("Payment already captured");
    public static final PaymentError PAYMENT_ALREADY_REFUNDED = new PaymentError("Payment already refunded");

    private PaymentError(String message) {
        super(message);
    }
}