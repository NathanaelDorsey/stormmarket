# BUG-2025-11-14T152500: Postman CLI --env-var not applied in GitHub Action due to multi-line command handling

## Problem

In the GitHub Actions workflow `.github/workflows/docker-build-deploy.yml`, the `test-docker-image` job runs Postman collection tests using `postmanlabs/postman-cli-action@v1`. The workflow passes `--env-var "baseUrl=http://localhost:8081"` to override the collection's default `baseUrl` (which is `http://localhost:8080`).

**Actual behavior:** All requests are made to `http://localhost:8080` (the collection default), resulting in `connect ECONNREFUSED 127.0.0.1:8080` errors because the application container exposes port 8080 internally but maps to host port 8081.

**Expected behavior:** Requests should be made to `http://localhost:8081` (the overridden value), allowing tests to reach the running application.

## Root Cause Analysis

The GitHub Action `postmanlabs/postman-cli-action@v1` (v1.0.0) executes the Postman CLI command using:

```bash
postman $POSTMAN_COMMAND
```

Where `POSTMAN_COMMAND` is the multi-line string from the workflow's `with.command` input:

```yaml
command: |
  collection run ./postman/collections/Storm_Market_API.json \
    --env-var "baseUrl=http://localhost:8081" \
    --env-var "authToken=${ADMIN_TOKEN}" \
    --bail=false \
    --output docker-admin-results.json
```

When bash expands `$POSTMAN_COMMAND` without `eval`, literal newlines in the variable are treated as **command separators**, not as line continuations. This causes the command to be parsed as:

1. `postman collection run ./postman/collections/Storm_Market_API.json \` (incomplete, fails silently)
2. `--env-var "baseUrl=http://localhost:8081" \` (treated as separate command, fails)
3. `--env-var "authToken=${ADMIN_TOKEN}" \` (treated as separate command, fails)
4. `--bail=false \` (treated as separate command, fails)
5. `--output docker-admin-results.json` (treated as separate command, fails)

The collection run executes without the `--env-var` arguments, so the collection's default `baseUrl` (`http://localhost:8080`) is used.

The `develop` branch of the action uses `eval "postman $POSTMAN_COMMAND"` which correctly handles line continuations, but the released `v1` tag does not include this fix.

**Risk level:** High — CI/CD pipeline fails to test the deployed application, giving false confidence.

## TDD Fix Plan

1. **RED**: Write a test that verifies the GitHub Action correctly applies `--env-var` arguments when the command contains line continuations
   **GREEN**: Update the workflow to use a single-line command or an environment file instead of multi-line `--env-var`
   **verify**: Run the workflow and confirm requests go to `http://localhost:8081`

2. **RED**: Write a test that verifies the Postman collection runs against the correct host port (8081) in the Docker test job
   **GREEN**: Pin the Postman CLI action to a version that includes the `eval` fix, or generate an environment file at runtime
   **verify**: Run the workflow and confirm all 22 requests execute against port 8081

**REFACTOR**: Consider migrating to the `develop` branch of the action or using a custom composite action that properly handles multi-line commands.

## Acceptance Criteria

- [ ] Postman collection requests target `http://localhost:8081` in the Docker test job
- [ ] All 22 requests execute (no connection refused errors)
- [ ] Test scripts no longer throw TypeError/JSONError due to missing response
- [ ] Workflow passes on `main` branch pushes

## Resolution

<!-- filled in by validate-fix -->