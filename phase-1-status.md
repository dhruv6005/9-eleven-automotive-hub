# Phase 1 delivery status

## Implemented in source

- Monorepo layout and local-development documentation.
- Java 21 / Spring Boot 3.5.16 API skeleton.
- API status endpoint at `/api/v1/system/status`.
- JPA audit base class, user/customer/vehicle/catalog/job foundation entities, role and job-status enums.
- Flyway V1 schema for users, customer profiles, vehicles, catalog and pricing, offers, enquiries, jobs, stage weights, timeline/media, approvals, invoices/payments, notifications, token stores, gallery, testimonials and audit log.
- Required indexes and sequential job-number counter.
- Problem Details error handling, field validation error structure, request correlation IDs, Actuator and OpenAPI.
- Angular 21 standalone app shell with the yellow/charcoal/white brand, responsive public landing page and explicitly non-authenticated admin/customer preview routes.
- Docker Compose for PostgreSQL/API/web, optional MailHog profile, Nginx API proxy, environment example and CI workflow.

## Offline checks completed

- JavaScript syntax parse for frontend TypeScript source files (syntax only, not Angular type checking).
- CSS parser check.
- JSON/XML/YAML configuration parsing.
- Shell syntax check for the Maven bootstrap script.
- SQL static reference scan: all referenced table names are declared in the V1 migration.
- Source trailing-whitespace check.

## Checks still required on a network-enabled machine

This execution environment does not have Docker or Maven installed and cannot resolve the npm/Maven package registries. Consequently, Maven compilation/JUnit execution, npm dependency installation, Angular production build, Flyway execution against PostgreSQL and Docker Compose startup have not been verified here. The CI workflow is supplied to run the dependency-backed checks after the source is pushed to a network-enabled runner.

## Deliberately not implemented yet

Registration/login/JWT, refresh-token rotation logic, role guards, CRUD endpoints, job transitions, customer ownership checks, media upload, email notifications, approval actions, invoice PDF generation, live updates, dashboard reporting and production deployment. These remain for the subsequent phases.
