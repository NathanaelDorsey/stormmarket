---
story_id: "e01s02"
epic_id: "e01"
title: "CartError, OrderError, PaymentError"
status: "pending"
estimate: "M"
---

## Story: CartError, OrderError, PaymentError

**Vertical Slice:** Creates the three remaining service-specific error types with static singleton instances covering all expected business rule failures in CartService, OrderService, and PaymentService.

### Acceptance Criteria
- [ ] `CartError` class created in `cart` package, extends `DomainError`
- [ ] `CartError` defines static singleton instances for:
  - `PRODUCT_NOT_FOUND` — product ID does not exist
  - `PRODUCT_INACTIVE` — product is not active
  - `INSUFFICIENT_STOCK` — requested quantity exceeds available stock
  - `INVALID_QUANTITY` — quantity null, <= 0, or > MAX_CART_ITEM_QUANTITY
  - `CART_FULL` — cart already holds MAX_DISTINCT_CART_ITEMS products
  - `CART_NOT_FOUND` — user has no cart
  - `ITEM_NOT_IN_CART` — product not found in cart
  - `COUPON_EXPIRED` — coupon has expired
  - `COUPON_USAGE_LIMIT_REACHED` — coupon usage limit reached
  - `COUPON_NOT_FOUND` — coupon code does not exist
  - `COUPON_INACTIVE` — coupon is not active
- [ ] `OrderError` class created in `order` package, extends `DomainError`
- [ ] `OrderError` defines static singleton instances for:
  - `ORDER_NOT_FOUND` — order ID does not exist
  - `EMPTY_CART` — cannot checkout with empty cart
  - `PRODUCT_INACTIVE` — product in cart/order is inactive
  - `INSUFFICIENT_STOCK` — insufficient stock for item
  - `INVALID_STATUS_TRANSITION` — requested status change not allowed
  - `PAYMENT_NOT_CAPTURED` — PENDING->CONFIRMED requires captured payment
  - `CANNOT_REFUND_STATUS` — DELIVERED/SHIPPED orders cannot be refunded
  - `GUEST_CHECKOUT_MISSING_REQUEST` — guest checkout without request body
- [ ] `PaymentError` class created in `payment` package, extends `DomainError`
- [ ] `PaymentError` defines static singleton instances for:
  - `PAYMENT_NOT_FOUND` — payment intent ID does not exist
  - `ACCESS_DENIED` — user does not own the payment/order
  - `INVALID_STATE_FOR_AUTHORIZE` — payment not in PENDING state
  - `INVALID_STATE_FOR_CAPTURE` — payment not in AUTHORIZED or order not PENDING
  - `INVALID_STATE_FOR_REFUND` — payment not CAPTURED or order SHIPPED/DELIVERED
  - `INVALID_STATE_FOR_FAIL` — payment in CAPTURED/REFUNDED state
  - `PAYMENT_ALREADY_EXISTS` — duplicate payment intent for order
- [ ] All error instances are thread-safe, zero-allocation singletons
- [ ] Error messages match existing exception messages for API contract compatibility

### Technical Notes
- Each error class in its service package (`cart`, `order`, `payment`)
- Follows exact same pattern as `StockError` and `CouponValidationError`
- Messages must match current `IllegalArgumentException`/`IllegalStateException` messages exactly
- `OrderError.INVALID_STATUS_TRANSITION` message format: "Cannot change order status from X to Y"

### Files to Create
- `src/main/java/com/nathanaeldorsey/stormmarket/cart/CartError.java` (new)
- `src/main/java/com/nathanaeldorsey/stormmarket/order/OrderError.java` (new)
- `src/main/java/com/nathanaeldorsey/stormmarket/payment/PaymentError.java` (new)

### Definition of Done
- Code compiles
- Follows project code style
- No new dependencies
- All error constants cover every current throw site in respective services