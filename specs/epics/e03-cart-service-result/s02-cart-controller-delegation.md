---
story_id: "e03s02"
epic_id: "e03"
title: "CartController delegates to ResultHttpMapper"
status: "pending"
estimate: "M"
depends_on: ["e06s01"]
---

## Story: CartController delegates to ResultHttpMapper

**Vertical Slice:** Updates CartController to call `ResultHttpMapper.toResponse(result)` for all mutation endpoints instead of relying on GlobalExceptionHandler to catch thrown exceptions. GET endpoints (getCart) remain unchanged as they don't return Result.

### Acceptance Criteria
- [ ] `addProductToCart` endpoint: calls `cartService.addProductToCart(...)`, passes result to `mapper.toResponse(result)`, returns `ResponseEntity<?>` 
- [ ] `updateProductQuantity` endpoint: same pattern
- [ ] `removeProductFromCart` endpoint: same pattern
- [ ] `clearCart` endpoint: same pattern (returns 204 on Ok, error response on Err)
- [ ] `applyCoupon` endpoint: same pattern
- [ ] `removeCoupon` endpoint: same pattern
- [ ] `getCart` endpoint: **unchanged** — returns `CartResponse` directly (not a Result)
- [ ] On `Ok`: returns appropriate success status (200 for get/update, 201 for add, 204 for delete/clear)
- [ ] On `Err`: `ResultHttpMapper` maps `CartError` to correct HTTP status (400 for INVALID_QUANTITY, 404 for NOT_FOUND, 409 for INSUFFICIENT_STOCK/CART_FULL, etc.)
- [ ] Controller no longer throws exceptions for expected failures — all handled via Result
- [ ] `ResourceNotFoundException` for user/cart not found still thrown (caught by GlobalExceptionHandler → 404)

### Technical Notes
- Inject `ResultHttpMapper` into CartController (created in Epic 6)
- Change return type from `CartResponse`/`void` to `ResponseEntity<?>`
- Use `mapper.toResponse(result)` which returns `ResponseEntity<ApiErrorResponse>` on Err or appropriate success response on Ok
- For void success (clearCart, removeProductFromCart): return 204 No Content
- For addProductToCart: return 201 Created with updated cart (or 200 if preferred consistency)
- Preserve existing `@Valid` and authentication logic
- Import `ResultHttpMapper` from `common.exception` (or wherever it's placed)

### Files to Modify
- `src/main/java/com/nathanaeldorsey/stormmarket/cart/CartController.java`

### Files Created in Dependency
- `src/main/java/com/nathanaeldorsey/stormmarket/common/exception/ResultHttpMapper.java` (Epic 6)

### Definition of Done
- Code compiles
- Follows project code style
- All 6 mutation endpoints use ResultHttpMapper
- HTTP response codes/bodies identical to current behavior
- getCart endpoint unchanged