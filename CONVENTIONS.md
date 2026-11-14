# Conventions

Storm Market AI Agent Conventions — generated via `seed-conventions`.

## § Always Green / Shift Left

- **1-10-100 Rationale:** Catching a defect during development costs 1×, during QA costs 10×, in production costs 100×. Preflight and CI must be green before any forward work is merged.
- **Preflight Definition:** `./mvnw verify` must pass (tests + spotbugs + findsecbugs + jacoco reporting) before `git add`/`git commit`.
- **CI Definition:** All checks in the PR pipeline must be green before merge. Revert any commit that leaves CI red.
- **Shift Left:** Run `./mvnw test` locally before committing. Fix any failing test before proceeding.

## § Discovered Defects

- **fix-or-log ladder:** If a defect is discovered (test failure, bug report, security scan), take one of two actions:
  1. **quick-fix** — trivial data-only fix with no logic risk (collapses 6 skills into 2).
  2. **fix-bug** — full bug fix cycle: investigate → develop-tdd → validate-fix.
- **Separate commits:** Discovered defects must be in separate commits from the work that triggered them, with Conventional Commits titles describing the fix.
- **Log if unsure:** If the defect's relation to recent changes is unclear, log it in `specs/bugs/registry.yaml` and request clarification rather than guessing.

## Banned Dismissive Phrases (Pre-existing / Unrelated / Out of Scope)

| Phrase | Response |
|--------|----------|
| "This is a pre-existing issue" | Verify: reproduce, bisect, and document in `specs/bugs/registry.yaml` if truly pre-existing |
| "That's unrelated to this session" | Surface the finding; do not silently ignore gate failures |
| "Not introduced by my changes" | If a new gate failure appears, attribute it to a separate commit with clear title |
| "Out of scope" | Log to `specs/bugs/registry.yaml` with justification; do not close without review |

## Defensive Code Categories (Project-Specific)

The following categories apply to this project. When adding new code, ensure appropriate defenses:

- **Rate limiting:** API endpoints must have rate protection (Spring Cloud Gateway or application-level filter)
- **Retry:** Transient MySQL / Flyway failures should use retry with exponential backoff
- **Circuit breaker:** Outgoing HTTP calls (if any external services are integrated) must have circuit breaker protection
- **Timeout:** All external calls must have configured timeouts; default 30 seconds for HTTP, 60 seconds for DB
- **Graceful degradation:** Non-critical features (optional analytics, reporting) must not bring down the app if they fail

## § Project

Storm Market - Java/Spring Boot e-commerce backend platform for online marketplace transactions
Stack: Java 25, Spring Boot 4.1.1, Maven, MySQL, H2 test database

## § Commands

| Action | Command |
|--------|---------|
| Run    | `./mvnw spring-boot:run` |
| Test   | `./mvnw test` |
| Build  | `./mvnw verify` |
| Lint   | `./mvnw spotcheck` |
| Preflight | `./mvnw verify` |

## § Architecture

Layered backend organized by feature (package-per-domain) under `com.nathanaeldorsey.stormmarket`: admin, auth, cart, category, common/exception, config, order, payment, product, user. REST controllers at `/v1` base path, Spring Data JPA repositories, MySQL production database with Flyway migrations, stateless JWT authentication.

## § Conventions

- camelCase JSON request/response bodies
- Pagination via Spring Data `Pageable` with `max-page-size: 100`
- `@Valid` DTO binding on all request DTOs
- Centralized `GlobalExceptionHandler` converting to `ApiErrorResponse`
- Stateless JWT authentication with RS256 signing and `role` → `ROLE_*` authority mapping
- Argon2 password encoding
- CSRF disabled per design; permitList for auth/catalog read routes
- File storage configurable between local (`uploads/products`) and S3 via `STORMMARKET_FILE_STORAGE_TYPE`

## § Never

- Never dismiss reproducible gate failures as pre-existing or out of scope
- Never proceed on red Preflight or red CI — invoke `quick-fix` or `fix-bug` first
- Never commit secrets or force-push to shared branches
- Never bypass `@Valid` validation or authentication checks

## § Agent Rules

- **Workflow Mandate:** You MUST use the bigpowers skills (e.g. `plan-work`, `develop-tdd`, `orchestrate-project`) to perform tasks. DO NOT write code directly in response to a user prompt like "build this feature".
- **Always Green:** Preflight and CI must be green before forward work. Reproducible gate failures require **fix-or-log** (quick-fix → fix-bug) per CONVENTIONS § Discovered Defects.
- Read `specs/` before writing code.
- All planning and specifications MUST be written to `specs/` (`product/SCOPE_LATEST.yaml`, `release-plan.yaml`, `epics/`) before any code is generated.
- Write the minimum code that solves the stated problem. Nothing extra.
- Run tests after every change. Show evidence before declaring done.
- One clarifying question beats a wrong assumption baked into 200 lines.