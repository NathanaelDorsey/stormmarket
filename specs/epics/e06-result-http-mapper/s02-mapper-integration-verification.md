---
story_id: "e06s02"
epic_id: "e06"
title: "ResultHttpMapper integration verification with all controllers"
status: "pending"
estimate: "M"
depends_on: ["e03s02", "e04s02", "e05s02"]
---

## Story: ResultHttpMapper integration verification with all controllers

**Vertical Slice:** Verifies that ResultHttpMapper works correctly when called from CartController, OrderController, and PaymentController. This is a validation story that runs after all controller delegation stories are complete.

### Acceptance Criteria
- [ ] CartController mutation endpoints return correct HTTP status/body for each CartError via mapper
- [ ] OrderController mutation endpoints return correct HTTP status/body for each OrderError via mapper
- [ ] PaymentController mutation endpoints return correct HTTP status/body for each PaymentError via mapper
- [ ] Success responses (Ok) return correct status (200/201/204) and body
- [ ] Error responses (Err) return correct status (400/403/404/409) and ApiErrorResponse body
- [ ] Request path correctly included in ApiErrorResponse
- [ ] Timestamp in UTC included in ApiErrorResponse
- [ ] No regressions in HTTP contract — all endpoints return identical responses to pre-change behavior
- [ ] GlobalExceptionHandler still handles unexpected exceptions (NPE, DB down, etc.) → 500
- [ ] GlobalExceptionHandler still handles auth/validation exceptions (BadCredentials, MethodArgumentNotValid, etc.)

### Technical Notes
- Run full test suite (175 tests) to verify integration
- Run Postman collection (21 requests, 67 assertions) to verify HTTP contract
- Spot-check key endpoints manually if needed
- This story has no code changes — only verification

### Verification Commands
- `mvn test` — all 175 tests pass
- `mvn spotless:check` — code style compliant
- Postman collection run — 21 requests, 67 assertions, 0 failures
- SpotBugs/FindSecBugs — 0 issues

### Definition of Done
- All tests pass
- Postman collection passes
- Code quality gates pass
- No HTTP contract regressions