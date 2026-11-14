---
story_id: "e03s01"
epic_id: "e03"
title: "CartService mutations return Result<Void, CartError>"
status: "pending"
estimate: "L"
---

## Story: CartService mutations return Result<Void, CartError>

**Vertical Slice:** Converts all 6 CartService mutation methods to return `Result<Void, CartError>` instead of throwing `IllegalArgumentException`/`IllegalStateException`/`ResourceNotFoundException` for expected business rule violations. ResourceNotFoundException for user/cart not found remains thrown (out of scope per SCOPE — repository finders keep Optional/null/exceptions).

### Acceptance Criteria
- [ ] `addProductToCart` returns `Result<Void, CartError>`:
  - `Err(CartError.INVALID_QUANTITY)` for null/<=0/>100 quantity
  - `Err(CartError.PRODUCT_NOT_FOUND)` for missing product
  - `Err(CartError.PRODUCT_INACTIVE)` for inactive product
  - `Err(CartError.INSUFFICIENT_STOCK)` for quantity > available stock
  - `Err(CartError.CART_FULL)` for >50 distinct items
  - `Ok(null)` on success
- [ ] `updateProductQuantity` returns `Result<Void, CartError>`:
  - `Err(CartError.INVALID_QUANTITY)` for invalid quantity
  - `Err(CartError.ITEM_NOT_IN_CART)` for product not in cart
  - `Err(CartError.PRODUCT_INACTIVE)` for inactive product
  - `Err(CartError.INSUFFICIENT_STOCK)` for quantity > stock
  - `Ok(null)` on success
- [ ] `removeProductFromCart` returns `Result<Void, CartError>`:
  - `Err(CartError.ITEM_NOT_IN_CART)` for product not in cart
  - `Ok(null)` on success
- [ ] `clearCart` returns `Result<Void, CartError>`:
  - `Ok(null)` on success (no failure modes)
- [ ] `applyCoupon` returns `Result<Void, CartError>`:
  - Delegates to `couponService.validate()` which returns `Result<Coupon, CouponValidationError>`
  - Maps `CouponValidationError` to appropriate `CartError` (NOT_FOUND->COUPON_NOT_FOUND, EXPIRED->COUPON_EXPIRED, USAGE_LIMIT_REACHED->COUPON_USAGE_LIMIT_REACHED, INACTIVE->COUPON_INACTIVE)
  - `Ok(null)` on success
- [ ] `removeCoupon` returns `Result<Void, CartError>`:
  - `Ok(null)` on success (no failure modes)
- [ ] `getCart` remains unchanged (returns `CartResponse`, not a mutation)
- [ ] Private helper methods (`validateQuantity`, `getUser`, `getOrCreateCart`, `getExistingCart`, `getCartItem`) updated to return/propagate Result where appropriate
- [ ] `@Transactional` boundaries preserved — rollback handled in Epic 7 via `setRollbackOnly()`

### Technical Notes
- `ResourceNotFoundException` for user not found / cart not found is **out of scope** — continues to be thrown (per SCOPE: "repository finders keep Optional/null returns")
- Only expected business rule violations (currently thrown as IllegalArgumentException/IllegalStateException) become Result errors
- Use `Result.err(CartError.XXX)` for failures, `Result.ok(null)` for success
- Coupon validation: `couponService.validate()` already returns `Result<Coupon, CouponValidationError>` — map its Err to CartError
- Preserve all existing logic, only change error signaling mechanism
- Import `Result`, `Ok`, `Err` from `common.exception`
- Import `CartError` from `cart` package

### Files to Modify
- `src/main/java/com/nathanaeldorsey/stormmarket/cart/CartService.java`

### Definition of Done
- Code compiles
- Follows project code style
- All 6 mutation methods return Result<Void, CartError>
- All existing business rule violations mapped to CartError constants
- ResourceNotFoundException for user/cart still thrown
- @Transactional annotations unchanged