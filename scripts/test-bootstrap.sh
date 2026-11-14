#!/bin/bash
#
# Local bootstrap script for running Postman collection tests
# This replicates what the CI pipeline (postman-cicd.yml) does:
# 1. Starts Spring Boot with H2 in-memory test profile
# 2. Creates test users via API (ci_user) + registers admin (ciadmin)
# 3. Updates admin user role to ADMIN via direct H2 SQL
# 4. Gets JWT auth tokens
# 5. Creates test data (categories, products) via admin API
# 6. Runs Postman collection with proper authentication
# 7. Cleans up on exit
#
# NOT persistent - only affects the in-memory H2 database
# Usage: ./scripts/test-bootstrap.sh [admin|user]
#

set -e

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Storm Market Postman Test Bootstrap${NC}"
echo -e "${GREEN}========================================${NC}"

# Function to cleanup on exit
cleanup() {
    echo -e "${YELLOW}Stopping application...${NC}"
    if [ -n "$APP_PID" ]; then
        kill "$APP_PID" 2>/dev/null || true
        wait "$APP_PID" 2>/dev/null || true
    fi
    echo -e "${GREEN}Application stopped. H2 database cleared.${NC}"
}

trap cleanup EXIT INT TERM

# Step 1: Start the application with H2 test profile
echo -e "${YELLOW}Starting Spring Boot application with H2 test profile...${NC}"

chmod +x ./mvnw
# Clean any stale data.sql files that might be copied to target
rm -f /var/home/viper/IdeaProjects/stormmarket/target/classes/data.sql
rm -f /var/home/viper/IdeaProjects/stormmarket/target/test-classes/data.sql

# Start the app in the background - use H2 with all config via command line
SPRING_PROFILES_ACTIVE=test ./mvnw spring-boot:run \
    -Dspring.datasource.url=jdbc:h2:mem:stormmarket_test \
    -Dspring.datasource.driver-class-name=org.h2.Driver \
    -Dspring.datasource.username=sa \
    -Dspring.datasource.password= \
    -Dspring.jpa.hibernate.ddl-auto=create-drop \
    -Dspring.flyway.enabled=false \
    -Dspring.sql.init.mode=never \
    -Dspring.security.oauth2.resourceserver.jwt.public-key-location=classpath:jwt/public-key.pem \
    > app.log 2>&1 &
APP_PID=$!

echo "App PID: $APP_PID"

# Wait for application to be ready
echo -e "${YELLOW}Waiting for application to start...${NC}"
for i in {1..60}; do
    if curl -s http://localhost:8080/v1/products > /dev/null 2>&1; then
        echo -e "${GREEN}Application is ready!${NC}"
        break
    fi
    echo "Waiting for app... ($i/60)"
    sleep 5
done

if [ "$i" -eq 60 ]; then
    echo -e "${RED}Application failed to start!${NC}"
    cat app.log
    exit 1
fi

# Step 2: Create test users via API
echo -e "${YELLOW}Creating test users via API...${NC}"

# Admin user (will be registered with default USER role, then updated to ADMIN)
curl -s -X POST http://localhost:8080/v1/auth/register \
    -H "Content-Type: application/json" \
    -d '{"username":"ciadmin","email":"admin@test.com","password":"TestAdminPass123!","firstName":"Test","lastName":"Admin"}' > /dev/null 2>&1 || true

# Regular user
curl -s -X POST http://localhost:8080/v1/auth/register \
    -H "Content-Type: application/json" \
    -d '{"username":"ciuser","email":"user@test.com","password":"TestUserPass123!","firstName":"Test","lastName":"User"}' > /dev/null 2>&1 || true

# Step 3: Update admin user role to ADMIN via H2 SQL
echo -e "${YELLOW}Updating admin user role to ADMIN...${NC}"
sleep 2
java -cp /var/home/viper/.m2/repository/com/h2database/h2/2.4.240/h2-2.4.240.jar org.h2.tools.Shell \
    -url "jdbc:h2:mem:stormmarket_test" -user sa \
    -sql "UPDATE users SET role='ADMIN' WHERE username='ciadmin';" > /dev/null 2>&1 || true

# Step 4: Get JWT tokens
echo -e "${YELLOW}Getting JWT tokens...${NC}"

# Login as admin
ADMIN_LOGIN=$(curl -s -X POST http://localhost:8080/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"ciadmin","password":"TestAdminPass123!"}')
ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | jq -r '.token')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo -e "${RED}Failed to get admin token!${NC}"
    echo "Login response: $ADMIN_LOGIN"
    exit 1
fi

echo -e "${GREEN}Admin token obtained${NC}"

# Login as user
USER_LOGIN=$(curl -s -X POST http://localhost:8080/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"ciuser","password":"TestUserPass123!"}')
USER_TOKEN=$(echo "$USER_LOGIN" | jq -r '.token')

if [ -z "$USER_TOKEN" ] || [ "$USER_TOKEN" = "null" ]; then
    echo -e "${RED}Failed to get user token!${NC}"
    echo "Login response: $USER_LOGIN"
    exit 1
fi

echo -e "${GREEN}User token obtained${NC}"

# Step 5: Create test data via admin API
echo -e "${YELLOW}Creating test data (categories, products) via admin API...${NC}"

# Create categories
CATEGORY_IDS=()
for CAT_NAME in "Electronics" "Clothing" "Books"; do
    RESPONSE=$(curl -s -X POST http://localhost:8080/v1/categories \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -d "{\"name\":\"$CAT_NAME\",\"description\":\"$CAT_NAME category\"}")
    CAT_ID=$(echo "$RESPONSE" | jq -r '.category.id // .id')
    CATEGORY_IDS+=("$CAT_ID")
    echo "Created category: $CAT_NAME (ID: $CAT_ID)"
done

# Create products
PRODUCT_DATA=(
    "Smartphone X|699.99|Latest smartphone with 128GB storage|${CATEGORY_IDS[0]}|50"
    "Laptop Pro|1299.99|Professional laptop with 16GB RAM|${CATEGORY_IDS[0]}|30"
    "Cotton T-Shirt|19.99|100% cotton, available in sizes S-XXL|${CATEGORY_IDS[1]}|100"
    "Denim Jeans|49.99|Classic fit denim jeans|${CATEGORY_IDS[1]}|75"
    "The Great Novel|14.99|A bestselling fiction book|${CATEGORY_IDS[2]}|200"
    "Physics Handbook|85.00|University level physics reference|${CATEGORY_IDS[2]}|25"
)

for PROD in "${PRODUCT_DATA[@]}"; do
    IFS='|' read -r NAME PRICE DESC CAT_ID STOCK <<< "$PROD"
    RESPONSE=$(curl -s -X POST http://localhost:8080/v1/products \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -d "{\"name\":\"$NAME\",\"description\":\"$DESC\",\"price\":$PRICE,\"categoryId\":$CAT_ID,\"stock\":$STOCK}")
    PROD_ID=$(echo "$RESPONSE" | jq -r '.product.id // .id')
    echo "Created product: $NAME (ID: $PROD_ID)"
done

# Verify data loaded
PRODUCTS_COUNT=$(curl -s http://localhost:8080/v1/products | jq 'length')
echo "Products count: $PRODUCTS_COUNT"

# Step 5: Run Postman Collection
echo -e "${YELLOW}Running Postman Collection...${NC}"

# Determine which token to use
if [ "$1" = "user" ]; then
    TOKEN_VAR="authToken"
    TOKEN_VALUE="$USER_TOKEN"
    echo -e "${GREEN}Running Postman collection with USER token${NC}"
else
    TOKEN_VAR="authToken"
    TOKEN_VALUE="$ADMIN_TOKEN"
    echo -e "${GREEN}Running Postman collection with ADMIN token${NC}"
fi

postman collection run "/var/home/viper/IdeaProjects/stormmarket/postman/collections/Storm Market API" \
    --global-var "baseUrl=http://localhost:8080" \
    --global-var "authToken=$TOKEN_VALUE" \
    --bail=false \
    --output postman-results.json

echo -e "${GREEN}Postman collection run complete!${NC}"
echo -e "${GREEN}Results saved to: postman-results.json${NC}"

# The app will be stopped on EXIT trap
echo -e "${GREEN}H2 in-memory database has been cleared.${NC}"