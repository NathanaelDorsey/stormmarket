# Impact Analysis (Latest)

## Dependents — Stories/Epics That This Module Affects
- **e01 (Product Catalog):** All product-related API consumers; changing product entity fields impacts all DTO mappings and controller responses
- **e03 (Shopping Cart):** Cart quantity validation logic affects order placement; changes to cart repository methods ripple to order service
- **e04 (Order Management):** Order creation from cart depends on cart repository API; changing order entity fields affects response DTOs and database schema
- **e05 (User Account Management):** User entity changes impact authentication flow, profile endpoints, and password encoding

## Affected Modules (Cross-Cutting)
- **`common/exception`:** New exception types or changes to `GlobalExceptionHandler` affect all controllers
- **`config/`:** Security configuration changes affect authentication/authorization for all routes
- **`application.yaml`:** Datasource, JPA, and security property changes affect all environments
- **`Flyway migrations`:** Schema changes (V26+, future) affect all data-dependent code

## Ripple Effects
- **Entity change → Mapper change → Controller DTO change → Client API change**
  - Example: Adding a field to `Product` entity → update `ProductMapper` → update `ProductResponse` record → update all controller return types → update frontend (if/when client exists)
- **Security config change → Auth boundary change → All mutating routes require new role mapping**
  - Example: Adding `ROLE_MODERATOR` → update `SecurityConfig` → update all `@PreAuthorize` expressions → update role check logic in services
- **Flyway migration → Schema change → All entities with `@Table` annotations may need `updatedColumns` or `schemaName` adjustment**

## Impact Mitigation Strategies
1. **Write unit tests before refactoring** — ensure green CI before and after changes
2. **Use Conventional Commits** — clear titles describe impact scope
3. **Run full Preflight (`./mvnw verify`)** — validate no regressions before committing
4. **Update `specs/state.yaml` `commit` field** after VCS changes to track hash
5. **Document impact in `specs/epics/`** — before making breaking changes, update epic capsules