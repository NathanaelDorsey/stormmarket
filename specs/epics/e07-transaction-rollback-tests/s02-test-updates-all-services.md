---
story_id: "e07s02"
epic_id: "e07"
title: "Update all 175 tests to expect Result instead of exceptions"
status: "pending"
estimate: "XL"
---

## Story: Update all 175 tests to expect Result instead of exceptions

**Vertical Slice:** Updates all existing unit and integration tests across ProductRepository, CartService, OrderService, PaymentService, and their controllers to assert on `Result` return values instead of expecting thrown exceptions for business rule violations.

### Acceptance Criteria
- [ ] ProductRepository tests: assert `Result<Integer, StockError>` — `isOk()` for success, `isErr()` with `StockError.XXX` for failures
- [ ] CartService tests: assert `Result<Void, CartError>` for all 6 mutation methods
- [ ] CartController tests: assert `ResponseEntity` from mapper — status codes and body match
- [ ] OrderService tests: assert `Result<OrderResponse, OrderError>` for all 4 transition methods
- [ ] OrderController tests: assert `ResponseEntity` from mapper for placeOrder, updateOrderStatus
- [ ] PaymentService tests: assert `Result<Payment, PaymentError>` / `Result<String, PaymentError>` for all 5 lifecycle methods
- [ ] PaymentController tests: assert `ResponseEntity` from mapper for all 5 lifecycle endpoints
- [ ] All 175 tests pass with updated assertions
- [ ] No test logic changes beyond assertion style — same scenarios, same expected outcomes
- [ ] JaCoCo coverage maintained (64% instruction / 51% branch / 51% line / 62% method / 85% class)

### Technical Notes
- Test files likely in `src/test/java/com/nathanaeldorsey/stormmarket/{product,cart,order,payment}/`
- Pattern change: `assertThrows(IllegalArgumentException.class, ...)` → `assertTrue(result.isErr()); assertEquals(CartError.INVALID_QUANTITY, ((Err<?, ?>) result).error())`
- Pattern change: `assertThrows(IllegalStateException.class, ...)` → `assertTrue(result.isErr()); assertEquals(OrderError.INSUFFICIENT_STOCK, ...)`
- For controller tests: `mockMvc.perform(...).andExpect(status().isConflict())` — unchanged, but now the path goes through ResultHttpMapper instead of GlobalExceptionHandler
- CouponService tests already use Result — no changes needed
- ResourceNotFoundException tests (user/cart/order/payment not found) — unchanged, still expect exception/thrown → 404 via GlobalExceptionHandler
- Authentication/authorization tests — unchanged

### Files to Modify
- All test files under `src/test/java/com/nathanaeldorsey/stormmarket/` for the 4 services and controllers

### Definition of Done
- All 175 tests pass (`mvn test`)
- JaCoCo coverage maintained
- SpotBugs/FindSecBugs: 0 issues
- Spotless compliant
- Postman collection: 21 requests, 67 assertions, 0 failures