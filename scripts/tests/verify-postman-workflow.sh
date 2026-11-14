#!/bin/bash
# Test: Verify Postman CLI workflow command format
# This test ensures the postmanlabs/postman-cli-action@v1 command is properly formatted
# as a folded scalar (>) without quotes around env-var values, using ${{ env.TOKEN }} syntax.

set -euo pipefail

WORKFLOW=".github/workflows/docker-build-deploy.yml"

echo "=== Verifying Postman CLI workflow command format ==="

# Extract commands (folded scalar format: command: >- ...)
# Get lines between "command: >-" and the next step (indented - name:)
ADMIN_CMD=$(sed -n '/Run Postman Collection (Admin) - Docker/,/^[[:space:]]*- name:/p' "$WORKFLOW" | sed -n '/command: >-/,/^[[:space:]]*- name:/p' | head -n -1 | tail -n +2 | sed 's/^[[:space:]]*//' | tr '\n' ' ' | sed 's/ *$//')
USER_CMD=$(sed -n '/Run Postman Collection (User) - Docker/,/^[[:space:]]*- name:/p' "$WORKFLOW" | sed -n '/command: >-/,/^[[:space:]]*- name:/p' | head -n -1 | tail -n +2 | sed 's/^[[:space:]]*//' | tr '\n' ' ' | sed 's/ *$//')

echo "Admin command: $ADMIN_CMD"
echo "User command: $USER_CMD"

# Test 1: Commands contain correct baseUrl
if echo "$ADMIN_CMD" | grep -q 'baseUrl=http://localhost:8081'; then
    echo "✓ PASS: Admin command contains baseUrl=http://localhost:8081"
else
    echo "❌ FAIL: Admin command missing baseUrl=http://localhost:8081"
    exit 1
fi

if echo "$USER_CMD" | grep -q 'baseUrl=http://localhost:8081'; then
    echo "✓ PASS: User command contains baseUrl=http://localhost:8081"
else
    echo "❌ FAIL: User command missing baseUrl=http://localhost:8081"
    exit 1
fi

# Test 2: Uses ${{ env.ADMIN_TOKEN }} syntax (not shell variable)
if echo "$ADMIN_CMD" | grep -q '\${{ env.ADMIN_TOKEN }}'; then
    echo "✓ PASS: Admin command uses \${{ env.ADMIN_TOKEN }} syntax"
else
    echo "❌ FAIL: Admin command missing \${{ env.ADMIN_TOKEN }} syntax"
    exit 1
fi

if echo "$USER_CMD" | grep -q '\${{ env.USER_TOKEN }}'; then
    echo "✓ PASS: User command uses \${{ env.USER_TOKEN }} syntax"
else
    echo "❌ FAIL: User command missing \${{ env.USER_TOKEN }} syntax"
    exit 1
fi

# Test 3: No quotes around env-var values
if echo "$ADMIN_CMD" | grep -q 'env-var "baseUrl'; then
    echo "❌ FAIL: Admin command has quotes around env-var value"
    exit 1
fi
echo "✓ PASS: Admin command has no quotes around env-var values"

if echo "$USER_CMD" | grep -q 'env-var "baseUrl'; then
    echo "❌ FAIL: User command has quotes around env-var value"
    exit 1
fi
echo "✓ PASS: User command has no quotes around env-var values"

echo ""
echo "✅ All Postman CLI workflow format checks passed!"