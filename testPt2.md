# StormMarket Testing Stage — Part 2

## Purpose

The first testing pass proves that the project builds and that the existing unit, controller, security, smoke, and MySQL integration tests pass. The remaining tests below should be considered before marking the testing stage fully complete.

Use focused tests for service logic, MVC/API behavior, security boundaries, validation, and real database behavior. Tests should verify both successful requests and expected failures.

## 1. Authentication and user management

- [ ] Test successful user registration.
- [ ] Test registration rejects invalid or incomplete fields.
- [ ] Test registration rejects duplicate usernames and emails.
- [ ] Test passwords are stored encoded and are never returned in API responses.
- [ ] Test successful login returns a valid JWT.
- [ ] Test login rejects an unknown username.
- [ ] Test login rejects an incorrect password.
- [ ] Test expired, malformed, and tampered JWTs are rejected.
- [ ] Test inactive users cannot authenticate.
- [ ] Test authenticated users cannot access another user's protected data.

## 2. Category tests

### Service and persistence

- [ ] Create a category successfully.
- [ ] Reject duplicate category names.
- [ ] Retrieve all categories.
- [ ] Retrieve a category by ID.
- [ ] Return `ResourceNotFoundException` for a missing category.
- [ ] Update a category successfully.
- [ ] Reject invalid category names.
- [ ] Delete an unused category.
- [ ] Verify the expected behavior when deleting a category assigned to products.

### Controller and API

- [ ] Test all category endpoints return the expected status codes and JSON.
- [ ] Test invalid request bodies return the standard validation error response.
- [ ] Test missing category IDs return `404`.
- [ ] Test authorization for category create, update, and delete operations.

## 3. Product test expansion

- [ ] Test product creation, update, activation/deactivation, and deletion through the controller.
- [ ] Test invalid product names, prices, stock quantities, and required fields.
- [ ] Test duplicate product handling.
- [ ] Test search by name, category, price range, active state, and stock state.
- [ ] Test empty search results.
- [ ] Test pagination boundaries, invalid page values, and invalid page sizes.
- [ ] Test admin product endpoints with anonymous, regular-user, and admin credentials.
- [ ] Test adding and removing categories through the API.
- [ ] Test deletion of a product with order history.

## 4. Cart tests

### Service and persistence

- [ ] Retrieve an empty cart.
- [ ] Add an item to a cart.
- [ ] Add the same product twice and verify quantity behavior.
- [ ] Reject nonexistent products.
- [ ] Reject inactive products.
- [ ] Reject quantities less than one.
- [ ] Reject quantities greater than available stock.
- [ ] Update an existing item quantity.
- [ ] Remove an item.
- [ ] Clear the cart.
- [ ] Verify cart totals and item subtotals.
- [ ] Verify carts and cart items persist correctly in MySQL.

### Controller and security

- [ ] Test every cart endpoint.
- [ ] Verify unauthenticated users receive `401`.
- [ ] Verify one user cannot read or modify another user's cart.
- [ ] Test missing authenticated users are handled with the standard error response.

## 5. Order and checkout tests

### Service and persistence

- [ ] Place an order from a valid cart.
- [ ] Reject checkout with an empty cart.
- [ ] Reject checkout when requested stock is unavailable.
- [ ] Verify stock is reduced after checkout.
- [ ] Verify the cart is cleared after successful checkout.
- [ ] Verify order totals, item quantities, names, and prices are snapshotted correctly.
- [ ] Verify checkout is transactional when one persistence operation fails.
- [ ] Retrieve a user's order history.
- [ ] Retrieve an order owned by the user.
- [ ] Reject access to another user's order.
- [ ] Retrieve and update orders as an admin.
- [ ] Test valid and invalid order-status transitions.
- [ ] Verify order and order-item relationships persist correctly in MySQL.

### Controller and security

- [ ] Test order creation returns `201`.
- [ ] Test order history and order detail responses.
- [ ] Test admin order listing with status filtering and pagination.
- [ ] Test admin order detail and status update endpoints.
- [ ] Verify anonymous users receive `401`.
- [ ] Verify regular users receive `403` for admin order endpoints.
- [ ] Verify admins can access admin order endpoints.

## 6. Admin user-management tests

- [ ] List all users as an admin.
- [ ] Retrieve a user by ID as an admin.
- [ ] Update a user's role.
- [ ] Activate and deactivate a user.
- [ ] Reject attempts to modify a nonexistent user.
- [ ] Test invalid roles and malformed request bodies.
- [ ] Verify an admin cannot remove or disable the last protected admin account, if that rule is intended.
- [ ] Verify an admin cannot perform actions without the required authorization.
- [ ] Verify regular users and anonymous users receive the correct `403` or `401` responses.

## 7. Error handling and validation

- [ ] Test malformed JSON bodies.
- [ ] Test missing required request fields.
- [ ] Test invalid numeric, enum, boolean, and date values.
- [ ] Test invalid path variables and query parameters.
- [ ] Test all expected `400`, `401`, `403`, `404`, and `409` responses.
- [ ] Verify every error response uses the documented consistent schema.
- [ ] Verify error responses include the correct request path.
- [ ] Verify unexpected server errors do not expose stack traces, credentials, or internal SQL details.

## 8. Database and migration coverage

- [ ] Run all Flyway migrations against a clean MySQL Testcontainers database.
- [ ] Verify migration startup fails clearly for invalid schema state.
- [ ] Test unique constraints, foreign keys, indexes, and delete behavior.
- [ ] Test transaction rollback for cart, checkout, and relationship updates.
- [ ] Test persistence of inactive products, users, categories, carts, and orders.
- [ ] Test database behavior that differs between H2 and MySQL.
- [ ] Ensure integration tests use isolated data and do not depend on execution order.

## 9. Quality gates

- [ ] Run `./mvnw test`.
- [ ] Run `./mvnw test -Drun.mysql.integration=true` with Docker available.
- [ ] Run `./mvnw verify`.
- [ ] Review the JaCoCo report and set a project-approved minimum line and branch coverage.
- [ ] Add Maven coverage thresholds so coverage regressions fail the build.
- [ ] Add CI execution for the default tests, MySQL integration tests, formatting, static analysis, and coverage.
- [ ] Confirm no test relies on production secrets, a remote database, test order, or a developer-specific environment.

## Completion standard

The testing stage can be considered fully complete when:

1. Every controller endpoint has at least one successful and one failure-path test.
2. Every service has tests for normal behavior, validation, missing resources, and important business rules.
3. Authentication and authorization are tested for anonymous users, regular users, and admins.
4. Cart and checkout behavior is tested with transactional MySQL persistence.
5. Flyway migrations and database constraints pass against MySQL Testcontainers.
6. `./mvnw verify` passes with enforced, project-approved coverage thresholds.
7. CI runs the same required checks on every change.

