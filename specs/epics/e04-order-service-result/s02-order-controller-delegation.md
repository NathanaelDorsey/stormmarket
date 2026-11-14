---
story_id: "e04s02"
epic_id: "e04"
title: "OrderController delegates to ResultHttpMapper"
status: "pending"
estimate: "M"
depends_on: ["e06s01"]
---

## Story: OrderController delegates to ResultHttpMapper

**Vertical Slice:** Updates OrderController to call `ResultHttpMapper.toResponse(result)` for all transition endpoints (placeOrder, updateOrderStatus). Read-only endpoints (getOrderHistory, getOrderDetails, getOrdersForAdmin, getOrderDetailsForAdmin) remain unchanged.

### Acceptance Criteria
- [ ] `placeOrder` endpoint: calls `orderService.placeOrder(...)`, passes result to `mapper.toResponse(result)`, returns `ResponseEntity<?>`
- [ ] `updateOrderStatus` endpoint (admin): same pattern
- [ ] `getOrderHistory`, `getOrderDetails`, `getOrdersForAdmin`, `getOrderDetailsForAdmin`: **unchanged** — return data directly
- [ ] On `Ok`: returns 201 Created for placeOrder, 200 OK for updateOrderStatus with OrderResponse body
- [ ] On `Err`: `ResultHttpMapper` maps `OrderError` to correct HTTP status (400 for EMPTY_CART/GUEST_CHECKOUT_MISSING_REQUEST, 404 for ORDER_NOT_FOUND, 409 for INSUFFICIENT_STOCK/INVALID_STATUS_TRANSITION/PAYMENT_NOT_CAPTURED/CANNOT_REFUND_STATUS/PRODUCT_INACTIVE)
- [ ] Controller no longer throws exceptions for expected failures — all handled via Result
- [ ] `ResourceNotFoundException` for user/order not found still thrown (caught by GlobalExceptionHandler → 404)
- [ ] Metrics instrumentation (`metrics.incrementOrdersCreated`, `metrics.incrementOrdersFailed`, `metrics.recordOrderProcessingTime`) preserved and called appropriately based on Result

### Technical Notes
- Inject `ResultHttpMapper` into OrderController
- Change `placeOrder` return type from `ResponseEntity<OrderResponse>` to `ResponseEntity<?>`
- Change `updateOrderStatus` return type from `OrderResponse` to `ResponseEntity<?>`
- For `placeOrder`: preserve Timer.Sample metrics — start timer, on Ok incrementOrdersCreated, on Err incrementOrdersFailed, finally recordOrderProcessingTime
- For `updateOrderStatus`: no metrics currently, but preserve any future needs
- Use `mapper.toResponse(result)` which handles success/error mapping
- Preserve `@PreAuthorize("hasRole('ADMIN')")` on admin endpoints
- Preserve authentication/user lookup logic (throws ResourceNotFoundException if user not found — out of scope)

### Files to Modify
- `src/main/java/com/nathanaeldorsey/stormmarket/order/OrderController.java`

### Files Created in Dependency
- `src/main/java/com/nathanaeldorsey/stormmarket/common/exception/ResultHttpMapper.java` (Epic 6)

### Definition of Done
- Code compiles
- Follows project code style
- placeOrder and updateOrderStatus use ResultHttpMapper
- Read-only endpoints unchanged
- HTTP response codes/bodies identical to current behavior
- Metrics instrumentation preserved and correct