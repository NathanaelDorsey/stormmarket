# Security Plan

## Scope
Storm Market API security surface including authentication, authorization, and data protection.

## Threat Model
- **JWT Interception:** Stateless RS256 tokens transmitted via Authorization: Bearer header. Risk: token theft if not transmitted over HTTPS.
- **Password Exposure:** Argon2 hashing prevents reversible password recovery. Risk: only if database is compromised.
- **CSRF:** Disabled per design for API-only usage. Risk: low (API clients typically don't share cookies).
- **SQL Injection:** Uses Spring Data JPA with parameterized queries via `@Param` and `Specifications`. Risk: minimal with proper use.
- **XSS:** Not applicable (backend-only, no rendered HTML served to browsers).

## Security Controls
- **Authentication:** Stateless JWT with RS256, role claim mapping to `ROLE_*` authorities
- **Password Encoding:** Argon2 password encoder, configured in `SecurityConfig`
- **Authorization:** Spring Security `SecurityConfig` with ` permitAll()` for catalog reads, ADMIN for mutating routes
- **CSRF:** Disabled (`sessionCreationPolicy.STATELESS` + `csrf().disable()`)
- **Headers:** Recommended: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Strict-Transport-Security`
- **Data Validation:** `@Valid` on all request DTOs, bean validation constraints (`@NotNull`, `@Size`, `@Pattern`)
- **Error Handling:** `GlobalExceptionHandler` returns generic messages to clients, detailed logs server-side

## Compliance
- OWASP Top 10: A02 (Broken Authentication) mitigated via JWT + Argon2; A03 (Injection) mitigated via JPA parameterization; A07 (Identification/Authentication Failures) mitigated via stateless design
- No password logging (Argon2 hashes never logged)
- No hardcoded credentials (datasource URL/password from environment/.env)