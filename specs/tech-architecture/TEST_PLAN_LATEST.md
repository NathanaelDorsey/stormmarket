# Test Plan

## Strategy
Layered testing approach matching the project's architecture:

### Unit Tests (H2 in-memory)
- **Scope:** Service layer and repository unit tests with mock mappers
- **Framework:** JUnit 5 + Spring Test
- **Run:** `./mvnw test` (default profile)
- **Coverage Target:** >= 80% overall, >= 70% per-class for core services
- **Example:** `ProductServiceTest`, `CartServiceTest`, `UserServiceTest`

### MVC Security Tests
- **Scope:** HTTP endpoint validation, auth boundaries, status code verification
- **Framework:** Spring MVC Test (`spring-boot-starter-webmvc-test`), `spring-security-test`
- **Run:** `./mvnw test` (included by default)
- **Coverage:** All `/v1/**` endpoints tested for 200/401/403/404 status codes
- **Example:** `ProductMvcTest`, `AuthControllerTest`, `SecurityIntegrationTest`

### Property-based Tests
- **Framework:** jqwik (1.9.2, test scope)
- **Run:** `./mvnw test` (included by default)
- **Examples:** `CartQuantityFuzzTest` — fuzz cart quantity boundaries
- **Target:** Find edge cases and validation failures not caught by example tests

### Integration Tests (MySQL opt-in)
- **Profile:** `-Drun.mysql.integration=true`
- **Dependency:** Testcontainers MySQL container
- **Scope:** End-to-end flow validation (request → repository → MySQL → response)
- **Run:** `./mvnw test -Drun.mysql.integration=true`
- **Examples:** `MySqlIntegrationTest`

### Static Analysis
- **SpotBugs:** `spotbugs-maven-plugin` 4.10.5.0, reports generated at verify phase
- **FindSecBugs:** Bundled with SpotBugs via exclude filter in `config/spotbugs/exclude.xml`
- **Run:** `./mvnw spotcheck` (or included in verify)
- **Gate:** No new warnings introduced (existing exclude filter preserved)

### Code Formatting
- **Tool:** Palantir Java Format 2.102.0 via `mvnw spotless:check`
- **Current CI:** `<skip>true</skip>` — format check not enforced in CI (tracked as technical debt)
- **Local:** `./mvnw spotless:apply` to format code

## Coverage (JaCoCo 0.8.14)
- **Report Goal:** `jacoco:report` at verify phase
- **Total Coverage Target:** >= 80%
- **Per-Class Target:** >= 70% for core service classes (`ProductService`, `CartService`, `UserService`, `AuthService`)
- **Exclusions:** `target/site/jacoco/excludes` — `*TestRunner*, *Abstract* test classes