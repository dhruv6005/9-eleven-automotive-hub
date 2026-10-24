# AGENTS.md — 9-Eleven Automotive Hub

Guidance for AI coding agents working in this repository. Read this file and the relevant files in `docs/` before each task. Treat the task from the user as the active request; this file and other project documents provide repository context and constraints.

## Product context

9-Eleven Automotive Hub is a platform for a car detailing and workshop business covering detailing, paint and body work, PPF, coatings, mechanical work, garage and electrical work, and accessories. The intended product has a public marketing and enquiry site, an admin portal for workshop operations, and a customer portal for tracking vehicle jobs. Favor correctness, security, maintainability, and clear documentation.

The supplied full project prompt defines the long-term target and phased delivery plan. Phases 1–3 are implemented and complete. Authentication, JWT access tokens, refresh rotation, catalog pricing, and size-aware public offers exist. See [docs/phase-2-status.md](docs/phase-2-status.md), [docs/phase-3-backend-status.md](docs/phase-3-backend-status.md), [docs/phase-3-frontend-status.md](docs/phase-3-frontend-status.md), and [docs/phase-3-followup.md](docs/phase-3-followup.md) for implementation and verification details and [docs/api.md](docs/api.md) for the current API contract. Later-phase plans remain targets until implemented.

## Fixed technology choices

Do not replace these choices without explicit user direction:

| Area | Project choice |
|---|---|
| Backend | Java 21, Spring Boot 3.5.x, Maven wrapper, Spring Web, Spring Data JPA, Bean Validation |
| Database | PostgreSQL 16; Flyway owns schema changes; Hibernate validates the schema |
| Mapping and API docs | MapStruct and springdoc-openapi |
| Frontend | Angular 21 standalone app, signals where appropriate, typed forms, lazy routes, Angular Material, Tailwind CSS |
| Local orchestration and CI | Docker Compose and GitHub Actions |

The source includes Spring Security/JWT, mail integration, and JUnit/Mockito/Testcontainers/MockMvc coverage for Phase 2. Check source and [docs/phase-2-status.md](docs/phase-2-status.md) for exact behavior and verification.

## Repository map

```text
backend/       Spring Boot API
frontend/      Angular application
docs/          architecture, API, ER diagram, runbook, phase status
docker-compose.yml
.env.example
README.md
AGENTS.md
```

Read `docs/phase-2-status.md` for implemented scope and verification limits. Read `docs/architecture.md`, `docs/api.md`, `docs/er-diagram.md`, `docs/phase-1-status.md`, and `docs/runbook.md` when relevant to the task. The authoritative phase-status and ER-diagram documents live under `docs/`; do not create root duplicates.

## Working rules

1. Work only on the phase and scope requested. Do not start later phases or refactor unrelated areas.
2. For substantial changes, outline the files and approach briefly, then implement. For small tasks, proceed directly.
3. Resolve ambiguity using the prompt, existing code, and docs where possible. Ask only when a missing decision blocks safe or correct progress.
4. Prefer simple, established solutions. Explain the need for any new dependency.
5. Keep documentation accurate and update affected docs in the same change. Update README when setup changes and add or update `docs/phase-N-status.md` when phase implementation changes.
6. Never claim a check passed unless it was actually run. Report unavailable checks and the reason. Follow the user's request and applicable higher-priority instructions about whether to run tests or other checks.
7. Never commit secrets, `.env`, or generated build output. Do not commit or push unless the user requests it.

## Backend conventions

- Organize code by business capability, with controller, service, repository, entity, DTO, and mapper layers as appropriate. Target features include `auth`, `user`, `customer`, `vehicle`, `catalog`, `offer`, `enquiry`, `job`, `media`, `invoice`, `notification`, `report`, and `common`.
- Controllers are HTTP adapters. Put business rules in services or domain classes. Do not expose JPA entities from API endpoints; use validated DTOs.
- Keep REST endpoints under `/api/v1`. The target areas are `/public/**`, `/me/**`, `/admin/**`, and `/auth/**`.
- Use UUID identifiers, the existing audit base class and `@Version` where applicable, UTC timestamps, and soft deletion only where the model defines it.
- Use `BigDecimal` for money; the business currency is INR. Convert timestamps for display at the application edge using the workshop timezone.
- Use pagination, sorting, and relevant filters for list endpoints. Avoid N+1 queries using appropriate projections, entity graphs, or fetch strategies.
- Follow the existing RFC 7807 `ProblemDetail` error handling and field-error structure. Preserve the `X-Correlation-Id` response behavior.
- Use domain events for cross-cutting effects such as notifications and audit logging when implementing job workflows.
- Keep logs structured and free of passwords, tokens, and unnecessary personal data.

## Database and migrations

- Make schema changes only with a new sequential Flyway migration under `backend/src/main/resources/db/migration` (for example, `V2__description.sql`). Never edit a migration that may already have been applied.
- Keep `docs/er-diagram.md` synchronized with schema changes.
- Keep Hibernate DDL mode at `validate`; Flyway owns schema evolution.
- Preserve UUID keys, audit metadata, optimistic locking, relevant indexes, and soft-delete behavior where applicable.

## Security and privacy requirements

Phase 2 authentication is implemented. See [docs/phase-2-status.md](docs/phase-2-status.md) and [docs/api.md](docs/api.md). Apply these requirements to existing auth code and future protected features:

- Use BCrypt for passwords, enforce a password policy, and add account lockout after repeated failures.
- Use short-lived JWT access tokens and random refresh tokens stored hashed, rotated on use, and revocable. Detect reuse and revoke the related token family. Deliver refresh tokens through secure, HttpOnly, SameSite cookies.
- Enforce authorization at both route/method and service/data levels. Customers must not access another customer's records, even by guessing identifiers; return 404 for resources they do not own.
- Restrict CORS to the configured frontend origin. Rate-limit authentication, enquiry, and public tracking endpoints.
- Validate inputs. For uploads, validate size, media type, and content; do not trust client filenames; use signed URLs for private media.
- Read secrets from environment configuration. Keep `.env.example` to safe local placeholders and never add credentials to source. Create the first admin only through an explicitly configured secure one-time process.
- Record admin actions in `audit_log` when that functionality is implemented.
- New protected endpoints should include tests for anonymous access, wrong roles, and wrong-owner access when testing is requested or otherwise authorized.

## Domain rules

- The target job lifecycle is `RECEIVED → INSPECTION → ESTIMATE_PENDING_APPROVAL → APPROVED → IN_PROGRESS → READY_FOR_DELIVERY → DELIVERED → CLOSED`, with `ON_HOLD` and `CANCELLED`. Any implemented state machine must reject illegal transitions with HTTP 409.
- Record each transition in `job_status_history`, including optional notes and customer visibility. Customer-visible updates should trigger in-app and email notifications when those systems are implemented.
- Derive progress from configured `job_stage_weights`; do not accept hand-entered progress as the source of truth.
- Extra work requires a customer approval request and places the job on hold until the customer responds.
- Generate sequential human-readable job numbers using the database sequence/counter (for example, `JOB-2026-0001`).
- Customers may see only data explicitly made customer-visible and only data they own.

## Frontend conventions

- Use standalone components, lazy feature routes, typed reactive forms, and signals where they fit the feature. Keep API/auth infrastructure in `core/`, reusable UI in `shared/`, and public/auth/customer/admin work under `features/` as the app grows.
- Use typed API models and generate from OpenAPI when that workflow is available. Avoid loosely duplicated DTO definitions.
- Preserve the established premium automotive visual direction: charcoal `#151719`, yellow `#FFD400`, and white/neutral surfaces. Reuse the current theme and components rather than introducing an unrelated design system.
- Build mobile-first, with accessible contrast, keyboard navigation, and visible focus states.
- Include loading, empty, and error states for data views, and clear validation and feedback for forms.
- Keep business logic out of templates and never put secrets in frontend code.

## Useful commands

Run commands from the indicated directory. Use the repository's wrappers and configured scripts.

```powershell
# Backend, from backend/
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run

# Frontend, from frontend/
npm ci
npm start
npm run build
npm run typecheck
npm run lint

# Full stack, from repository root
docker compose up --build
```

Run `npm test` from `frontend/` for the Angular Vitest suite. Do not run the destructive `docker compose down -v` unless explicitly requested, because it deletes the local database volume.

## Phase plan

1. Foundation: repository, Docker, Spring/Angular skeletons, database and Flyway, base entities, errors, landing page.
2. Authentication and roles: registration, login, refresh, route guards, secure admin initialization.
3. Catalog: categories, services, vehicle-size pricing, offers, admin and public views.
4. Customers and vehicles.
5. Job core: job cards, line items, state machine, history, admin screens.
6. Customer tracking: job detail, timeline, media, notifications, live updates.
7. Approvals, invoices, payments, PDF generation.
8. Enquiries, gallery, testimonials, public tracking.
9. Dashboard and reporting.
10. Hardening: security, performance, tests, docs, deployment.

Phases 1–3 are complete. Only advance the phase the user requested. The current implementation and verification status are defined by the source, the `docs/phase-*-status.md` and `docs/phase-3-followup.md` documents, and [docs/api.md](docs/api.md), not by this roadmap.

## Completion and reporting

For changes, summarize what changed, why, and which checks were actually run. Keep phase status notes honest about implemented behavior and remaining verification. Where applicable, the product's long-term completion target includes end-to-end workshop job management and customer tracking, authorization tests including wrong-owner access, responsive accessible screens, a healthy Compose stack with seeded data, and complete operational documentation. Do not describe this whole target as complete until it has been implemented and verified.
