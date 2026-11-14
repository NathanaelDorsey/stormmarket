package com.nathanaeldorsey.stormmarket.payment;

import com.nathanaeldorsey.stormmarket.common.exception.DomainError;
import com.nathanaeldorsey.stormmarket.common.exception.Result;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentErrorTest {

    @Test
    void paymentErrorExtendsDomainError() {
        assertThat(PaymentError.OWNERSHIP_MISMATCH).isInstanceOf(DomainError.class);
    }

    @Test
    void paymentErrorConstantsAreSingletons() {
        assertThat(PaymentError.OWNERSHIP_MISMATCH).isSameAs(PaymentError.OWNERSHIP_MISMATCH);
    }

    @Test
    void paymentErrorHasCorrectMessages() {
        assertThat(PaymentError.OWNERSHIP_MISMATCH.getMessage()).isEqualTo("Payment not found or access denied");
        assertThat(PaymentError.INVALID_STATE_TRANSITION.getMessage()).isEqualTo("Invalid payment state transition");
        assertThat(PaymentError.PAYMENT_NOT_FOUND.getMessage()).isEqualTo("Payment not found");
        assertThat(PaymentError.ORDER_NOT_PENDING.getMessage()).isEqualTo("Order is not in a pending state");
        assertThat(PaymentError.CANNOT_REFUND_SHIPPED.getMessage()).isEqualTo("Cannot refund shipped or delivered order without return authorization");
        assertThat(PaymentError.PAYMENT_ALREADY_CAPTURED.getMessage()).isEqualTo("Payment already captured");
        assertThat(PaymentError.PAYMENT_ALREADY_REFUNDED.getMessage()).isEqualTo("Payment already refunded");
    }

    @Test
    void paymentErrorCanBeUsedInResult() {
        Result<Void, PaymentError> result = Result.err(PaymentError.OWNERSHIP_MISMATCH);
        assertThat(result.isErr()).isTrue();
    }

    @Test
    void paymentErrorConstantsAreNotNull() {
        assertThat(PaymentError.OWNERSHIP_MISMATCH).isNotNull();
        assertThat(PaymentError.INVALID_STATE_TRANSITION).isNotNull();
        assertThat(PaymentError.PAYMENT_NOT_FOUND).isNotNull();
        assertThat(PaymentError.ORDER_NOT_PENDING).isNotNull();
        assertThat(PaymentError.CANNOT_REFUND_SHIPPED).isNotNull();
        assertThat(PaymentError.PAYMENT_ALREADY_CAPTURED).isNotNull();
        assertThat(PaymentError.PAYMENT_ALREADY_REFUNDED).isNotNull();
    }
}