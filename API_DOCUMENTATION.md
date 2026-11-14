# Storm Market API Documentation

*Generated from Postman collection and OpenAPI specification.*
*Backend: Java 25, Spring Boot 4.1.1, Maven*
*Base URL: `http://localhost:8080/v1`*

---
## Table of Contents

1. [Authentication](#authentication)
2. [Public Endpoints (No Auth Required)](#public-endpoints)
3. [Authenticated Endpoints](#authenticated-endpoints)
4. [Admin Endpoints](#admin-endpoints)
5. [Error Response Format](#error-response-format)
6. [Data Models](#data-models)
7. [Test Coverage](#test-coverage)

---
## Authentication

This API uses **Bearer Token Authentication** (JWT — RS256 signing).

- **How to obtain a token**:
  - `POST /v1/auth/login` — with valid username/password
  - `POST /v1/auth/register` — to create a new account
- **How to send a token**:
  - `Authorization: Bearer <token>` header on all protected requests
- **Token scope**:
  - `ROLE_USER` — access user-level endpoints (cart, orders)
  - `ROLE_ADMIN` — access admin-only endpoints (CRUD on products/categories/users)
- **Public routes** (no token required):
  - `GET /v1/products**`, `GET /v1/products/{id}`
  - `GET /v1/categories**`, `GET /v1/categories/{id}`
  - All `/v1/auth/**` endpoints

**Auth flow example**:
1. `POST /v1/auth/login` → returns JWT RS256 token
2. Use token in `Authorization: Bearer <token>` header
3. Call protected endpoints; receive `401` if token invalid/expired, `403` if insufficient role

---
## Public Endpoints (Authentication Not Required)

### `GET /v1/products`

**Description**: Public product catalog listing with pagination, sorting, and filtering.

**Parameters**:

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `page` | Integer | No | Page number (default: 1) |
| `size` | Integer | No | Items per page (default: 20, max: 100) |
| `sort` | String | No | Sort field (e.g., "name", "price", "createdAt") |

**Successful response (200)**:

- Returns a JSON array of product summaries
- **Response format**: `[ { id, name, price, ... }, ... ]`
- **ApiErrorResponse envelope is NOT present on 200**

**Test coverage** (Postman):

- `pm.test("Status code is 200", ...)` — validates 200 status
- `pm.test("Response is an array", ...)` — validates JSON array response
- `pm.test("ApiErrorResponse envelope not present on success", ...)` — ensures no error fields on success

---

### `GET /v1/products/{productId}`

**Description**: Public product detail retrieval by ID.

**Path parameter**:

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `productId` | Integer | Yes | Product's unique identifier |

**Successful response (200)**:

- Returns product detail object
- **Response format**: `{ id, name, description, price, stock, ... }`

**Error responses**:

| Status | Meaning |
|--------|---------|
| `404` | Product not found — returns `ApiErrorResponse` envelope |
| `500` | Internal server error — returns `ApiErrorResponse` envelope |

**Test coverage**:

- `200` → validates product object present, no ApiErrorResponse envelope
- `404` → validates `timestamp`, `status`, `error`, `message` fields present
- `500` → validates `timestamp`, `status`, `error`, `message` fields present

---

### `GET /v1/categories`

**Description**: Public category browsing with hierarchical display.

**Successful response (200)**:

- Returns JSON array of category objects
- Same structure as individual category detail

**Test coverage**:

- `pm.test("Status code is 200", ...)`
- `pm.test("Response is an array", ...)`
- `pm.test("ApiErrorResponse envelope not present on success", ...)`

---

### `GET /v1/categories/{categoryId}`

**Description**: Public category detail retrieval by ID.

**Path parameter**: `categoryId` (Integer, required)

**Successful response (200)** → returns category object

**Error response (404)** → returns `ApiErrorResponse` envelope with `timestamp`, `status`, `error`, `message`

---
## Authenticated Endpoints (JWT Token Required)

### `POST /v1/cart`

**Description**: Shopping cart management — add items, update quantities, or remove items.

**Authentication**: `USER` role required

**Request body**:

```json
{
  "action": "add",   // "add" | "remove" | "update"
  "productId": 1,    // Product identifier
  "quantity": 2      // Item quantity, minimum 1
}
```

**Successful response (200)**:

- Returns cart object with updated items
- **Response format**: `{ id, userId, items: [ { productId, quantity, price }, ... ] }`

**Error responses**:

| Status | Meaning |
|--------|---------|
| `400` | Invalid action or parameters — returns `ApiErrorResponse` |
| `401` | Unauthorized — invalid/missing token — returns `ApiErrorResponse` |
| `404` | Product not found in catalog |

**Test coverage**:

- `pm.test("Status is 200, 401, or 404", ...)` — status code validation
- `pm.test("Response has cart object on success 200", ...)` — cart object present
- `pm.test("Unauthorized (401) returns ApiErrorResponse", ...)` — error envelope fields

---

### `POST /v1/orders`

**Description**: Create a new order from the shopping cart.

**Authentication**: `USER` or `ADMIN` role required

**Request body**:

```json
{
  "shippingAddress": {
    "street": "123 Test St",
    "city": "Testville",
    "zipCode": "12345",
    "country": "USA"
  },
  "paymentMethod": "credit_card"
}
```

**Successful response (201)**:

- Returns order object with total and status
- **Response format**: `{ id, userId, items: [ ... ], total: <BigDecimal>, status: "PENDING" }`

**Error responses**:

| Status | Meaning |
|--------|---------|
| `400` | Missing required fields or invalid data — returns `ApiErrorResponse` |
| `401` | Unauthorized — returns `ApiErrorResponse` |
| `403` | Forbidden — insufficient role — returns `ApiErrorResponse` |

**Test coverage**:

- Status: `400`, `401`, `403` validation
- `Unauthorized (401) returns ApiErrorResponse` — full envelope fields
- `Bad request (400) — missing required fields`

---

### `GET /v1/orders/{orderId}`

**Description**: Get order details by order ID.

**Path parameter**: `orderId` (Integer, required)

**Authentication**: `USER` or `ADMIN` role required

**Successful response (200)** → returns order object

**Error responses**:

| Status | Meaning |
|--------|---------|
| `401` | Unauthorized — `ApiErrorResponse` |
| `403` | Forbidden — insufficient role — `ApiErrorResponse` |
| `404` | Order not found — `ApiErrorResponse` |

**Test coverage**:

- All three error codes validate ApiErrorResponse envelope fields

---
## Admin Endpoints (ADMIN Role Required)

All admin endpoints require `ROLE_ADMIN` in the JWT token. Admin endpoints follow the same error patterns as authenticated endpoints, with `401` (unauthenticated) and `403` (forbidden for non-admin) always possible.

### `GET /v1/admin/users`

**Description**: Admin endpoint to list all users.

**Authentication**: `ADMIN` role required

**Successful response (200)** → JSON array of user objects

**Error responses**: `401` (unauthenticated), `403` (not admin)

---

### `GET /v1/admin/users/{userId}`

**Description**: Admin endpoint to get user by ID.

**Path parameter**: `userId` (Integer, required)

**Error responses**: `401`, `403`, `404` — all return `ApiErrorResponse`

---

### `POST /v1/admin/categories`

**Description**: Admin endpoint to create a new category.

**Request body**: `{ "name": "Electronics", "description": "Electronic devices" }`

**Error responses**: `400` (invalid data), `401` (unauthenticated), `403` (not admin) — all return `ApiErrorResponse`

---

### `POST /v1/admin/products`

**Description**: Admin endpoint to create a new product.

**Request body**: `{ "name": "Test Product", "description": "A test product", "price": 99.99, "categoryId": 1, "stock": 100 }`

**Error responses**: `400` (invalid product data), `401`, `403` — all return `ApiErrorResponse` with full envelope fields

---

## Error Response Format (`ApiErrorResponse`)

All `4xx` and `5xx` responses follow a standardized error envelope. This ensures clients can reliably parse errors without reading implementation details.

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `timestamp` | String (date-time) | Time when the error occurred | `"2025-11-13T08:35:12.453Z"` |
| `status` | Integer | HTTP status code | `404` |
| `error` | String | Short error summary | `"Not Found"` |
| `message` | String | Human-readable error message | `"No product found with id: 999"` |
| `path` | String | Request URL path that generated the error | `"/v1/products/999"` |
| `validationErrors` | Object (optional) | Field-level validation errors | `{ "price": ["Must be greater than 0"] }` |

**Example error response**:

```json
{
  "timestamp": "2025-11-13T08:35:12.453Z",
  "status": 404,
  "error": "Not Found",
  "message": "No product found with id: 999",
  "path": "/v1/products/999"
}
```

**Test coverage** (Postman, all error paths):

- `pm.test("Unauthorized (401) returns ApiErrorResponse", ...)` — validates all 5 fields
- `pm.test("Not found (404) returns ApiErrorResponse", ...)` — validates all 5 fields
- `pm.test("Bad request (400) returns ApiErrorResponse", ...)` — validates with validationErrors if present

---
## Data Models

### `Product`

| Field | Type | Description |
|-------|------|-------------|
| `id` | Integer | Product's unique identifier |
| `name` | String | Product name |
| `description` | String | Product description |
| `price` | Number | Product price (float) |
| `stock` | Integer | Product stock quantity |
| `active` | Boolean | Whether product is active |
| `createdAt` | String (date-time) | Creation timestamp |
| `updatedAt` | String (date-time) | Last update timestamp |

### `Category`

| Field | Type | Description |
|-------|------|-------------|
| `id` | Integer | Category's unique identifier |
| `name` | String | Category name |
| `description` | String | Category description |
| `active` | Boolean | Whether category is active |
| `createdAt` | String (date-time) | Creation timestamp |
| `updatedAt` | String (date-time) | Last update timestamp |

### `User`

| Field | Type | Description |
|-------|------|-------------|
| `id` | Integer | User's unique identifier |
| `username` | String | User's chosen username |
| `email` | String (format: email) | User's email address |
| `password` | String | Hashed password (Argon2) |
| `active` | Boolean | Whether user is active |
| `role` | String | User role (`ROLE_USER`, `ROLE_ADMIN`) |
| `createdAt` | String (date-time) | Creation timestamp |
| `updatedAt` | String (date-time) | Last update timestamp |

### `ApiErrorResponse`

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | String (date-time) | Error timestamp |
| `status` | Integer | HTTP status code |
| `error` | String | Error name |
| `message` | String | Human-readable message |
| `path` | String | Request URL path |
| `validationErrors` | Object (optional) | Field-level errors |

### `Cart`

| Field | Type | Description |
|-------|------|-------------|
| `id` | Integer | Cart's unique identifier |
| `userId` | Integer | User's unique identifier |
| `items` | Array | Cart items |

### `CartItem`

| Field | Type | Description |
|-------|------|-------------|
| `id` | Integer | Cart item's unique identifier |
| `productId` | Integer | Product's unique identifier |
| `quantity` | Integer | Item quantity |
| `price` | Number | Product price at time of addition |

### `Order`

| Field | Type | Description |
|-------|------|-------------|
| `id` | Integer | Order's unique identifier |
| `userId` | Integer | User's unique identifier |
| `items` | Array | Order items |
| `total` | Number | Total order amount |
| `status` | String | Order status (`PENDING`, `CONFIRMED`, `SHIPPED`, `COMPLETED`, `CANCELLED`) |
| `createdAt` | String (date-time) | Order creation timestamp |

### `OrderItem`

| Field | Type | Description |
|-------|------|-------------|
| `productId` | Integer | Product's unique identifier |
| `quantity` | Integer | Item quantity |
| `price` | Number | Product price at time of order |

---
## Test Coverage Summary

| Area | Status |
|------|--------|
| **Status code validation** | ✅ All 21 request files test 3+ codes |
| **Response shape validation** | ✅ Property checks on success/error |
| **Authentication flow** | ✅ Full flow tested (login → token → protected) |
| **Public catalog access** | ✅ Status + array + pagination |
| **Admin vs user boundaries** | ✅ Different tokens, different results |
| **ApiErrorResponse envelope** | ✅ **Now on ALL error responses** |
| **Pagination validation** | ✅ On catalog/category lists |
| **Input validation details** | ✅ **On 8 key endpoints** |
| **Auth edge cases** | ✅ **On all Protected + Admin** |
| **Property-based testing** | ✅ **4 jqwik test classes (801 lines)** |
| — Cart quantity boundaries | |
| — Order total calculation | |
| — Stock consistency | |
| — Price validation ranges | |

---
## Generation Information

- **Source**: Postman Collection (`Storm Market API`) + OpenAPI Specification (`openapi-storm-market.yaml`)
- **Total request files**: 21 (Public: 5, Protected: 3, Admin: 13)
- **Test enhancements**: ApiErrorResponse validation, pagination, input validation, auth edge cases
- **Property-based tests**: 4 jqwik classes, 801 lines, covering cart/order/stock/price scenarios
- **Generated**: November 2025