# StormMarket

StormMarket is a Spring Boot e-commerce backend built to demonstrate production-minded backend engineering: secure authentication, migration-driven persistence, layered architecture, and a testing strategy that balances speed with real-world validation.

This project models a marketplace API with JWT-based auth, role-aware admin actions, catalog management, and persistence using MySQL in production and H2 in the default local test path.

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
- cart, order, shipping, and transaction foundations
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

The default smoke test uses an isolated H2 profile. MySQL integration tests are explicitly gated behind `-Drun.mysql.integration=true` and require Docker.

### Test coverage by domain

| Domain | What is tested | Test class |
|--------|---------------|------------|
| Auth & users | Registration, login, JWT validation, inactive users, duplicate detection | `AuthServiceTest`, `GlobalExceptionHandlerTest` |
| Categories | CRUD, unique constraints, authorization, validation errors | `CategoryControllerTest`, `CategoryServiceTest` |
| Products | Create/update/delete, search/filter, pagination, admin endpoints, stock | `ProductControllerTest`, `ProductServiceTest` |
| Cart | Add/remove/update items, quantity boundaries, stock checks, totals, cross-user isolation | `CartControllerTest`, `CartServiceTest`, `MySqlIntegrationTest` |
| Orders | Checkout, stock reduction, cart clearing, snapshots, transactional rollback, history | `OrderControllerTest`, `OrderServiceTest`, `MySqlIntegrationTest` | Admin | User CRUD, role changes, activate/deactivate, authorization | `AdminControllerTest`, `AdminServiceTest` |
| Errors & validation | Malformed JSON, missing fields, invalid types, consistent error schema | `GlobalExceptionHandlerTest` |
| Database | Flyway migrations, constraints, FKs, indexes, transaction rollback, H2 vs MySQL differences | `MySqlIntegrationTest` |
| Application wiring | Full-context startup composition | `StormmarketApplicationSmokeTest`, `StormmarketApplicationTests` |

### Quality gates

```bash
# Run the default H2 smoke suite (no Docker required)
./mvnw test

# Run the MySQL/Testcontainers integration suite (requires Docker)
./mvnw test -Drun.mysql.integration=true

# Full verification: tests, static analysis, formatting, coverage
./mvnw verify
```

### Test status

All 119 tests pass on `./mvnw test` (BUILD SUCCESS, 0 failures, 0 errors, 0 skipped).

### Notes on fuzz testing

A `FuzzTest` (jqwik property-based tests for cart quantities) was previously included but removed because random `@Positive` quantity generation caused OutOfMemory errors. Property-based fuzzing is useful for catching edge cases, but where each test run is expensive or unbounded inputs risk resource exhaustion, targeted boundary-value tests (`@IntRange` instead of `@Positive`) are preferred.

The full test plan and boundaries are documented in `src/testing_stage_plan.md` and `testPt2.md`.

## Project structure

```text
stormmarket/
├── README.md
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nathanaeldorsey/stormmarket/
│   │   ├── resources/
│   │   │   ├── application.yaml
│   │   │   └── db/migration/
│   ├── test/
│   │   ├── java/
│   │   └── resources/
│   └── testing_stage_plan.md
└── target/
```

## Getting started

### Prerequisites

- Java 25
- Maven
- Docker for MySQL/Testcontainers validation

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

This requires Docker because the MySQL integration tests start a disposable MySQL container via Testcontainers.

## What I built

This is a marketplace backend with a realistic, production-aware structure. It includes:
- JWT-based authentication and authorization
- role-protected admin operations
- catalog and product management
- migration-based persistence conventions
- layered services and repository-based domain logic
- a multi-layered test suite for trust and maintainability

## Challenges I solved

- secured endpoints with Spring Security and JWT claims
- aligned authorization rules with the app’s intended public/private contract
- fixed service and JPA issues surfaced by tests
- hardened startup behavior so local tests do not depend on external secrets or DB setup
- created a fast default smoke suite while keeping MySQL parity checks available
- validated migration and persistence behavior with Testcontainers

## What I learned

- how to balance developer speed with production realism in testing
- how to manage security policy and JWT authority mapping in a real API
- why migration-backed schema validation matters for database correctness
- how test failures reveal service contract issues early
- how to design a backend that is secure, maintainable, and testable

## Future improvements

- expand admin workflows and reporting
- add full e2e API documentation and examples
- improve observability with structured logging and metrics
- add CI/CD automation for build and release validation
- optionally add a frontend client for a complete storefront experience


See `.agents/skills/greptile-review/SKILL.md` for detailed usage.