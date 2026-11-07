package com.nathanaeldorsey.stormmarket.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for a payment: avoids circular references from Payment -> Order -> Payment.
 */
@Getter
@Setter
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private String intentId;
    private String provider;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long orderId;
}