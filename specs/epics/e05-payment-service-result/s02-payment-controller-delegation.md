---
story_id: "e05s02"
epic_id: "e05"
title: "PaymentController delegates to ResultHttpMapper"
status: "pending"
estimate: "M"
depends_on: ["e06s01"]
---

## Story: PaymentController delegates to ResultHttpMapper

**Vertical Slice:** Updates PaymentController to call `ResultHttpMapper.toResponse(result)` for all lifecycle endpoints (createPaymentIntent, authorizePayment, capturePayment, refundPayment, failPayment). Read-only endpoint (getPaymentByOrderId) remains unchanged.

### Acceptance Criteria
- [ ] `createPaymentIntent` endpoint: calls `paymentService.createPaymentIntent(...)`, passes result to `mapper.toResponse(result)`, returns `ResponseEntity<?>`
- [ ] `authorizePayment` endpoint: same pattern
- [ ] `capturePayment` endpoint: same pattern
- [ ] `refundPayment` endpoint: same pattern
- [ ] `failPayment` endpoint: same pattern
- [ ] `getPaymentByOrderId` endpoint: **unchanged** — returns `ResponseEntity<PaymentResponse>` directly
- [ ] On `Ok`: returns 200 OK with appropriate body (String for intentId, PaymentResponse for others)
- [ ] On `Err`: `ResultHttpMapper` maps `PaymentError` to correct HTTP status (400 for PAYMENT_ALREADY_EXISTS, 404 for PAYMENT_NOT_FOUND/ORDER_NOT_FOUND, 403 for ACCESS_DENIED, 409 for INVALID_STATE_FOR_*)
- [ ] Controller no longer throws exceptions for expected failures — all handled via Result
- [ ] `ResourceNotFoundException` for order/payment not found caught by GlobalExceptionHandler → 404 (but PaymentService now maps these, so shouldn't reach controller)
- [ ] Authentication/user lookup and order ownership checks preserved (currently in controller private methods)

### Technical Notes
- Inject `ResultHttpMapper` into PaymentController
- Change all mutation endpoint return types from `ResponseEntity<PaymentResponse>`/`ResponseEntity<String>` to `ResponseEntity<?>`
- For `createPaymentIntent`: on Ok, return 200 OK with intentId String body (current behavior)
- For others: on Ok, return 200 OK with PaymentResponse body (current behavior)
- Preserve `getCurrentUser` and `checkOrderOwnership` private methods — they throw `IllegalArgumentException` for user not found / access denied — these are **auth/authorization** failures, not business rule violations. Per SCOPE, GlobalExceptionHandler handles these (401/403). Keep as-is.
- Actually, `checkOrderOwnership` throws `IllegalArgumentException` — GlobalExceptionHandler maps to 400. But this is authorization (403). The current handler maps IllegalArgumentException to 400. This is existing behavior — preserve it.
- The `ResultHttpMapper` only handles service-layer `PaymentError` (business rule violations). Controller-layer auth checks remain as thrown exceptions.

### Files to Modify
- `src/main/java/com/nathanaeldorsey/stormmarket/payment/PaymentController.java`

### Files Created in Dependency
- `src/main/java/com/nathanaeldorsey/stormmarket/common/exception/ResultHttpMapper.java` (Epic 6)

### Definition of Done
- Code compiles
- Follows project code style
- All 5 lifecycle endpoints use ResultHttpMapper
- getPaymentByOrderId unchanged
- HTTP response codes/bodies identical to current behavior
- Controller auth/ownership checks preserved