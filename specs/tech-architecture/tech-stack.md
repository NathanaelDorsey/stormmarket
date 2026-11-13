# Project Context

## Stack

| Layer | Technology | Version / Notes |
|-------|-----------|-----------------|
| Language | Java | 25 (`<java.version>25</java.version>`) |
| Framework | Spring Boot | 4.1.1 (via `spring-boot-starter-parent`) |
| Build tool | Maven | Maven Wrapper (`mvnw`) |
| Web | Spring Web (Spring MVC) | REST controllers, `/v1` base path |
| Data | Spring Data JPA | Hibernate (default JPA provider) |
| Database (prod) | MySQL | `mysql-connector-j` runtime scope |
| Database (test) | H2 | In-memory, default test path |
| Migrations | Flyway | `spring-boot-starter-flyway` + `flyway-mysql`, 25 versioned SQL migrations |
| Security | Spring Security | Stateless JWT, `SessionCreationPolicy.STATELESS`, CSRF disabled |
| Auth | OAuth2 Resource Server + JWT | RS256 signing, `role` claim → `ROLE_*` authorities, Argon2 password encoding |
| Validation | Jakarta Bean Validation | `spring-boot-starter-validation`, `@Valid` on request DTOs |
| Observability | Spring Boot Actuator | `spring-boot-starter-actuator` |
| Codegen / boilerplate | Lombok | `@RequiredArgsConstructor`, `@Getter`/`@Setter`, annotation processing |
| CSV import | Apache Commons CSV | 1.10.0 |
| Crypto | BouncyCastle | `bcprov-jdk18on` 1.80 |
| Testing | JUnit 5 + Spring Test | `spring-boot-starter-test`, `webmvc-test`, `spring-security-test` |
| Property-based testing | jqwik | 1.9.2, test scope |
| Integration containers | Testcontainers | MySQL + JUnit Jupiter, gated behind `-Drun.mysql.integration=true` |
| Static analysis | SpotBugs + FindSecBugs | `spotbugs-maven-plugin` 4.10.5.0, exclude filter in `config/spotbugs/exclude.xml` |
| Formatting | Spotless | Palantir Java Format 2.102.0 (currently `<skip>true</skip>`) |
| Coverage | JaCoCo | 0.8.14, report at `verify` phase |
| Group / artifact | `com.nathanaeldorsey:stormmarket` | `0.0.1-SNAPSHOT` |

## Architecture

### Pattern
Layered backend, organized by feature (package-per-domain) under `com.nathanaeldorsey.stormmarket`:

```
admin, auth, cart, category, common/exception, config, order, payment, product, user
```

### Data flow (typical request)
```
Client HTTP request
    ↓
SecurityFilterChain (stateless JWT, Argon2, permitList for auth/catalog reads)
    ↓
@RestController (@RequestMapping("/v1/..."), @Valid DTO binding)
    ↓
@Service (@Transactional, business rules, repository calls)
    ↓
Repository (Spring Data JPA / JpaRepository)
    ↓
MySQL (via Flyway-managed schema)
```

### Key structural conventions
- **Controllers** — accept HTTP requests, delegate to services, return DTOs. Public GET catalog routes are permitAll; mutating product/category routes require `ADMIN`.
- **Services** — contain business rules and validation; `@RequiredArgsConstructor` constructor injection; `@Transactional` where writes occur.
- **Repositories** — `JpaRepository` interfaces; custom queries via Spring Data Specifications (`ProductSpecification`) for search/filter/pagination.
- **Entities** — JPA `@Entity` classes (e.g., `Product`, `User`, `Order`, `Cart`, `Category`); `@Version` optimistic locking on `Product`; bidirectional `@ManyToMany` for product↔category.
- **DTOs** — request/response classes/records defining API contracts (e.g., `ProductRequest`, `ProductResponse`, `ApiErrorResponse`).
- **Mappers** — `@Component` or manual classes converting Entity ↔ DTO (e.g., `ProductMapper`, `CartMapper`, `OrderMapper`, `UserMapper`).
- **Error handling** — `@RestControllerAdvice` `GlobalExceptionHandler` converts exceptions into a consistent `ApiErrorResponse` record: `{ timestamp, status, error, message, path, validationErrors }`. Internal details logged server-side, generic message returned to clients.
- **Security** — `SecurityConfig` defines public routes (`/v1/auth/**`, GET `/v1/products/**`, GET `/v1/categories/**`, GET `/v1/files/**`, POST `/v1/orders` guest checkout) and ADMIN-guarded mutating routes. `JwtService` issues 1-hour RS256 JWTs with `role` claim. `Argon2PasswordEncoder` for password hashing.
- **File storage** — toggleable local/S3 via `STORMMARKET_FILE_STORAGE_TYPE`; local default (`uploads/products`, `/v1/files` base URL).
- **Migrations** — 25 versioned Flyway SQL migrations (`V1__create_users.sql` … `V25__add_coupon_code_to_carts_and_orders.sql`) in `src/main/resources/db/migration/`.

## Conventions (Observed)

### Error handling
- Centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) maps domain/framework exceptions to HTTP statuses:
  - `ResourceNotFoundException` → 404
  - `IllegalArgumentException` / malformed JSON / type mismatch → 400
  - `IllegalStateException` / optimistic locking / duplicate resource → 409
  - `AuthorizationDeniedException` → 403
  - `BadCredentialsException` / `DisabledException` → 401
  - `Exception` (unexpected) → 500, logged with stack trace, generic client message
- Validation failures return per-field error maps (`field → message`).

### API design
- REST over HTTP, JSON, `/v1` base path.
- Default Jackson casing (camelCase request/response).
- Pagination via Spring Data `Pageable`, `max-page-size: 100`.
- Bean validation (`@Valid`, `@NotNull`, etc.) on request DTOs.
- Consistent error envelope via `ApiErrorResponse` record.

### Type safety
- Strictly typed Java; entities and DTOs are explicit classes/records.
- Minimal `any`-equivalent risk (Java is statically typed); Lombok reduces boilerplate without weakening types.
- Interfaces used for repository contracts (Spring Data JPA); mappers as plain components.

### Observability
- Actuator dependency present; no custom structured logging or health-check endpoints observed beyond defaults.
- Logs go to stdout via SLF4J/Logback (Spring Boot default).

### Testing
- Layered strategy: fast H2 in-memory tests by default; MySQL via Testcontainers opt-in (`-Drun.mysql.integration=true`).
- Service unit tests mock repositories and mappers; MVC/security tests validate HTTP behavior and auth boundaries; persistence tests use real repositories.
- Property-based testing with jqwik (`CartQuantityFuzzTest`).
- Integration test `MySqlIntegrationTest` excluded from default surefire run; runs under the `mysql-integration` profile.
- README reports: all 175 tests pass on `./mvnw test`.
- `spring-boot-starter-webmvc-test` correctly scoped to `test` (not shipped in app JAR).

## Signals / Active Considerations

| Signal | Detail |
|--------|--------|
| Hardcoded prod datasource | `application.yaml` hardcodes `jdbc:mysql://100.97.128.106:3306/stormmarket`; only password is env-var driven (`STORMMARKET_DB_PASSWORD`). Consider env-var-ing the full URL for environment portability. |
| File storage S3 incomplete | S3 block exists in config but most S3 fields are empty/default; local is the default. No S3 wrapper/integration observed yet. |
| No Stripe/payment provider wrapper | `Payment` entity + `PaymentService` exist, but no external payment-gateway client (e.g., Stripe) is wired in — an integration point if real payments are added. |
| Spotless disabled | `spotless-maven-plugin` has `<skip>true</skip>`; Palantir Java Format configured but not enforced. |
| Optimistic locking on Product only | `@Version` present on `Product`; other entities lack versioning — consider where concurrent writes could corrupt state (cart, order). |
| Default test path is H2 | H2 can mask MySQL-specific SQL/migration behavior; the Testcontainers path is opt-in and requires Docker/Podman. |
| Future improvements (README) | Structured logging/metrics, CI/CD automation, frontend client, expanded admin workflows/reporting. |
