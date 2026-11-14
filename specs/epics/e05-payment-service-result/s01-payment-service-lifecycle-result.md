---
story_id: "e05s01"
epic_id: "e05"
title: "PaymentService lifecycle returns Result<Payment, PaymentError>"
status: "pending"
estimate: "L"
---

## Story: PaymentService lifecycle returns Result<Payment, PaymentError>

**Vertical Slice:** Converts all 5 PaymentService lifecycle methods to return `Result<Payment, PaymentError>` instead of throwing exceptions. `getPaymentByOrderId` remains unchanged (finder, returns Payment directly).

### Acceptance Criteria
- [ ] `createPaymentIntent` returns `Result<String, PaymentError>` (intent ID, not Payment):
  - `Err(PaymentError.PAYMENT_ALREADY_EXISTS)` if payment already exists for order
  - `Err(PaymentError.ORDER_NOT_FOUND)` if order not found (ResourceNotFoundException thrown by repository — catch and map)
  - `Ok(intentId)` on success
- [ ] `authorizePayment` returns `Result<Payment, PaymentError>`:
  - `Err(PaymentError.PAYMENT_NOT_FOUND)` if intentId not found
  - `Err(PaymentError.ACCESS_DENIED)` if user doesn't own payment
  - `Err(PaymentError.INVALID_STATE_FOR_AUTHORIZE)` if payment not PENDING
  - `Ok(payment)` on success
- [ ] `capturePayment` returns `Result<Payment, PaymentError>`:
  - `Err(PaymentError.PAYMENT_NOT_FOUND)` if intentId not found
  - `Err(PaymentError.ACCESS_DENIED)` if user doesn't own payment
  - `Err(PaymentError.INVALID_STATE_FOR_CAPTURE)` if payment not AUTHORIZED or order not PENDING
  - `Ok(payment)` on success (after calling orderService.completeOrderWithPayment which now returns Result)
- [ ] `refundPayment` returns `Result<Payment, PaymentError>`:
  - `Err(PaymentError.PAYMENT_NOT_FOUND)` if intentId not found
  - `Err(PaymentError.ACCESS_DENIED)` if user doesn't own payment
  - `Err(PaymentError.INVALID_STATE_FOR_REFUND)` if payment not CAPTURED or order SHIPPED/DELIVERED
  - `Ok(payment)` on success (after calling orderService.cancelOrderWithRefund which now returns Result)
- [ ] `failPayment` returns `Result<Payment, PaymentError>`:
  - `Err(PaymentError.PAYMENT_NOT_FOUND)` if intentId not found
  - `Err(PaymentError.ACCESS_DENIED)` if user doesn't own payment
  - `Err(PaymentError.INVALID_STATE_FOR_FAIL)` if payment in CAPTURED/REFUNDED state
  - `Ok(payment)` on success
- [ ] `getPaymentByOrderId` **unchanged** — returns `Payment` directly (finder)
- [ ] Private helper `incrementCouponUsage` unchanged
- [ ] `@Transactional` boundaries preserved — rollback handled in Epic 7 via `setRollbackOnly()`
- [ ] `ResourceNotFoundException` for order/payment not found caught and mapped to PaymentError (since repository findByIdWithLock throws)

### Technical Notes
- `createPaymentIntent` currently throws `ResourceNotFoundException` for order not found and `IllegalStateException` for duplicate payment — map to PaymentError
- `authorizePayment`, `capturePayment`, `refundPayment`, `failPayment` all use `findByIntentIdWithLock` which throws `ResourceNotFoundException` — catch and map to `PAYMENT_NOT_FOUND`
- `capturePayment` calls `orderService.completeOrderWithPayment` which now returns `Result<OrderResponse, OrderError>` — handle Result: on Ok continue, on Err map OrderError to PaymentError (likely `INVALID_STATE_FOR_CAPTURE` or `INSUFFICIENT_STOCK`)
- `refundPayment` calls `orderService.cancelOrderWithRefund` which now returns `Result<OrderResponse, OrderError>` — handle Result similarly
- `capturePayment` calls `incrementCouponUsage` on success — preserve this
- All methods use pessimistic locking (`findByIntentIdWithLock`, `findByIdWithLock`) — preserve locking behavior
- Since OrderService is now converted (Epic 4), PaymentService can properly handle Result from OrderService calls

### Files to Modify
- `src/main/java/com/nathanaeldorsey/stormmarket/payment/PaymentService.java`

### Definition of Done
- Code compiles
- Follows project code style
- All 5 lifecycle methods return appropriate Result type
- All business rule violations mapped to PaymentError constants
- ResourceNotFoundException caught and mapped
- OrderService Result returns handled correctly
- @Transactional annotations unchanged
- getPaymentByOrderId unchanged