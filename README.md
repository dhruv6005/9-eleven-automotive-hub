# 9-Eleven Automotive Hub

Phase 1 foundation and Phase 2 authentication for a premium automotive detailing and workshop platform. The monorepo contains a Java 21 / Spring Boot API, Angular 21 frontend, PostgreSQL 16, Flyway migrations, Docker Compose, and GitHub Actions CI.

## Quick start

Prerequisites: Docker Engine or Docker Desktop with Compose v2. Copy `.env.example` to `.env` (or add the new Phase 2 variables below if you already have a Phase 1 `.env`), then set a random `APP_JWT_SECRET` of at least 32 bytes. Set both `ADMIN_EMAIL` and `ADMIN_INITIAL_PASSWORD` if you want the first admin seeded; the password must satisfy the app policy. Leave both blank to skip seeding.

```dotenv
APP_JWT_SECRET=replace-with-a-random-value-of-at-least-32-bytes
FRONTEND_ORIGIN=http://localhost:4200
FRONTEND_URL=http://localhost:4200
ADMIN_EMAIL=
ADMIN_INITIAL_PASSWORD=
MAIL_HOST=mailhog
MAIL_PORT=1025
```

```bash
cp .env.example .env
docker compose --profile mail up --build
```

Open the public site at <http://localhost:4200>, API status at <http://localhost:8080/api/v1/system/status>, Swagger UI at <http://localhost:8080/swagger-ui.html>, Actuator health at <http://localhost:8080/actuator/health>, and MailHog at <http://localhost:8025>. The MailHog profile captures verification and password reset email in development. The API and database use local-only credentials from `.env`; replace them and use managed secrets before deployment.

Stop the stack with `docker compose down`. Do not use `docker compose down -v` unless you intend to permanently delete the local database volume.

## Local development without Docker

- Java 21 and PostgreSQL 16 are required for the API. Configure the database and `APP_JWT_SECRET` in the process environment; the secret must contain at least 32 bytes.
- Run the API from `backend` using `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). The Maven wrapper bootstraps Maven if needed.
- Run the Angular application from `frontend` using `npm install` then `npm start`; the development server proxies `/api` to the local API.
- For verification, run `./mvnw verify` from `backend`, then `npm run build` and `npm test` from `frontend`. Auth integration tests use Testcontainers and need Docker.

## Phase 1 and Phase 2 scope

Included: the Phase 1 monorepo, API, foundational schema, status endpoint, errors, OpenAPI, public site, and Compose/CI setup; plus Phase 2 customer registration/email verification, login/logout, JWT access and rotating refresh tokens, password recovery and forced initial-admin password change, lockout/rate limits, customer profile GET/PUT, role-protected admin/customer shells, and an environment-configured first-admin seed.

Not yet implemented: catalog workflows, customer vehicle management, job lifecycle commands and tracking, operational notifications, upload storage, billing workflows, live events, dashboards, and production deployment. Those are reserved for their later phases.

## Environment configuration

`.env.example` contains local-only values. Change credentials and supply proper secret management before deploying anywhere public. `APP_JWT_SECRET` is required and must be at least 32 bytes. The `prod` Spring profile requires database environment variables and keeps Hibernate DDL generation in `validate` mode; Flyway owns schema changes. Configure both admin seed variables together or leave both blank. `docker compose --profile mail up --build` runs the development MailHog SMTP capture service.

## API basics

- `GET /api/v1/system/status` — confirms the API is running.
- `POST /api/v1/auth/**` — registration, login, refresh, logout, password recovery and change.
- `GET /api/v1/auth/verify-email?token=...` — verify an email link.
- `GET|PUT /api/v1/me` — authenticated customer profile.
- `GET /actuator/health` — application health probe.
- `GET /api-docs` — OpenAPI document.
- `GET /swagger-ui.html` — Swagger UI.

Every API response includes an `X-Correlation-Id` response header. Send a safe `X-Correlation-Id` request header to propagate a caller-provided value, or the API will generate one.

## Project documents

See `docs/architecture.md`, `docs/er-diagram.md`, `docs/runbook.md`, and `docs/phase-2-status.md`. The complete target product plan is in `Car Detailing Workshop – Full Project Prompt.docx`.
