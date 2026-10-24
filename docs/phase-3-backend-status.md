# Phase 3 backend delivery status

## Implemented

- Flyway V3 adds category and service slugs, category sort order, image URLs, service short benefits, and audited soft-delete metadata for service prices.
- V3 seeds eight active categories and three services in each. No service price data is seeded.
- Public anonymous endpoints list active categories, browse active services with optional category and vehicle-size filters and pagination, retrieve services by slug, and list only active offers within their inclusive validity dates. Public reads set a 60-second public cache header.
- Admin endpoints provide category, service, service-price, and offer CRUD. Hiding a category, service, or offer sets `active=false`; price rows can be removed. Offers persist service links through `offer_services`.
- DTO validation and MapStruct mappings keep persistence entities out of responses. Errors use the existing ProblemDetail handler. Admin routes are protected at the URL and method levels; anonymous access returns 401 and CUSTOMER access returns 403.
- Prices use `BigDecimal` and INR semantics; none are fabricated. A public service has `price: null` when the requested size class has no configured price. When no size class is supplied, one configured price can be returned; multiple size prices produce `null` to avoid choosing an arbitrary vehicle class.
- Offer validation enforces date order, percent discounts from 1 through 100, non-negative flat discounts, and flat discounts no greater than known applicable service prices. The pricing helper returns a discounted price for a service and vehicle size class and returns null when no price exists.
- Public service list and detail responses include prices ordered SMALL, MEDIUM, LARGE, SUV and the lowest configured `fromPrice` (or null). The legacy `price` field is populated only when a valid `sizeClass` is explicitly requested.
- `VehicleSizeClass` is the single supported enum used to validate service-price API inputs. `GET /api/v1/public/vehicle-size-classes` returns its display labels. Unknown values return 400. Flyway V4 conditionally adds CHECK constraints to service prices and vehicles only when the existing respective data is valid.
- Public offers include server-calculated discounted prices for each eligible service and each configured size class through the existing `OfferPricing` helper.
- `docs/api.md` and `docs/er-diagram.md` describe the current Phase 3 API and schema.

## Verification

Ran `mvnw verify` from `backend/` with Java 21 and Docker Desktop available:

- Build: passed.
- Tests: 19 passed, 0 failures, 0 errors, 0 skipped.
- Testcontainers started PostgreSQL 16. Flyway validated and applied V1, V2, and V3 to an empty database; Hibernate schema validation completed.
- Catalog integration coverage includes inactive services and offers, expired offers, current offers, admin category/service/price creation, offer create/update/soft-hide, empty/multiple-size price behavior, unsupported size rejection, validation errors, and anonymous/CUSTOMER authorization.
- Offer pricing unit tests cover percentage calculation/rounding and flat-discount bounds.

## Remaining scope

This document captures the Phase 3 backend delivery snapshot. Follow-up verification for the combined size-aware API and frontend is recorded in [phase-3-followup.md](phase-3-followup.md). No broader job, customer, payment, or enquiry workflows were added.
