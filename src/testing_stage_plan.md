# StormMarket Testing Stage

## Why this approach

The application is tested as a layered Spring REST service:

- **Unit tests** isolate service business rules and keep feedback fast.
- **MVC/security slice tests** verify HTTP contracts, validation, exception responses, and authorization without requiring persistence.
- **Integration tests** use MySQL in Testcontainers so JPA behavior, constraints, and Flyway migrations are tested against the same database family used in production.
- **The full-context smoke test** verifies that configuration, migrations, security, repositories, and controllers compose successfully.

This is preferred over only end-to-end tests because failures remain localized and fast to diagnose. MySQL Testcontainers is preferred over H2 for persistence tests because an in-memory database can accept SQL, types, constraints, and transaction behavior that differ from production MySQL.

## Test boundaries

Tests assert observable behavior and API contracts. Service unit tests mock repositories and mappers; MVC slice tests mock services; persistence tests use real repositories and a disposable database. Shared mutable fixtures and ordering between tests are avoided.

The full-context smoke test uses an isolated H2 test profile and does not use the application's configured remote database or credentials. MySQL Testcontainers integration tests are explicitly gated behind `-Drun.mysql.integration=true`; Docker is required for that profile.

## Running tests

```text
./mvnw.cmd test
./mvnw.cmd -Drun.mysql.integration=true test
./mvnw.cmd verify
```

The first command runs the test suite. The second also runs formatting, coverage reporting, and static analysis configured in Maven.
