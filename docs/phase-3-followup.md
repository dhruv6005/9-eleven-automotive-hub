# Phase 3 pricing follow-up verification

## Changes covered

- Public service list and detail payloads carry a size-ordered `prices` list and minimum configured `fromPrice`. The compatibility `price` field is populated only for an explicit supported `sizeClass` request.
- The backend uses `VehicleSizeClass` for supported service-price input and exposes the labels through `GET /api/v1/public/vehicle-size-classes`. V4 conditionally adds database CHECK constraints when existing values are valid.
- Public offers calculate discounted prices server-side for each applicable service and configured size using `OfferPricing`. The services, detail, offers, and admin price-grid views consume the expanded API.
- Integration coverage exercises multiple size prices, no configured prices, class listing, unknown class rejection, and discounted offer prices.

## Verification performed on 2026-10-24

- Backend `mvn verify`: **not completed**. Java 21 and Maven were available from the local development cache. Main source compilation succeeded, and test sources compiled. The test run started `ApiStatusControllerTest` but stalled before continuing to Testcontainers-backed integration tests because Docker Desktop/the Docker daemon was not running; the command was stopped after confirming no Docker Desktop/backend process was present. No backend test result is claimed.
- Backend `mvn test-compile -DskipTests -q`: passed after the final backend/test edits.
- Frontend production build: **blocked by network access**. The Angular CLI build attempted Google Fonts inlining from `fonts.googleapis.com`, which failed DNS resolution (`ENOTFOUND`). `npm` itself was unavailable on PATH, so the equivalent local Angular CLI command was used.
- Frontend unit tests: **passed**, 3 test files and 6 tests. Executed through the local Angular CLI with the temporary directory set inside the workspace to avoid Windows temp rename permissions.
- Frontend development build: **not successful**; Angular CLI exited with code 1 after printing only the initial build banner, without actionable diagnostics. This is recorded separately from the production build's explicit Google Fonts network failure.
- `git diff --check`: passed; Git emitted only line-ending conversion warnings.

## Remaining verification

Run `mvn verify` with Docker Desktop available to execute PostgreSQL 16 migrations and catalog integration tests. Run `npm run build` on a network-enabled machine (or with font inlining available) to verify the production build. The unit tests passed locally, but no browser walkthrough or live API smoke test was performed.
