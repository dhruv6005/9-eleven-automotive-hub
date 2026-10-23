# Local runbook

## Start

1. Install Docker Desktop / Docker Engine with Compose v2.
2. Copy `.env.example` to `.env`; keep these values local only.
3. Run `docker compose up --build` from the repository root.
4. Visit the site at `http://localhost:4200` and the API status at `http://localhost:8080/api/v1/system/status`.

## Stop and reset

- Stop containers: `docker compose down`.
- Stop and delete the local database volume: `docker compose down -v`. This deletes local data.
- View service logs: `docker compose logs -f api` or `docker compose logs -f postgres`.
- Start MailHog with `docker compose --profile mail up --build` when email delivery is implemented.

## Database migrations

Flyway runs on API startup. Add future migrations under `backend/src/main/resources/db/migration` using the next sequential prefix (`V2__...sql`). Do not edit a migration that has already been applied to a shared environment. Hibernate must remain in `validate` mode.

## Phase 1 smoke checks

- `GET http://localhost:8080/actuator/health` returns health status.
- `GET http://localhost:8080/api/v1/system/status` returns the application name and phase.
- `GET http://localhost:8080/api-docs` returns the OpenAPI schema.
- `http://localhost:4200` serves the Angular public-site shell.

## Known phase boundary

The `admin` and `portal` routes are static unauthenticated previews. Do not enter real customer data or use this foundation as a production workshop system before authentication, ownership checks, production secrets and operational workflows are implemented and tested.
