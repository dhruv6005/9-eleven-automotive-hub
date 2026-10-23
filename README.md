# 9-Eleven Automotive Hub

Phase 1 foundation for a premium automotive detailing and workshop platform. This is a monorepo with a Java 21 / Spring Boot API, Angular frontend, PostgreSQL 16, Flyway migrations, Docker Compose, and CI.

## Quick start

Prerequisites: Docker Engine / Docker Desktop with Compose v2.

```bash
cp .env.example .env
docker compose up --build
```

Open the public site at <http://localhost:4200>, API status at <http://localhost:8080/api/v1/system/status>, Swagger UI at <http://localhost:8080/swagger-ui.html>, and Actuator health at <http://localhost:8080/actuator/health>.

To stop the stack, run `docker compose down`. To remove the local database volume too, use `docker compose down -v` (this permanently deletes local database data).

## Local development without Docker

- Java 21 is required for the API. PostgreSQL 16 must be running and reachable at the URL in `backend/src/main/resources/application-dev.yml`.
- Run the API from `backend` using `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). The Maven wrapper bootstraps Maven if needed.
- Run the Angular application from `frontend` using `npm install` then `npm start`.

## Phase 1 scope

Included: monorepo skeleton, Spring Boot API entry point, versioned status endpoint, shared audit entity, foundational user/customer/vehicle/catalog/job entities, baseline role and job status enums, PostgreSQL schema with Flyway, optimistic-lock version columns, correlation IDs, Problem Details error responses, Swagger/OpenAPI, Angular public-site shell, placeholder admin/customer portal routes, Docker Compose, and GitHub Actions CI.

Not yet implemented: registration and authentication, JWT/session refresh, role guards, service catalogue screens, job lifecycle commands, customer tracking, notifications, real upload storage, billing workflows, live events, and a production deployment. Those are intentionally reserved for later phases.

## Environment configuration

`.env.example` contains local-only defaults to make the first Compose run easy. Change credentials and supply proper secret management before deploying anywhere public. The `prod` Spring profile requires database environment variables and keeps Hibernate DDL generation in `validate` mode; Flyway owns schema changes.

## API basics

- `GET /api/v1/system/status` — confirms the API is running.
- `GET /actuator/health` — application health probe.
- `GET /api-docs` — OpenAPI document.
- `GET /swagger-ui.html` — Swagger UI.

Every API response includes an `X-Correlation-Id` response header. Send a safe `X-Correlation-Id` request header to propagate a caller-provided value, or the API will generate one.

## Project documents

See `docs/architecture.md`, `docs/er-diagram.md`, and `docs/runbook.md`. The complete target product plan is derived from the supplied *Car Detailing Workshop – Full Project Prompt*.
