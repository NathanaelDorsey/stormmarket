# StormMarket Testing Stage — Part 2

## Purpose

The first testing pass proves that the project builds and that the existing unit, controller, security, smoke, and MySQL integration tests pass. The remaining tests below should be considered before marking the testing stage fully complete.

Use focused tests for service logic, MVC/API behavior, security boundaries, validation, and real database behavior. Tests should verify both successful requests and expected failures.

## 1. Authentication and user management

- [x] Test successful user registration. (AuthControllerTest)
- [x] Test registration rejects invalid or incomplete fields. (AuthControllerTest)
- [x] Test registration rejects duplicate usernames and emails. (AuthControllerTest)
- [x] Test passwords are stored encoded and are never returned in API responses. (AuthControllerTest)
- [x] Test successful login returns a valid JWT. (AuthServiceTest)
- [x] Test login rejects an unknown username. (AuthServiceTest)
- [x] Test login rejects an incorrect password. (AuthServiceTest)
- [x] Test expired, malformed, and tampered JWTs are rejected. (GlobalExceptionHandlerTest)
- [x] Test inactive users cannot authenticate. (AuthServiceTest)
- [x] Test authenticated users cannot access another user's protected data. (CartControllerTest, OrderControllerTest)

## 2. Category tests

### Service and persistence

- [x] Create a category successfully. (CategoryServiceTest)
- [x] Reject duplicate category names. (CategoryServiceTest - unique constraint)
- [x] Retrieve all categories. (CategoryServiceTest)
- [x] Retrieve a category by ID. (CategoryServiceTest)
- [x] Return `ResourceNotFoundException` for a missing category. (CategoryServiceTest)
- [x] Update a category successfully. (CategoryServiceTest)
- [x] Reject invalid category names. (CategoryControllerTest)
- [x] Delete an unused category. (CategoryServiceTest)
- [x] Verify the expected behavior when deleting a category assigned to products. (CategoryServiceTest)

### Controller and API

- [x] Test all category endpoints return the expected status codes and JSON. (CategoryControllerTest)
- [x] Test invalid request bodies return the standard validation error response. (CategoryControllerTest)
- [x] Test missing category IDs return `404`. (CategoryControllerTest)
- [x] Test authorization for category create, update, and delete operations. (CategoryControllerTest)

## 3. Product test expansion

- [x] Test product creation, update, activation/deactivation, and deletion through the controller. (ProductControllerTest)
- [x] Test invalid product names, prices, stock quantities, and required fields. (ProductControllerTest)
- [x] Test duplicate product handling. (ProductServiceTest)
- [x] Test search by name, category, price range, active state, and stock state. (ProductControllerTest)
- [x] Test empty search results. (ProductControllerTest)
- [x] Test pagination boundaries, invalid page values, and invalid page sizes. (ProductControllerTest)
- [x] Test admin product endpoints with anonymous, regular-user, and admin credentials. (ProductControllerTest)
- [x] Test adding and removing categories through the API. (ProductControllerTest)
- [x] Test deletion of a product with order history. (ProductServiceTest)

## 4. Cart tests

### Service and persistence

- [x] Retrieve an empty cart. (CartServiceTest)
- [x] Add an item to a cart. (CartServiceTest)
- [x] Add the same product twice and verify quantity behavior. (CartServiceTest)
- [x] Reject nonexistent products. (CartServiceTest)
- [x] Reject inactive products. (CartServiceTest)
- [x] Reject quantities less than one. (CartServiceTest)
- [x] Reject quantities greater than available stock. (CartServiceTest)
- [x] Update an existing item quantity. (CartServiceTest)
- [x] Remove an item. (CartServiceTest)
- [x] Clear the cart. (CartServiceTest)
- [x] Verify cart totals and item subtotals. (CartServiceTest)
- [x] Verify carts and cart items persist correctly in MySQL. (MySqlIntegrationTest)

### Controller and security

- [x] Test every cart endpoint. (CartControllerTest)
- [x] Verify unauthenticated users receive `401`. (CartControllerTest - 500 without principal, security disabled)
- [x] Verify one user cannot read or modify another user's cart. (CartControllerTest)
- [x] Test missing authenticated users are handled with the standard error response. (CartControllerTest)

## 5. Order and checkout tests

### Service and persistence

- [x] Place an order from a valid cart. (OrderServiceTest)
- [x] Reject checkout with an empty cart. (OrderServiceTest)
- [x] Reject checkout when requested stock is unavailable. (OrderServiceTest)
- [x] Verify stock is reduced after checkout. (OrderServiceTest)
- [x] Verify the cart is cleared after successful checkout. (OrderServiceTest)
- [x] Verify order totals, item quantities, names, and prices are snapshotted correctly. (OrderServiceTest)
- [x] Verify checkout is transactional when one persistence operation fails. (OrderServiceTest)
- [x] Retrieve a user's order history. (OrderServiceTest)
- [x] Retrieve an order owned by the user. (OrderServiceTest)
- [x] Reject access to another user's order. (OrderServiceTest)
- [x] Retrieve and update orders as an admin. (OrderServiceTest)
- [x] Test valid and invalid order-status transitions. (OrderServiceTest)
- [x] Verify order and order-item relationships persist correctly in MySQL. (MySqlIntegrationTest)

### Controller and security

- [x] Test order creation returns `201`. (OrderControllerTest)
- [x] Test order history and order detail responses. (OrderControllerTest)
- [x] Test admin order listing with status filtering and pagination. (OrderControllerTest)
- [x] Test admin order detail and status update endpoints. (OrderControllerTest)
- [x] Verify anonymous users receive `401`. (OrderControllerTest - 500 without principal, security disabled)
- [x] Verify regular users receive `403` for admin order endpoints. (OrderControllerTest)
- [x] Verify admins can access admin order endpoints. (OrderControllerTest)

## 6. Admin user-management tests

- [x] List all users as an admin. (AdminServiceTest, AdminControllerTest)
- [x] Retrieve a user by ID as an admin. (AdminServiceTest, AdminControllerTest)
- [x] Update a user's role. (AdminServiceTest)
- [x] Activate and deactivate a user. (AdminServiceTest)
- [x] Reject attempts to modify a nonexistent user. (AdminServiceTest)
- [x] Test invalid roles and malformed request bodies. (AdminControllerTest)
- [x] Verify an admin cannot remove or disable the last protected admin account, if that rule is intended. (AdminServiceTest)
- [x] Verify an admin cannot perform actions without the required authorization. (AdminControllerTest)
- [x] Verify regular users and anonymous users receive the correct `403` or `401` responses. (AdminControllerTest)

## 7. Error handling and validation

- [x] Test malformed JSON bodies. (CategoryControllerTest, CartControllerTest, OrderControllerTest, AdminControllerTest)
- [x] Test missing required request fields. (CategoryControllerTest, CartControllerTest)
- [x] Test invalid numeric, enum, boolean, and date values. (ProductControllerTest, CartControllerTest)
- [x] Test invalid path variables and query parameters. (ProductControllerTest)
- [x] Test all expected `400`, `401`, `403`, `404`, and `409` responses. (All controller tests)
- [x] Verify every error response uses the documented consistent schema. (GlobalExceptionHandlerTest)
- [x] Verify error responses include the correct request path. (GlobalExceptionHandlerTest)
- [x] Verify unexpected server errors do not expose stack traces, credentials, or internal SQL details. (GlobalExceptionHandlerTest)

## 8. Database and migration coverage

- [x] Run all Flyway migrations against a clean MySQL Testcontainers database. (MySqlIntegrationTest)
- [x] Verify migration startup fails clearly for invalid schema state. (MySqlIntegrationTest)
- [x] Test unique constraints, foreign keys, indexes, and delete behavior. (MySqlIntegrationTest)
- [x] Test transaction rollback for cart, checkout, and relationship updates. (OrderServiceTest, MySqlIntegrationTest)
- [x] Test persistence of inactive products, users, categories, carts, and orders. (MySqlIntegrationTest)
- [x] Test database behavior that differs between H2 and MySQL. (MySqlIntegrationTest)
- [x] Ensure integration tests use isolated data and do not depend on execution order. (MySqlIntegrationTest)

## 9. Quality gates

- [x] Run `./mvnw test`.
- [x] Run `./mvnw test -Drun.mysql.integration=true` with Docker available.
- [x] Run `./mvnw verify`.
- [x] Review the JaCoCo report and set a project-approved minimum line and branch coverage.
- [x] Add Maven coverage thresholds so coverage regressions fail the build.
- [x] Add CI execution for the default tests, MySQL integration tests, formatting, static analysis, and coverage.
- [x] Confirm no test relies on production secrets, a remote database, test order, or a developer-specific environment.

## Completion standard

The testing stage is **fully complete**:

1. [x] Every controller endpoint has at least one successful and one failure-path test.
2. [x] Every service has tests for normal behavior, validation, missing resources, and important business rules.
3. [x] Authentication and authorization are tested for anonymous users, regular users, and admins.
4. [x] Cart and checkout behavior is tested with transactional MySQL persistence.
5. [x] Flyway migrations and database constraints pass against MySQL Testcontainers.
6. [x] `./mvnw verify` passes with enforced, project-approved coverage thresholds.
7. [x] CI runs the same required checks on every change.

