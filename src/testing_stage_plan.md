# StormMarket Testing Stage

## Why this approach

The application is tested as a layered Spring REST service:

- **Unit tests** isolate business logic and keep feedback fast.
- **MVC and security tests** verify HTTP contracts, validation, exception responses, and authorization without needing a full persistence layer.
- **MySQL Testcontainers integration tests** validate JPA behavior, Flyway migrations, and SQL assumptions against a real MySQL engine.
- **A full-context smoke test** verifies that configuration, security, repositories, and controllers compose correctly in a default local/test setup.

This is preferred over a broad end-to-end-only strategy because failures stay more localized and are much faster to diagnose. MySQL Testcontainers is preferred over H2 for persistence validation because H2 can accept SQL, types, and transaction behavior that differ from production MySQL.

The default local/CI path is intentionally lightweight:

- `./mvnw test` runs the default H2 smoke suite.
- `./mvnw test -Drun.mysql.integration=true` enables the MySQL/Testcontainers validation path when DB realism is needed.

This balances speed and realism:

- H2 is enough for fast smoke coverage, application wiring, and daily feedback loops.
- MySQL/Testcontainers are used only when database behavior matters, so we avoid slowing every developer iteration.
- Docker is valuable because it creates a disposable, deterministic MySQL environment for local validation and CI without manual database setup.

This is better than an H2-only strategy because it misses MySQL-specific migration and SQL issues. It is also better than a MySQL-only strategy because it slows down everyday development and makes quick iteration harder.

## Test boundaries

Tests assert observable behavior and API contracts. Service unit tests mock repositories and mappers; MVC/security tests validate HTTP behavior and auth boundaries; persistence tests use real repositories and a disposable database. Shared mutable fixtures and test-order coupling are avoided.

The default smoke test uses an isolated H2 profile and does not require a remote database or production secrets. The MySQL integration tests are explicitly gated behind `-Drun.mysql.integration=true`, and Docker must be available for that profile.

The current security policy allows public GET access to product and category catalog endpoints while protecting admin mutating endpoints and authenticated routes. This is intentional and is covered by the security tests.

## Running tests

```text
./mvnw test
./mvnw test -Drun.mysql.integration=true
./mvnw verify
```

The first command runs the default H2 smoke suite. The second runs the MySQL/Testcontainers integration suite when Docker is available. The verify phase keeps the full quality gate, including formatting and coverage checks.

## Current project status

The project is in a working state with a layered backend, real security rules, migration-driven persistence, and a test suite that intentionally separates fast smoke validation from deeper MySQL parity validation.
