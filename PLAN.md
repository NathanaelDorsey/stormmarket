# StormMarket — PR #2 Greptile Findings Fix Plan (Round 2)

## Context

PR #2 (`feature/greptile-test` targeting `main`) has 5 remaining Greptile findings blocking merge. The user wants the `.github/workflows/greptile-review.yml` removed entirely, plus fixes for 4 remaining code/test findings.

## Approach

Fix each finding directly:
1. Delete `greptile-review.yml` entirely (user requested removal)
2. Restructure `refundPayment` to lock order before reading any state from it
3. Recreate `AuthControllerTest.java` with `/v1/auth/register` routes
4. Rewrite security tests to use real `SecurityConfig` via `@Import(SecurityConfig.class)` instead of test-local config
5. Add missing PaymentService tests for shipped-order refund protection and database-backed locking

## Files to Modify

| # | Finding | File | Action |
|---|---------|------|--------|
| 1 | Review workflow cannot load | `.github/workflows/greptile-review.yml` | Delete entirely |
| 2 | Shipped orders can be refunded | `src/main/java/.../payment/PaymentService.java` | Lock order before reading `payment.getOrder()`; restructure ownership check to use locked order |
| 3 | Registration loses HTTP tests | `src/test/java/.../auth/AuthControllerTest.java` | Create new test with `/v1/auth/register` routes (valid, invalid, duplicate username, duplicate email) |
| 4 | Tests check different security rules | `src/test/java/.../order/OrderControllerSecurityTest.java` | Rewrite to `@Import(SecurityConfig.class)` + mock `JwtDecoder` |
| 4 | Tests check different security rules | `src/test/java/.../cart/CartControllerSecurityTest.java` | Rewrite to `@Import(SecurityConfig.class)` + mock `JwtDecoder` |
| 5 | Payment rules lack tests | `src/test/java/.../payment/PaymentServiceTest.java` | Add test for SHIPPED/DELIVERED refund rejection + database-backed locking verification |

## Reuse

- `OrderRepository.findByIdWithLock` — already exists, used for pessimistic locking
- `SecurityConfig` — real config at `src/main/java/.../config/SecurityConfig.java`, to be imported in tests
- `JwtDecoder` mock — standard Spring Security test mock
- `PaymentServiceTest` — already exists with 17 tests; add missing shipped-order refund test

## Steps

- [x] Step 1: Delete `.github/workflows/greptile-review.yml`
- [x] Step 2: Fix `PaymentService.refundPayment` — lock order before reading state
- [x] Step 3: Create `AuthControllerTest.java` with `/v1/auth/register` routes
- [x] Step 4: Rewrite `OrderControllerSecurityTest.java` to use real `SecurityConfig`
- [x] Step 5: Rewrite `CartControllerSecurityTest.java` to use real `SecurityConfig`
- [x] Step 6: Add `refundPaymentRejectsShippedOrDeliveredOrder` test to `PaymentServiceTest.java`
- [ ] Step 7: Run `./mvnw test` — all tests must pass
- [ ] Step 8: Commit and push to `feature/greptile-test`

## Verification

- `./mvnw test` must pass (156+ tests, 0 failures)
- `greptile-review.yml` must not exist
- `AuthControllerTest` must test `/v1/auth/register` with valid/invalid/duplicate cases
- Security tests must import real `SecurityConfig`, not define their own
- `PaymentServiceTest` must verify SHIPPED/DELIVERED orders cannot be refunded