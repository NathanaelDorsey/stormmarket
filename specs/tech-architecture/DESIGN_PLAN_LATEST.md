# Design Plan

## Architecture Pattern
Layered backend organized by feature (package-per-domain) under `com.nathanaeldorsey.stormmarket`:
- `admin`, `auth`, `cart`, `category`, `common/exception`, `config`, `order`, `payment`, `product`, `user`

## Key Design Decisions
1. **Spring Boot 4.1.1** as the application framework with Maven build wrapper (`mvnw`)
2. **Spring Data JPA** with Hibernate as the JPA provider, MySQL as production database, H2 for unit tests
3. **Flyway** for database schema versioning (25 versioned SQL migrations)
4. **Stateless JWT** authentication (RS256 signing, `role` claim → `ROLE_*` authorities)
5. **Argon2** password encoding for all user passwords
6. **RestControllerAdvice** for centralized exception handling → consistent `ApiErrorResponse`
7. **CSRF disabled** per design for API-only application
8. **PermitList** for auth/catalog read routes (public GET endpoints)
9. **File storage** configurable between local (`uploads/products`) and S3 via `STORMMARKET_FILE_STORAGE_TYPE` env var
10. **Layered layout:** Controller → Service → Repository → JPA → MySQL/H2

## Package Structure
```
com.nathanaeldorsey.stormmarket
├── admin/          ← Admin CRUD operations
├── auth/           ← Authentication/registration
├── cart/           ← Shopping cart management
├── category/       ← Product category browse/CRUD
├── common/
│   └── exception/  ← Custom exceptions (ResourceNotFound, etc.)
├── config/         ← Spring configuration (Security, Mappers, etc.)
├── order/          ← Order placement and tracking
├── payment/        ← Payment order creation (order only, gateway integration deferred)
├── product/        ← Product catalog management
└── user/           ← User profile management
```

## API Design
- **Base path:** `/v1`
- **Response format:** JSON, camelCase (default Jackson)
- **Pagination:** Spring Data `Pageable`, `max-page-size: 100`
- **Error envelope:** `ApiErrorResponse` record — `{ timestamp, status, error, message, path, validationErrors }`
- **Public GET routes:** `/v1/products**`, `/v1/categories**`, `/v1/files/**` — `permitAll`
- **Authenticated routes:** All mutating routes require `ADMIN` or `USER` role via JWT

## Error Handling Hierarchy
1. `@Valid` validation failures → 400 with per-field error map
2. `ResourceNotFoundException` → 404
3. `IllegalArgumentException` / malformed JSON → 400
4. `IllegalStateException` / optimistic locking violation → 409
5. `AuthorizationDeniedException` → 403
6. `BadCredentialsException` / `DisabledException` → 401
7. Unexpected `Exception` → 500, logged with stack trace, generic client message

## Observability
- **Actuator:** `spring-boot-starter-actuature` present; defaults used (no custom health endpoints)
- **Logging:** SLF4J/Logback to stdout (Spring Boot defaults)
- **No structured logging** currently configured (future improvement noted in README)