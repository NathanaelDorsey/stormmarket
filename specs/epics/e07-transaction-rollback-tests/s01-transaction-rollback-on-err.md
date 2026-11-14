---
story_id: "e07s01"
epic_id: "e07"
title: "Transaction rollback on Err via setRollbackOnly()"
status: "pending"
estimate: "M"
---

## Story: Transaction rollback on Err via setRollbackOnly()

**Vertical Slice:** Adds explicit transaction rollback marking in all `@Transactional` service methods that return `Result` when the result is `Err`. Uses `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()` to preserve declarative `@Transactional` semantics without throwing exceptions.

### Acceptance Criteria
- [ ] `ProductService` stock methods (if any `@Transactional`) call `setRollbackOnly()` on `Err`
- [ ] `CartService` all 6 mutation methods call `setRollbackOnly()` on `Err`
- [ ] `OrderService` all 4 transition methods + `expirePendingOrders` call `setRollbackOnly()` on `Err`
- [ ] `PaymentService` all 5 lifecycle methods call `setRollbackOnly()` on `Err`
- [ ] Rollback marking only on `Err` — `Ok` commits normally
- [ ] No changes to `@Transactional` annotations (readOnly, propagation, etc.)
- [ ] Integration tests verify rollback behavior (database state unchanged after Err)

### Technical Notes
- Import `org.springframework.transaction.support.TransactionAspectSupport`
- Pattern: `if (result.isErr()) { TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); return result; }`
- Place at the point of returning Err, before return statement
- For methods that return Result from other service calls (e.g., PaymentService calling OrderService), the callee should handle its own rollback; caller just propagates
- `expirePendingOrders` is `@Scheduled` `@Transactional` — on Err from stock methods, log but continue; rollback only the current order's transaction
- Since each order in `expirePendingOrders` is processed in the same transaction (method-level), a rollback would roll back all. Better: each order processed in its own transaction. But current code processes all in one transaction. For now, on any Err, mark rollback and continue loop — but this rolls back all. Acceptable for scheduled job? Or extract per-order to a separate @Transactional method. Per SCOPE: "Transaction rollback on Err via explicit setRollbackOnly()" — implement as specified.

### Files to Modify
- `src/main/java/com/nathanaeldorsey/stormmarket/product/ProductService.java` (if has @Transactional stock methods)
- `src/main/java/com/nathanaeldorsey/stormmarket/cart/CartService.java`
- `src/main/java/com/nathanaeldorsey/stormmarket/order/OrderService.java`
- `src/main/java/com/nathanaeldorsey/stormmarket/payment/PaymentService.java`

### Definition of Done
- Code compiles
- Follows project code style
- All @Transactional Result-returning methods call setRollbackOnly() on Err
- Integration tests verify rollback (database state unchanged after expected failure)
- No @Transactional annotation changes