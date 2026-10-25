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

The project uses a layered testing strategy designed for real delivery teams.

### Default path: H2 smoke tests

The default local and CI path runs against an in-memory H2 database:
- fast feedback during development
- minimal setup cost
- validates application wiring, security rules, and core service behavior

### Opt-in path: MySQL + Testcontainers

When persistence correctness matters, the project runs MySQL in disposable containers:
- validates JPA behavior against MySQL semantics
- verifies Flyway migrations and schema compatibility
- helps catch issues that H2 would miss

### Why this approach

This is the preferred balance over a single-database strategy:
- H2-only is too optimistic and misses MySQL-specific behavior
- MySQL-only is slower and frustrating for daily iteration
- Docker/Testcontainers provide the realism we need without forcing every local run to use MySQL

The rationale and project-specific test plan are documented in `src/testing_stage_plan.md`.

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