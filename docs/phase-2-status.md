# Phase 2 delivery status

## Implemented in source

- Spring Security 6 stateless JWT authentication with 15-minute access tokens, `@EnableMethodSecurity`, role authorization, and CORS restricted to the configured frontend origin.
- Customer registration, BCrypt password hashing, email verification, login, 15-minute lockout after five failures, refresh rotation, logout, forgot/reset password, and forced first-admin password change.
- Refresh tokens are random 256-bit opaque tokens; only SHA-256 hashes are stored. Each token belongs to a family. Reuse of a revoked token revokes active tokens in that family.
- Secure, HttpOnly, SameSite=Strict refresh cookie scoped to `/api/v1/auth`; access tokens stay in Angular memory.
- Bucket4j process-local rate limiting on auth routes, plus audit entries for successful login, failed login, and password reset.
- Customer profile `GET` and `PUT` at `/api/v1/me` with authenticated-user resolution and CUSTOMER role enforcement.
- First-admin startup seeder uses `ADMIN_EMAIL` and `ADMIN_INITIAL_PASSWORD`; it skips when both are blank, rejects partial configuration, refuses to promote an existing customer, and marks a new admin for password change.
- SMTP-based verification and reset emails, MailHog Compose profile, Angular auth pages, single-refresh HTTP interceptor, role guards, and authenticated admin/customer shells.
- Flyway V2 adds lockout/forced-change columns, refresh-token family IDs, and verification token storage.
- Testcontainers integration tests cover registration, email verification, login, refresh rotation and reuse, logout, password reset, lockout, profile access, role denial, admin first-login change, and weak-password rejection.

## Verification

Checks completed against the final Phase 2 source:

- `mvn -B verify` from the backend with a host Docker socket available: passed. All 11 tests passed, including all 8 Testcontainers integration tests; Testcontainers started PostgreSQL 16, applied Flyway V1 and V2 to an empty schema, and Hibernate schema validation completed.
- Frontend production build: passed in the Docker build using `npm run build`; Angular generated the application bundle (initial bundle 311.85 kB).
- `docker compose --profile mail up --build -d`: passed. PostgreSQL reported healthy, the API and web containers started, and the web container health check passed. A request from the web container to `/api/v1/system/status` returned `{"status":"UP"}`.
- Compose's isolated API image build also completed `mvn verify`; its 8 Testcontainers tests were skipped because the image builder has no Docker socket. The separately run host-mounted suite above executed and passed those tests.
- `git diff --check`: passed (only Git line-ending conversion warnings were emitted).

## Not verified in this environment

- Verification/reset email was mocked in the integration suite. Compose starts MailHog, but end-to-end email capture and clicking a real delivered link were not exercised.
- The first-admin seeder's environment-driven startup path was not launched against the Compose database with real admin variables; the auth integration test exercises forced password change and admin authorization with a seeded test record.
- Browser-based manual checks of responsive layout, redirect behavior, and refresh behavior were not performed. No frontend test runner is configured; the requested production build was used.
- Production SMTP, HTTPS, and multi-instance rate-limit storage were not tested.
- Compose reused the existing local Postgres volume, which had been migrated from V1 to V2. The Testcontainers run independently verified both migrations from an empty schema.

## Known limits and follow-up

- Email is plain text over SMTP in this phase; configure real SMTP and HTTPS before public use.
- Bucket4j currently stores buckets in process memory. In Compose, client IP forwarding is accepted only from the explicitly configured Nginx container address; a multi-instance deployment needs shared storage and its own trusted proxy configuration.
- Admin/customer dashboards are authenticated shells only. Catalog, vehicle, job, and workshop workflows remain in later phases.
- Angular's Vitest runner and focused auth guard/interceptor tests are added in the Phase 2 follow-up; see `docs/phase-2-followup.md` for verification.
