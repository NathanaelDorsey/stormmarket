#!/bin/bash
# Test: Verify Postman CLI workflow command format
# This test ensures the postmanlabs/postman-cli-action@v1 command is properly formatted
# as a single line without quotes around env-var values, so that --env-var arguments
# are correctly applied when the action executes `postman $POSTMAN_COMMAND` without eval.

set -euo pipefail

WORKFLOW=".github/workflows/docker-build-deploy.yml"

echo "=== Verifying Postman CLI workflow command format ==="

# Extract commands
ADMIN_CMD=$(grep -A 4 'Run Postman Collection (Admin) - Docker' "$WORKFLOW" | grep 'command:' | sed 's/.*command: *//')
USER_CMD=$(grep -A 4 'Run Postman Collection (User) - Docker' "$WORKFLOW" | grep 'command:' | sed 's/.*command: *//')

echo "Admin command: $ADMIN_CMD"
echo "User command: $USER_CMD"

# Test 1: Commands are single-line (no line continuations)
if echo "$ADMIN_CMD" | grep -q '\\$'; then
    echo "❌ FAIL: Admin command contains line continuations (backslash-newline)"
    exit 1
fi
echo "✓ PASS: Admin command is single-line"

if echo "$USER_CMD" | grep -q '\\$'; then
    echo "❌ FAIL: User command contains line continuations (backslash-newline)"
    exit 1
fi
echo "✓ PASS: User command is single-line"

# Test 2: Commands contain correct baseUrl
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

# Test 3: No quotes around env-var values (quotes cause the value to include literal quotes)
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
EOF
chmod +x /var/home/viper/IdeaProjects/fix-postman-cli-env-var/scripts/tests/verify-postman-workflow.sh