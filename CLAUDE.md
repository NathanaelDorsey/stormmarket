# stormmarket — AI Agents

Read CONVENTIONS.md before any GitHub or git operation.

## Project
Storm Market - Java/Spring Boot e-commerce backend platform for online marketplace transactions
Stack: Java 25, Spring Boot 4.1.1, Maven, MySQL, H2 test database

## Commands
| Action | Command |
|--------|---------|
| Run    | `./mvnw spring-boot:run` |
| Test   | `./mvnw test` |
| Build  | `./mvnw verify` |
| Lint   | `./mvnw spotcheck` |
| Preflight | `./mvnw verify` |
| CI     | `gh pr checks` (when a PR is open) |

## Observability
| What | Command |
|------|---------|
| Health check | `curl http://localhost:8080/actuator/health` |
| Health check (detailed) | `curl http://localhost:8080/actuator/health | jq` |
| Health (liveness) | `curl http://localhost:8080/actuator/health/liveness` |
| Health (readiness) | `curl http://localhost:8080/actuator/health/readiness` |
| Metrics (112 total) | `curl http://localhost:8080/actuator/metrics` |
| Prometheus metrics (14 custom stormmarket.*) | `curl http://localhost:8080/actuator/prometheus` |
| Application info | `curl http://localhost:8080/actuator/info` |
| Loggers (9 app loggers configurable) | `curl http://localhost:8080/actuator/loggers` |
| Log level (change) | `curl -X POST -H "Content-Type: application/json" -d '{"configuredLevel": "DEBUG"}' http://localhost:8080/actuator/loggers/com.nathanaeldorsey.stormmarket` |
| Thread dump | `curl http://localhost:8080/actuator/threaddump` |
| Heap dump | `curl http://localhost:8080/actuator/heapdump` |
| Mappings | `curl http://localhost:8080/actuator/mappings` |
| Environment | `curl http://localhost:8080/actuator/env` |
| Config props | `curl http://localhost:8080/actuator/configprops` |
| View logs (local) | `tail -f logs/stormmarket.json.log` |
| View logs (Docker) | `docker logs -f stormmarket-app` |
| Check DB connection | `curl http://localhost:8080/actuator/health | jq '.components.db'` |

### Custom business metrics (14)
- `stormmarket_auth_failures_total` — failed login attempts
- `stormmarket_orders_total` — orders placed
- `stormmarket_orders_failed_total` — failed orders
- `stormmarket_orders_processing_time_seconds` — order latency histogram
- `stormmarket_products_total` — products created
- `stormmarket_products_updated_total` — products updated
- `stormmarket_users_active` — active user gauge
- `stormmarket_users_registered_total` — registered users

### JaCoCo Coverage (verify)
- Instruction: 64% | Branch: 51% | Line: 51% | Method: 62% | Class: 85%
- SpotBugs: 0 bugs, 0 errors
- FindSecBugs: 0 issues
- Spotless: compliant (Palantir Java Format)

## Architecture
Layered backend organized by feature (package-per-domain) under `com.nathanaeldorsey.stormmarket`: admin, auth, cart, category, common/exception, config, order, payment, product, user. REST controllers at `/v1` base path, Spring Data JPA repositories, MySQL production database with Flyway migrations, stateless JWT authentication.

### Result<T,E> Pattern
Domain-wide explicit error handling via `Result<T, E extends Throwable>` sealed interface (`Ok` / `Err`). Services return `Result<T, E>` for expected failures instead of throwing exceptions.

| Error Type | Constants | HTTP |
|------------|-----------|------|
| `StockError` | 6 (INSUFFICIENT_STOCK, PRODUCT_NOT_FOUND, ...) | 404/409 |
| `CartError` | 12 (INSUFFICIENT_STOCK, CART_FULL, INACTIVE_PRODUCT, ...) | 400/404/409 |
| `OrderError` | 8 (INVALID_STATUS_TRANSITION, PAYMENT_NOT_CAPTURED, ...) | 400/404/409 |
| `PaymentError` | 7 (OWNERSHIP_MISMATCH, INVALID_STATE_TRANSITION, ...) | 403/404/409 |

Services: `ProductRepository`, `CartService`, `OrderService`, `PaymentService` return `Result<T, E>`.
Controllers delegate to `ResultHttpMapper` → HTTP (200/204/400/403/404/409/500).
Transaction rollback on `Err` via `TransactionAspectSupport.setRollbackOnly()`.

## Conventions
- camelCase JSON request/response bodies
- Pagination via Spring Data `Pageable` with `max-page-size: 100`
- `@Valid` DTO binding on all request DTOs
- Centralized `GlobalExceptionHandler` converting to `ApiErrorResponse`
- Stateless JWT authentication with RS256 signing and `role` → `ROLE_*` authority mapping
- Argon2 password encoding
- CSRF disabled per design; permitList for auth/catalog read routes
- File storage configurable between local (`uploads/products`) and S3 via `STORMMARKET_FILE_STORAGE_TYPE`

## Never
- Never dismiss reproducible gate failures as pre-existing or out of scope
- Never proceed on red Preflight or red CI — invoke `quick-fix` or `fix-bug` first
- Never commit secrets or force-push to shared branches
- Never bypass `@Valid` validation or authentication checks

## Agent Rules
- **Workflow Mandate:** You MUST use the bigpowers skills (e.g. `plan-work`, `develop-tdd`, `orchestrate-project`) to perform tasks. DO NOT write code directly in response to a user prompt like "build this feature".
- **Always Green:** Preflight and CI must be green before forward work. Reproducible gate failures require **fix-or-log** (quick-fix → fix-bug) per CONVENTIONS § Discovered Defects.
- Read `specs/` before writing code.
- All planning and specifications MUST be written to `specs/` (`product/SCOPE_LATEST.yaml`, `release-plan.yaml`, `epics/`) before any code is generated.
- Write the minimum code that solves the stated problem. Nothing extra.
- Run tests after every change. Show evidence before declaring done.
- One clarifying question beats a wrong assumption baked into 200 lines.