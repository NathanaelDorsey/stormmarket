---
story_id: "e01s01"
epic_id: "e01"
title: "DomainError base class and StockError"
status: "pending"
estimate: "S"
---

## Story: DomainError base class and StockError

**Vertical Slice:** Creates the foundational `DomainError` abstract base class extending `RuntimeException` and the `StockError` error type with static singleton instances for all product stock failure scenarios.

### Acceptance Criteria
- [ ] `DomainError` abstract class created in `common.exception` package, extends `RuntimeException`
- [ ] `StockError` class created in `product` package, extends `DomainError`
- [ ] `StockError` defines static singleton instances for:
  - `INSUFFICIENT_STOCK` — requested quantity exceeds available stock
  - `PRODUCT_NOT_FOUND` — product ID does not exist
  - `PRODUCT_INACTIVE` — product is not active
  - `RESERVATION_FAILED` — atomic reserveStock returned 0 rows
  - `CAPTURE_FAILED` — atomic captureStock returned 0 rows
  - `DEDUCT_FAILED` — atomic deductStock returned 0 rows
- [ ] All error instances are thread-safe, zero-allocation singletons (private constructor)
- [ ] Error messages are descriptive and consistent with existing exception messages
- [ ] No changes to existing behavior — this is a new type foundation only

### Technical Notes
- `DomainError` should be `abstract` to prevent direct instantiation
- `DomainError` constructor accepts message string, passes to `super(message)`
- Each error class has `private` constructor taking message, creates static final instances
- Package placement: `DomainError` in `common.exception`, service-specific errors in their respective service packages
- Follows existing pattern from `CouponValidationError`

### Files to Create/Modify
- `src/main/java/com/nathanaeldorsey/stormmarket/common/exception/DomainError.java` (new)
- `src/main/java/com/nathanaeldorsey/stormmarket/product/StockError.java` (new)

### Definition of Done
- Code compiles
- Follows project code style (Spotless/Palantir Java Format)
- No new dependencies