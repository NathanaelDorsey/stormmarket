---
story_id: "e04s01"
epic_id: "e04"
title: "OrderService transitions return Result<OrderResponse, OrderError>"
status: "pending"
estimate: "L"
---

## Story: OrderService transitions return Result<OrderResponse, OrderError>

**Vertical Slice:** Converts all 4 OrderService transition methods plus the scheduled `expirePendingOrders` to return `Result<OrderResponse, OrderError>` instead of throwing exceptions. `getOrderHistory`, `getOrdersForAdmin`, `getOrderDetails`, `getOrderDetailsForAdmin` remain unchanged (read-only, return data directly).

### Acceptance Criteria
- [ ] `placeOrder` returns `Result<OrderResponse, OrderError>`:
  - `Err(OrderError.EMPTY_CART)` for empty cart
  - `Err(OrderError.PRODUCT_INACTIVE)` for inactive product in cart
  - `Err(OrderError.INSUFFICIENT_STOCK)` for insufficient stock (both check and reserveStock failure)
  - `Err(OrderError.GUEST_CHECKOUT_MISSING_REQUEST)` for guest without request
  - `Ok(orderResponse)` on success
- [ ] `completeOrderWithPayment` returns `Result<OrderResponse, OrderError>`:
  - `Err(OrderError.ORDER_NOT_FOUND)` for missing order
  - `Err(OrderError.INVALID_STATUS_FOR_CAPTURE)` for order not PENDING (rename from current message)
  - `Err(OrderError.PRODUCT_INACTIVE)` for inactive product
  - `Err(OrderError.INSUFFICIENT_STOCK)` for captureStock/deductStock failure
  - `Ok(orderResponse)` on success
- [ ] `cancelOrderWithRefund` returns `Result<OrderResponse, OrderError>`:
  - `Err(OrderError.ORDER_NOT_FOUND)` for missing order
  - `Err(OrderError.CANNOT_REFUND_STATUS)` for DELIVERED/SHIPPED orders
  - `Ok(orderResponse)` on success (including already-canceled idempotent case)
- [ ] `updateOrderStatus` returns `Result<OrderResponse, OrderError>`:
  - `Err(OrderError.ORDER_NOT_FOUND)` for missing order
  - `Err(OrderError.INVALID_STATUS_TRANSITION)` for invalid transition
  - `Err(OrderError.PAYMENT_NOT_CAPTURED)` for PENDING->CONFIRMED without captured payment
  - `Ok(orderResponse)` on success
- [ ] `expirePendingOrders` (scheduled) returns `void` but internally handles Result from stock operations:
  - Calls `restoreStock`/`releaseReservedStock` which return `Result<Void, StockError>`
  - On Err, logs error but continues expiring other orders (scheduled job semantics)
  - Does not throw — scheduled job must not fail entirely
- [ ] Private helpers (`getUserForCheckout`, `getCartForCheckout`, `validateCart`, `createOrder`, `createGuestOrder`) updated to propagate Result where appropriate
- [ ] `@Transactional` boundaries preserved — rollback handled in Epic 7 via `setRollbackOnly()`
- [ ] `ResourceNotFoundException` for user/order not found still thrown (out of scope per SCOPE)

### Technical Notes
- `placeOrder` currently calls `productRepository.reserveStock` which now returns `Result<Integer, StockError>` — must handle and map to `OrderError.INSUFFICIENT_STOCK`
- `completeOrderWithPayment` calls `productRepository.captureStock` then `deductStock` — both return `Result<Integer, StockError>` — map failures to `OrderError.INSUFFICIENT_STOCK`
- `cancelOrderWithRefund` calls `productRepository.restoreStock` and `releaseReservedStock` — return `Result<Void, StockError>` — on Err, log but continue (rollback handled separately)
- `updateOrderStatus` calls `productRepository.restoreStock`/`releaseReservedStock` on cancel/expire — same pattern
- Coupon validation via `couponService.validate()` returns `Result<Coupon, CouponValidationError>` — already returns Result, just use it
- PaymentService calls (`createPaymentIntent`, `completeOrderWithPayment`, `cancelOrderWithRefund`) currently throw — will be converted in Epic 5, so for now catch exceptions and map to OrderError, OR defer PaymentService integration to after Epic 5
- **Important**: Since PaymentService is not yet converted, `placeOrder` calling `paymentService.createPaymentIntent` which throws — wrap in try/catch and map to OrderError for now. Same for `completeOrderWithPayment` calling `orderService.completeOrderWithPayment` (circular) — actually PaymentService calls OrderService, so OrderService doesn't call PaymentService for completion. But `placeOrder` calls `paymentService.createPaymentIntent`.

### Files to Modify
- `src/main/java/com/nathanaeldorsey/stormmarket/order/OrderService.java`

### Definition of Done
- Code compiles
- Follows project code style
- All 4 transition methods return Result<OrderResponse, OrderError>
- All business rule violations mapped to OrderError constants
- ResourceNotFoundException for user/order still thrown
- @Transactional annotations unchanged
- expirePendingOrders handles Result from stock methods without throwing