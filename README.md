# StormMarket

StormMarket is a Spring Boot e-commerce backend built to demonstrate production-minded backend engineering: secure authentication, database persistence, layered architecture, migration-driven schema evolution, and a practical testing strategy that balances speed with realism.

This project is designed to read like a real-world service: it exposes REST APIs, protects admin operations with role-based authorization, persists domain data in MySQL, and validates behavior with a fast H2-based smoke suite alongside opt-in MySQL/Testcontainers integration checks.

## Why this project exists

This application is meant to showcase the kind of backend work expected in a modern software engineering environment:
- clean separation of concerns between controller, service, repository, and domain layers
- secure API design with JWT-based authentication and authorization
- database-first development using Flyway migrations
- maintainable API contracts with DTOs, validation, and structured error responses
- a disciplined testing strategy suited for real delivery teams

## Core capabilities

- user registration and authentication
- role-based access control for admin-only workflows
- product and category management
- product search and catalog exposure
- shopping cart and order processing foundations
- transaction and shipping-related domain models
- full application boot verification with a default smoke test profile

## Tech stack

- Java 25
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server / JWT authentication
- MySQL
- Flyway
- H2 for default testing
- Testcontainers for MySQL integration validation
- Maven
- Lombok
- JUnit 5 + Spring Test
- JaCoCo

## Architecture

The application follows a standard layered backend pattern:

- Controllers: accept HTTP requests and translate them into application actions
- Services: implement business logic and validation
- Repositories: persist and retrieve data through JPA
- Entities: represent database tables and domain state
- DTOs: define request/response contracts for the API
- Mappers: convert between entities and API models
- SecurityConfig: enforces public routes, protected routes, and JWT validation

This separation keeps the codebase easier to maintain, test, and extend as the business domain grows.

## Domain overview

StormMarket models a marketplace with a practical e-commerce domain:
- users and authentication
- roles and authorization boundaries
- products and categories
- cart and cart item workflows
- orders, transactions, and shipping data
- admin management of catalog and platform operations

## Security and API behavior

The system is built around secure API practices:
- public endpoints for authentication and catalog read access where appropriate
- protected admin functionality behind role checks
- JWT authentication for authenticated requests
- validation and consistent error handling for invalid inputs and auth failures

## Testing strategy

The project follows an enterprise-style layered testing approach, intentionally designed to balance speed and realism.

### Option A: fast default H2 smoke suite + opt-in MySQL validation

This is the selected strategy for the project:
- default local and CI execution uses an in-memory H2 database for quick feedback
- the app boots and validates under a lightweight test profile without requiring a production database
- MySQL validation is enabled through Testcontainers when persistence realism matters
- Docker is valuable because it creates disposable, deterministic MySQL environments for validation runs

This approach is preferred over a pure H2-only strategy because it misses MySQL-specific behavior, and over a pure MySQL-only strategy because it makes everyday development slower and less productive.

### Coverage types in this project

- unit tests for service-level logic and business rules
- security tests for authentication and authorization contracts
- application context smoke tests for configuration and boot integrity
- MySQL integration tests for migration and persistence parity

The implementation and rationale are documented in `src/testing_stage_plan.md`.

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
- Docker (required for MySQL/Testcontainers validation runs)

### Run the application
```bash
./mvnw spring-boot:run
```

### Run the default test suite
```bash
./mvnw test
```

### Run the MySQL integration test profile
```bash
./mvnw test -Dspring.profiles.active=mysql-integration
```

## Notes for recruiters and reviewers

This project demonstrates practical engineering judgment rather than a toy CRUD example. It reflects the kinds of decisions teams make in production:
- where to optimize for speed
- where to test against real infrastructure
- how to secure a backend without overcomplicating the system
- how to keep local development fast while still validating critical behavior

## Project status

The backend is in a working, test-verified state with a layered architecture and a realistic delivery-oriented testing plan.

## License

This repository is currently intended for portfolio and learning use.

## What I built

This project is a complete backend for a marketplace-style application. It includes secure authentication, role-based access control, catalog management, cart and order foundations, and a database-driven architecture that uses real migration and persistence patterns.

I designed the application to look and feel like a real product backend rather than a simple tutorial project. The code separates concerns cleanly, uses validation and consistent error handling, and includes testing at multiple levels so the system is easier to trust and extend.

## Challenges I solved

- implemented JWT-based authentication and authorization with Spring Security
- enforced public vs protected API routes using a structured security configuration
- handled persistence and schema evolution through Flyway migrations
- created a fast default testing profile using H2 while keeping MySQL parity checks available via Testcontainers
- ensured authentication failures and invalid requests returned meaningful HTTP behavior instead of generic server errors
- resolved runtime issues related to JPA criteria, validation, and service contract mismatches during the testing phase

## What I learned

- how to design a backend around clear boundaries between API, business logic, and persistence
- how to balance developer speed with production realism when choosing testing strategies
- how to secure a REST API with JWTs and authorization rules without overengineering the solution
- why migration-based schema management and realistic integration tests matter for database correctness
- how test failures reveal product and contract issues early when the app is structured for real-world use

## Future improvements

- add a frontend client for a complete end-to-end user experience
- expand the admin dashboard and reporting workflows
- add stronger observability with structured logs, metrics, and health dashboards
- improve containerization and deployment automation
- add CI/CD pipelines for automated build, test, and release validation

## Demo / walkthrough

A typical flow for the app includes:
1. register a user or authenticate with existing credentials
2. browse catalog products and categories
3. create or manage products as an admin
4. perform cart and order-related operations through the backend API
5. validate access control and business rules through the test suite

This gives a clear picture of how the product behaves in a realistic e-commerce context.

## Architecture highlights

- Spring Boot application with REST controllers
- JPA entities for business domain modeling
- repository/service/controller separation
- Flyway-managed MySQL schema evolution
- JWT resource-server configuration
- H2 smoke tests for fast developer feedback
- MySQL/Testcontainers checks for persistence confidence

## Why this is portfolio-worthy

This project reflects practical engineering judgment and real backend work: it is not just a CRUD list, but a layered system with authentication, persistence, testing discipline, and deployment-oriented design decisions.

The codebase is structured to show that I can build service-oriented applications that are secure, maintainable, and testable—qualities that matter in production development teams.
