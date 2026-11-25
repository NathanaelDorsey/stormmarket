# StormMarket

StormMarket is a Spring Boot e-commerce backend built to demonstrate production-minded backend engineering: secure authentication, migration-driven persistence, layered architecture, and a testing strategy that balances speed with real-world validation.

This project models a marketplace API with JWT-based auth, role-aware admin actions, catalog management, and persistence using MySQL in production and H2 in the default local test path.

## Current Status

| Component | Status |
|-----------|--------|
| Backend API | ✅ **COMPLETE** |
| Frontend/Client | 🔄 **IN-PROGRESS** |
| CI/CD Pipeline | ✅ **COMPLETE** |

## Why this project exists

This application is meant to showcase the kinds of decisions real backend teams make:

- separate controller, service, repository, and domain responsibilities
- secure API design with JWT bearer authentication
- migration-based schema evolution with Flyway
- API contracts built around DTOs, validation, and structured errors
- testing that is fast by default but realistic when database behavior matters

## Core capabilities

- user registration and authentication
- role-based access control for admin-only workflows
- product and category management
- public product and category catalog reads
- cart, order, shipping, coupon discounts, and transaction foundations
- application boot verification with a lightweight smoke suite

## Tech stack

- Java 25
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server + JWT
- MySQL
- Flyway
- H2 (test default)
- Testcontainers (MySQL integration validation)
- Maven
- Lombok
- JUnit 5 + Spring Test
- JaCoCo
- jqwik (property-based testing)

## Architecture

The project follows a standard layered backend structure:

- Controllers: accept HTTP requests and translate them into use cases
- Services: contain business rules and validation
- Repositories: persist and retrieve JPA entities
- Entities: represent the database/domain model
- DTOs: define API contracts
- Mappers: convert between entity and response models
- SecurityConfig: defines public routes, protected routes, and JWT authorization

This keeps the codebase easier to test, reason about, and extend.

## Security and API behavior

The system is built around secure API practices:

- public auth endpoints are allowed for sign-in and registration flows
- public GET access is intentionally allowed for products and categories
- admin-only workflows require ADMIN authority
- JWT authentication is enforced for protected requests
- invalid credentials and disabled accounts return 401-style responses

## CI/CD Pipeline (Complete)

The project includes a complete CI/CD pipeline via GitHub Actions and local bootstrap:

```bash
# Local CI validation (runs full pipeline locally)
./scripts/test-bootstrap.sh [admin|user]

# Full CI verification
./mvnw verify
```

### Pipeline Components

| Component | Status | Description |
|-----------|--------|-------------|
| Postman API Tests | ✅ Complete | 21 requests, ~48 assertions executed per run (201 declared in collection), 0 failures |
| Maven Verify | ✅ Complete | 186 tests, 0 SpotBugs, 0 FindSecBugs, JaCoCo (56% instruction / 40% branch / 58% line) |
| Code Formatting | ✅ Complete | Spotless (Palantir Java Format) |
| CodeRabbit AI Review | ✅ Complete | Automated PR review |
| Local Bootstrap | ✅ Complete | `scripts/test-bootstrap.sh` |
| H2 In-Memory DB | ✅ Complete | No Docker required for local testing |
| Docker Multi-stage Build | ✅ Complete | Multi-stage Dockerfile for production |
| docker-compose.yml | ✅ Complete | Local dev and production orchestration |
| Nginx Reverse Proxy | ✅ Complete | SSL termination, rate limiting, security headers |
| Build/Deploy Workflow | ✅ Complete | GitHub Actions docker-build-deploy.yml |
| Smoke Test Script | ✅ Complete | scripts/smoke-test.sh for post-deploy validation |
| Container Registry | ✅ Complete | GitHub Container Registry (ghcr.io) |
| Result<T,E> Pattern | ✅ Complete | Domain-wide explicit error handling (7 epics, 186 tests) |

### Local CI Validation

```bash
# Run full CI pipeline locally (starts H2, creates users, seeds data, runs Postman)
./scripts/test-bootstrap.sh admin
```

This script:
1. Starts Spring Boot with H2 in-memory database
2. Registers test users (ci_admin, ci_user) with Argon2 passwords
3. Updates admin role to ADMIN via H2 Shell
5. Gets JWT tokens (RS256)
5. Creates test data (categories, products) via admin API
6. Runs Postman collection (21 requests, ~48 assertions executed per run)
6. Cleans up H2 database on exit

## Testing strategy

The project uses a layered testing strategy designed for real delivery teams. Tests assert observable behavior and API contracts — service unit tests mock repositories and mappers, MVC/security tests validate HTTP behavior and auth boundaries, and persistence tests use real repositories against a disposable database. Shared mutable fixtures and test-order coupling are avoided.

### Default path: H2 smoke tests

The default local and CI path runs against an in-memory H2 database:

- fast feedback during development
- minimal setup cost
- validates application wiring, security rules, and core service behavior
- requires no Docker, remote database, or production secrets

### Opt-in path: MySQL + Testcontainers

When persistence correctness matters, the project runs MySQL in disposable containers:

- validates JPA behavior against MySQL semantics
- verifies Flyway migrations and schema compatibility
- catches MySQL-specific behavior that H2 would miss

### Why this approach

This balances speed and realism:

- H2-only is too optimistic and misses MySQL-specific migration and SQL issues
- MySQL-only is slower and frustrating for daily iteration
- Docker/Testcontainers provide realism without forcing every local run to use MySQL

The default smoke test uses an isolated H2 profile. MySQL integration tests are explicitly gated behind `-Drun.mysql.integration=true` and require Docker or Podman.

### Container runtime: Docker or Podman

Testcontainers works with either Docker or Podman.

```bash
# Docker (if installed)
./mvnw test -Drun.mysql.integration=true

# Podman (DOCKER_HOST points to the Podman socket)
export DOCKER_HOST="unix://$XDG_RUNTIME_DIR/podman/podman.sock"
./mvnw test -Drun.mysql.integration=true
```

Testcontainers detects `DOCKER_HOST` and will talk to the Podman socket automatically. No Docker CLI is required.

### Test coverage by domain

| Domain              | What is tested                                                                              | Test class                                                        |
| ------------------- | ------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| Auth & users        | Registration, login, JWT validation, inactive users, duplicate detection                    | `AuthServiceTest`, `GlobalExceptionHandlerTest`                   |
| Categories          | CRUD, unique constraints, authorization, validation errors                                  | `CategoryControllerTest`, `CategoryServiceTest`                   |
| Products            | Create/update/delete, search/filter, pagination, admin endpoints, stock                     | `ProductControllerTest`, `ProductServiceTest`                     |
| Cart                | Add/remove/update items, quantity boundaries, stock checks, totals, cross-user isolation    | `CartControllerTest`, `CartServiceTest`, `MySqlIntegrationTest`   |
| Orders              | Checkout, stock reduction, cart clearing, snapshots, transactional rollback, history        | `OrderControllerTest`, `OrderServiceTest`, `MySqlIntegrationTest` |
| Errors & validation | Malformed JSON, missing fields, invalid types, consistent error schema                      | `GlobalExceptionHandlerTest`                                      |
| Database            | Flyway migrations, constraints, FKs, indexes, transaction rollback, H2 vs MySQL differences | `MySqlIntegrationTest`                                            |
| Application wiring  | Full-context startup composition                                                            | `StormmarketApplicationSmokeTest`, `StormmarketApplicationTests`  |

### Property-based tests (jqwik)

| Test class | Properties tested |
|------------|-------------------|
| `CartQuantityFuzzTest` | Valid/invalid quantity boundaries (1-100) |
| `OrderTotalCalculationTest` | Total = sum(price×qty), discount boundaries, tax, precision |
| `StockConsistencyTest` | Never below zero, no oversell, reservation, cumulative |
| `PriceValidationTest` | Positive prices, discount consistency, tax boundaries, precision |

### Quality gates

```bash
# Run the default H2 smoke suite (no Docker required)
./mvnw test

# Run the MySQL/Testcontainers integration suite (requires Docker or Podman)
./mvnw test -Drun.mysql.integration=true

# Full verification: tests, static analysis, formatting, coverage
./mvnw verify

# Local CI bootstrap (runs full pipeline)
./scripts/test-bootstrap.sh admin

# Check formatting
./mvnw spotless:check

# View JaCoCo coverage report
open target/site/jacoco/index.html
```

### Test status

All 186 tests pass on `./mvnw test` (BUILD SUCCESS, 0 failures, 0 errors, 0 skipped).
Postman collection: 21 requests, ~48 assertions executed per run (201 declared across all response branches), 0 failures.

### Result<T,E> Pattern (NEW)

Domain-wide adoption of `Result<T, E>` sealed interface for explicit error handling:

| Aspect | Detail |
|--------|--------|
| Type | Sealed interface `Result<T, E extends Throwable>` with `Ok<T, E>` / `Err<T, E>` records |
| Factories | `Result.ok(value)`, `Result.err(error)` |
| Accessors | `result.isOk()`, `result.isErr()`, `result.getValue()`, `result.getError()` |
| Error hierarchy | `DomainError extends RuntimeException` → `StockError`, `CartError`, `OrderError`, `PaymentError` |
| Error constants | 32 static singleton error constants (e.g., `CartError.INSUFFICIENT_STOCK`) |
| HTTP mapping | `ResultHttpMapper` component maps `Result` → HTTP (200/204/400/403/404/409/500) |
| Services updated | `ProductRepository`, `CartService`, `OrderService`, `PaymentService` |
| Transaction rollback | `TransactionAspectSupport.setRollbackOnly()` on `Err` paths |

**Before**: Exceptions thrown for expected failures (try/catch control flow)
**After**: `Result<T, E>` returned for expected failures (compile-time handling enforced)

### Observability metrics

| Metric | Value |
|--------|-------|
| Total Micrometer metrics | 112 |
| Custom stormmarket.* metrics | 14 |
| JaCoCo Instruction Coverage | 56% |
| JaCoCo Branch Coverage | 40% |
| JaCoCo Line Coverage | 58% |
| JaCoCo Method Coverage | 59% |
| JaCoCo Class Coverage | 86% |
| SpotBugs | 0 bugs, 0 errors |
| FindSecBugs | 0 issues |
| Actuator endpoints exposed | 9 (health, info, metrics, prometheus, loggers, threaddump, heapdump, mappings, env, configprops) |

### Custom business metrics

- `stormmarket_auth_failures_total` — failed login attempts
- `stormmarket_orders_total` — orders placed
- `stormmarket_orders_failed_total` — failed orders
- `stormmarket_orders_processing_time_seconds` — order latency histogram
- `stormmarket_products_total` — products created
- `stormmarket_products_updated_total` — products updated
- `stormmarket_users_active` — active user gauge
- `stormmarket_users_registered_total` — registered users

## Project structure

```text
stormmarket/
├── README.md
├── pom.xml
├── scripts/
│   └── test-bootstrap.sh          # Local CI bootstrap
├── specs/
│   ├── product/                   # SCOPE, VISION, GLOSSARY
│   ├── tech-architecture/         # Tech plans (security, test, design, etc.)
│   ├── epics/                     # Epic capsules
│   └── state.yaml                 # Session state
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nathanaeldorsey/stormmarket/
│   │   ├── resources/
│   │   │   ├── application.yaml
│   │   │   └── db/migration/      # 25 Flyway migrations
│   ├── test/
│   │   ├── java/
│   │   │   └── com/nathanaeldorsey/stormmarket/
│   │   │       └── fuzz/          # jqwik property tests
│   │   └── resources/
│   └── project_outline.txt
├── postman/
│   └── collections/               # Postman v3 schema
└── target/
```

## Getting started

### Prerequisites

- Java 25
- Maven
- Docker or Podman for MySQL/Testcontainers validation

### Run the application

```bash
./mvnw spring-boot:run
```

### Run the default test suite

```bash
./mvnw test
```

### Run the MySQL integration suite

```bash
./mvnw test -Drun.mysql.integration=true
```

This requires Docker or Podman because the MySQL integration tests start a disposable MySQL container via Testcontainers. Set DOCKER_HOST to point to your container engine socket if using Podman.

### Run local CI validation

```bash
./scripts/test-bootstrap.sh admin
```

## What I built

This is a marketplace backend with a realistic, production-aware structure. It includes:

- JWT-based authentication and authorization
- role-protected admin operations
- catalog and product management with coupon discount support
- migration-based persistence conventions
- layered services and repository-based domain logic
- a multi-layered test suite for trust and maintainability

## Challenges I solved

- secured endpoints with Spring Security and JWT claims
- aligned authorization rules with the app's intended public/private contract
- fixed service and JPA issues surfaced by tests
- hardened startup behavior so local tests do not depend on external secrets or DB setup
- created a fast default smoke suite while keeping MySQL parity checks available
- wired coupon discounts into cart and checkout flow (apply/remove coupon on cart, snapshot discount on order, usage count incremented on payment capture)
- validated migration and persistence behavior with Testcontainers
- built local CI bootstrap for full pipeline validation without CI server

## What I learned

- how to balance developer speed with production realism in testing
- how to manage security policy and JWT authority mapping in a real API
- why migration-backed schema validation matters for database correctness
- how test failures reveal service contract issues early
- how to design a backend that is secure, maintainable, and testable

## Future improvements

- expand admin workflows and reporting
- add full e2e API documentation and examples
- optionally add a frontend client for a complete storefront experience
