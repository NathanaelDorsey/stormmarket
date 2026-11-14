#!/bin/bash
#
# Smoke test script for post-deployment verification
# Run against a deployed Storm Market API instance
#
# Usage: ./scripts/smoke-test.sh <base_url> [admin_token]
# Example: ./scripts/smoke-test.sh https://api.stormmarket.com

set -e

BASE_URL="${1:-http://localhost:8080}"
ADMIN_TOKEN="${2:-}"

echo "=========================================="
echo "Storm Market Smoke Tests"
echo "Base URL: $BASE_URL"
echo "=========================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Test counter
PASSED=0
FAILED=0

test_endpoint() {
    local name="$1"
    local method="$2"
    local endpoint="$3"
    local expected_status="$4"
    local headers="${5:-}"
    local body="${6:-}"

    echo -n "Testing $name... "

    if [ -n "$body" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            $headers \
            -d "$body")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            $headers)
    fi

    http_code=$(echo "$response" | tail -n1)
    body_content=$(echo "$response" | sed '$d')

    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}PASS${NC} (HTTP $http_code)"
        ((PASSED++))
    else
        echo -e "${RED}FAIL${NC} (Expected HTTP $expected_status, got $http_code)"
        echo "Response: $body_content"
        ((FAILED++))
    fi
}

# 1. Health check
test_endpoint "Health Check" "GET" "/actuator/health" "200"

# 2. Public catalog endpoints
test_endpoint "Product Catalog" "GET" "/v1/products" "200"
test_endpoint "Product Detail (404)" "GET" "/v1/products/999999" "404"
test_endpoint "Categories List" "GET" "/v1/categories" "200"
test_endpoint "Category Detail (404)" "GET" "/v1/categories/999999" "404"

# 3. Auth endpoints
test_endpoint "Invalid Login" "POST" "/v1/auth/login" "401" "" '{"username":"invalid","password":"wrong"}'

# 4. Protected endpoints (without token - should return 401/403)
if [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${YELLOW}Warning: No admin token provided, skipping authenticated tests${NC}"
else
    AUTH_HEADER="-H \"Authorization: Bearer $ADMIN_TOKEN\""
    
    test_endpoint "Create Product (Admin)" "POST" "/v1/products" "201" "$AUTH_HEADER" '{"name":"Test","description":"Test","price":10.0,"categoryId":1,"stock":5}'
    test_endpoint "Create Category (Admin)" "POST" "/v1/categories" "201" "$AUTH_HEADER" '{"name":"Test Category","description":"Test"}'
    test_endpoint "List Users (Admin)" "GET" "/v1/admin/users" "200" "$AUTH_HEADER"
    test_endpoint "Manage Cart (User)" "POST" "/v1/cart" "401" "" '{"action":"add","productId":1,"quantity":1}'
fi

# Summary
echo ""
echo "=========================================="
echo "Results: ${PASSED} passed, ${FAILED} failed"
echo "=========================================="

if [ $FAILED -gt 0 ]; then
    echo -e "${RED}Some tests failed!${NC}"
    exit 1
else
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
fi