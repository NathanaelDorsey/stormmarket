# StormMarket — PR #2 Greptile Findings Fix Plan

## Context

PR #2 (`feature/greptile-test` targeting `main`) has 11 open Greptile findings. Need to fix all so the PR can merge.

## Files to Modify

### Source Code Fixes
| # | Finding | File | Fix |
|---|---------|------|-----|
| 1 | Guests cannot check out | `SecurityConfig.java` | Add `POST /v1/orders` permitAll before `anyRequest().authenticated()` |
| 2 | Refunds erase other reservations | `OrderService.java` | Skip stock changes if order already CANCELED |
| 3 | Expiration changes wrong stock | `OrderService.java` | `updateOrderStatus` EXPIRED: use same flag-based cleanup as expirePendingOrders |
| 4 | Shipped orders can be refunded | `PaymentService.java` | Lock order before reading state in refundPayment |
| 5 | Catalog edits erase reservations | `ProductRepository.java` | Add `p.version = p.version + 1` to all stock-changing queries |

### Workflow Fixes
| # | Finding | File | Fix |
|---|---------|------|-----|
| 6 | Critical findings sort last | `greptile-review.yml` | `|| 99` → `?? 99` (both occurrences) |
| 7 | Failed reviews become parsing errors | `greptile-review.yml` | Check command result, validate report before JSON.parse |

### Documentation Fixes
| # | Finding | File | Fix |
|---|---------|------|-----|
| 11 | Coverage checks overstated | `testPt2.md` | Reopen coverage threshold items |
| 11 | Coverage table outdated | `README.md` | Remove deleted AuthControllerTest from coverage table |

### Test Fixes
| # | Finding | File | Fix |
|---|---------|------|-----|
| 8 | Tests check different security rules | `OrderControllerSecurityTest.java`, `CartControllerSecurityTest.java` | Use real SecurityConfig, mock JWT decoder |
| 9 | Registration loses HTTP tests | `AuthControllerTest.java` | Recreate with `/v1/auth/register` routes |
| 10 | Payment rules lack tests | New `PaymentServiceTest.java` | Add tests for ownership, state rules, capture/refund, locking |

## Steps
- [ ] Step 1: SecurityConfig - add guest checkout permitAll
- [ ] Step 2: OrderService - cancelOrderWithRefund skip if already CANCELED
- [ ] Step 3: OrderService - updateOrderStatus EXPIRED flag-based cleanup
- [ ] Step 4: PaymentService - lock order before reading in refundPayment
- [ ] Step 5: ProductRepository - advance version in stock queries
- [ ] Step 6: greptile-review.yml - fix sort (`?? 99`) and error handling
- [ ] Step 7: testPt2.md - reopen coverage items
- [ ] Step 8: README.md - update coverage table
- [ ] Step 9: Security tests - use real SecurityConfig
- [ ] Step 10: AuthControllerTest - recreate with /v1/auth routes
- [ ] Step 11: PaymentServiceTest - add new tests
- [ ] Step 12: Run tests (136 must pass)
- [ ] Step 13: Commit and push to feature/greptile-test