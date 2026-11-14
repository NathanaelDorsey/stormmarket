# Refactor Plan (Latest)

## Observed Code Smells / Technical Debt
- **SpotBugs/FindSecBugs exclude filter** in `config/spotbugs/exclude.xml` — existing suppressions; new warnings must be addressed before commit
- **Spotless (Palantir Java Format)** currently `<skip>true</skip>` in CI — technical debt ticket tracked; local `./mvnw spotless:apply` available
- **H2 test database** can mask MySQL-specific SQL behavior — Testcontainers path opt-in only (`-Drun.mysql.integration=true`)
- **Default test path is H2** — may not catch MySQL-specific dialect issues; integration tests opt-in only
- **Hardcoded prod datasource** in `application.yaml` — connection details masked in doc, but worth reviewing for rotation

## Prioritized Refactor Candidates (by impact / effort)
1. **Enable Spotless in CI** — unskip `<spotless.check>` to enforce formatting on every PR
2. **Add structured logging** — replace raw SLF4J statements with structured JSON logs (correlates with trace-requirements)
3. **Introduce @Entity versioning** on `Order` and `Cart` entities (currently only `Product` has `@Version`)
4. **Extract common mappers** to shared `common/mapper` package — reduce duplication across `ProductMapper`, `CartMapper`, `OrderMapper`, `UserMapper`
5. **Add integration test suite** — expand MySQL integration tests beyond the single `MySqlIntegrationTest`

## Refactor Backlog (speculative, awaiting discovery)
- Replace `@ManyToMany` product↔category with join table for performance (currently bidirectional)
- Introduce `ProductSpecification` custom queries as the sole query approach (currently raw JPA queries observed)
- Add Redis caching for frequently-read product catalog (currently no caching layer)
- Implement API rate limiting (currently no rate protection observed)