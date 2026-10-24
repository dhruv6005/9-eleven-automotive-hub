# Local runbook

## Start

1. Install Docker Desktop / Docker Engine with Compose v2.
2. Copy `.env.example` to `.env` or add the Phase 2 variables to an existing `.env`; Phase 3 requires no new environment variables. Replace `APP_JWT_SECRET` with a random value of at least 32 bytes, keep `TRUSTED_PROXY_ADDRESSES` aligned with the web container address on the Compose network, and configure both admin seed values or leave both blank. Keep these values local only.
3. Run `docker compose --profile mail up --build` from the repository root to enable MailHog.
4. Visit the site at `http://localhost:4200` and the API status at `http://localhost:8080/api/v1/system/status`.

## Stop and reset

- Stop containers: `docker compose down`.
- Stop and delete the local database volume: `docker compose down -v`. This deletes local data.
- View service logs: `docker compose logs -f api` or `docker compose logs -f postgres`.
- View development email at `http://localhost:8025`.
- Start the app without MailHog with `docker compose up --build`; auth mail delivery then requires a reachable SMTP service.

## Database migrations

Flyway runs on API startup. Add future migrations under `backend/src/main/resources/db/migration` using the next sequential prefix (`V2__...sql`). Do not edit a migration that has already been applied to a shared environment. Hibernate must remain in `validate` mode.

## Phase 1 smoke checks

- `GET http://localhost:8080/actuator/health` returns health status.
- `GET http://localhost:8080/api/v1/system/status` returns the application name and phase.
- `GET http://localhost:8080/api-docs` returns the OpenAPI schema.
- `http://localhost:4200` serves the Angular public-site shell.

## Phase 2 authentication checks

- Register a customer and open the verification link captured in MailHog.
- Sign in and confirm the response contains a 15-minute access token and a `Secure; HttpOnly; SameSite=Strict` refresh cookie.
- Refresh once and confirm the cookie rotates; attempt to reuse the previous cookie and confirm the family is revoked.
- Confirm five failed logins lock the account temporarily, and that forgot/reset password returns a generic forgotten-password response.
- Confirm `/portal` requires CUSTOMER and `/admin` requires ADMIN. A newly seeded admin must change the initial password before using protected routes.

## Phase 3 catalog smoke checks

- Confirm `GET /api/v1/public/vehicle-size-classes` returns SMALL, MEDIUM, LARGE, and SUV with display labels.
- Confirm public service list and detail responses include `prices` ordered by size and `fromPrice`; when no prices are configured, `prices` is `[]` and `fromPrice` is `null`. `price` should contain a value only when `sizeClass` is explicitly supplied.
- Request a supported size class and confirm its matching price; an unknown class should return 400.
- In Admin → Services → Prices, confirm the price grid comes from the size-class endpoint. Add actual prices and confirm the services page shows “From ₹X” and the detail page shows the size table.
- Create an offer for a priced service and confirm the public offer response has discounted prices by service and size. On the offers page, switch the size and confirm the displayed amount changes.
- Check pre-existing database values before relying on V4 checks: each CHECK is added only when all existing values in that column belong to the supported class set.

## Current phase boundary

The `admin` and `portal` routes now require authenticated roles, but their contents are shells. Do not use the app as a production workshop system before the remaining operational workflows, ownership checks, production secrets, and deployment controls are implemented and tested.
