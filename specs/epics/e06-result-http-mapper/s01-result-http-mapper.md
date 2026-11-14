---
story_id: "e06s01"
epic_id: "e06"
title: "ResultHttpMapper component for centralized error→HTTP mapping"
status: "pending"
estimate: "L"
---

## Story: ResultHttpMapper component for centralized error→HTTP mapping

**Vertical Slice:** Creates a new Spring `@Component` `ResultHttpMapper` in `common.exception` package that converts `Result<?, ? extends DomainError>` into `ResponseEntity<?>`. Uses exhaustive switch on error type to map each error constant to the correct HTTP status and `ApiErrorResponse` body.

### Acceptance Criteria
- [ ] `ResultHttpMapper` class created in `common.exception` package, annotated with `@Component`
- [ ] Public method `toResponse(Result<?, ? extends DomainError> result)` returns `ResponseEntity<?>`
- [ ] On `Ok`: returns `ResponseEntity.ok(value)` for non-void, `ResponseEntity.noContent().build()` for void (null value)
- [ ] On `Err`: exhaustive switch on `error` (which is `DomainError` subtype) mapping each constant to HTTP status:
  - **StockError** (used internally by services, not directly by controllers):
    - `INSUFFICIENT_STOCK` → 409 Conflict
    - `PRODUCT_NOT_FOUND` → 404 Not Found
    - `PRODUCT_INACTIVE` → 409 Conflict
    - `RESERVATION_FAILED` → 409 Conflict
    - `CAPTURE_FAILED` → 409 Conflict
    - `DEDUCT_FAILED` → 409 Conflict
  - **CartError**:
    - `INVALID_QUANTITY` → 400 Bad Request
    - `PRODUCT_NOT_FOUND` → 404 Not Found
    - `PRODUCT_INACTIVE` → 409 Conflict
    - `INSUFFICIENT_STOCK` → 409 Conflict
    - `CART_FULL` → 409 Conflict
    - `CART_NOT_FOUND` → 404 Not Found
    - `ITEM_NOT_IN_CART` → 404 Not Found
    - `COUPON_EXPIRED` → 400 Bad Request (or 409)
    - `COUPON_USAGE_LIMIT_REACHED` → 409 Conflict
    - `COUPON_NOT_FOUND` → 404 Not Found
    - `COUPON_INACTIVE` → 400 Bad Request
  - **OrderError**:
    - `ORDER_NOT_FOUND` → 404 Not Found
    - `EMPTY_CART` → 400 Bad Request
    - `PRODUCT_INACTIVE` → 409 Conflict
    - `INSUFFICIENT_STOCK` → 409 Conflict
    - `INVALID_STATUS_TRANSITION` → 409 Conflict
    - `PAYMENT_NOT_CAPTURED` → 409 Conflict
    - `CANNOT_REFUND_STATUS` → 409 Conflict
    - `GUEST_CHECKOUT_MISSING_REQUEST` → 400 Bad Request
  - **PaymentError**:
    - `PAYMENT_NOT_FOUND` → 404 Not Found
    - `ORDER_NOT_FOUND` → 404 Not Found
    - `ACCESS_DENIED` → 403 Forbidden
    - `INVALID_STATE_FOR_AUTHORIZE` → 409 Conflict
    - `INVALID_STATE_FOR_CAPTURE` → 409 Conflict
    - `INVALID_STATE_FOR_REFUND` → 409 Conflict
    - `INVALID_STATE_FOR_FAIL` → 409 Conflict
    - `PAYMENT_ALREADY_EXISTS` → 409 Conflict
- [ ] Error response body uses existing `ApiErrorResponse` with timestamp, status, error, message, path
- [ ] Path extracted from `ServletRequestAttributes` (current request)
- [ ] Method is generic: handles any `Result<?, E extends DomainError>`
- [ ] No `GlobalExceptionHandler` changes — it still catches unexpected exceptions

### Technical Notes
- Use `instanceof` pattern matching for exhaustive switch (Java 21+)
- Since `DomainError` is not sealed, use `if-else` chain or `switch` with `instanceof` for each subtype
- Import `ApiErrorResponse` from `common.exception`
- Inject `HttpServletRequest` or use `RequestContextHolder` to get current request path
- For void success (null value in Ok), return 204 No Content
- For non-void success, return 200 OK with value
- The mapper should be stateless and thread-safe

### HTTP Status Mapping Reference (from current GlobalExceptionHandler)
- `ResourceNotFoundException` → 404
- `IllegalStateException` → 409
- `IllegalArgumentException` → 400
- `ResourceAlreadyExistsException` → 409
- `ObjectOptimisticLockingFailureException` → 409
- `AuthorizationDeniedException` → 403
- `BadCredentialsException` → 401
- `DisabledException` → 401

### Files to Create
- `src/main/java/com/nathanaeldorsey/stormmarket/common/exception/ResultHttpMapper.java`

### Definition of Done
- Code compiles
- Follows project code style
- All 4 error types fully mapped
- Returns correct ResponseEntity for Ok/Err
- Uses ApiErrorResponse for error bodies
- No GlobalExceptionHandler modifications