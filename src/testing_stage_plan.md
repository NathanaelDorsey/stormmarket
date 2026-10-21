# StormMarket Testing Stage

## Why this approach

The application is tested as a layered Spring REST service:

- **Unit tests** isolate service business rules and keep feedback fast.
- **MVC/security slice tests** verify HTTP contracts, validation, exception responses, and authorization without requiring persistence.
- **Integration tests** use MySQL in Testcontainers so JPA behavior, constraints, and Flyway migrations are tested against the same database family used in production.
- **The full-context smoke test** verifies that configuration, migrations, security, repositories, and controllers compose successfully.

This is preferred over only end-to-end tests because failures remain localized and fast to diagnose. MySQL Testcontainers is preferred over H2 for persistence tests because an in-memory database can accept SQL, types, constraints, and transaction behavior that differ from production MySQL.

The default local/CI path is intentionally fast and lightweight:

- `./mvnw test` runs the default smoke suite against H2.
- `./mvnw -Drun.mysql.integration=true test` enables MySQL/Testcontainers verification when production parity is needed.

This balances speed and realism:

- H2 is sufficient for smoke coverage, application wiring, and fast feedback loops.
- MySQL/Testcontainers are used only when database behavior matters, avoiding unnecessary slowdowns for every developer iteration.
- Docker is useful because it gives us a disposable, deterministic MySQL environment in CI and local development without manual database setup.

This is better than a single H2-only strategy because it misses MySQL-specific SQL and migration issues. It is also better than a single MySQL-only strategy because it slows every developer run and makes everyday feedback less efficient.

## Test boundaries

Tests assert observable behavior and API contracts. Service unit tests mock repositories and mappers; MVC slice tests mock services; persistence tests use real repositories and a disposable database. Shared mutable fixtures and ordering between tests are avoided.

The full-context smoke test uses an isolated H2 test profile and does not use the application's configured remote database or credentials. MySQL Testcontainers integration tests are explicitly gated behind `-Drun.mysql.integration=true`; Docker is required for that profile.

## Running tests

```text
./mvnw test
./mvnw -Drun.mysql.integration=true test
./mvnw verify
```

The first command runs the default H2 smoke test suite. The second runs the MySQL/Testcontainers integration suite when Docker is available. The verify phase keeps the full quality gate, including formatting and coverage reporting.
