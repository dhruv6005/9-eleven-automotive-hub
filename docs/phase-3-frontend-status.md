# Phase 3 frontend delivery status

## Implemented

- The landing page loads active service categories from the public catalog API and links each category into `/services`.
- Public catalog routes include `/services` with category filters, `/services/:slug` detail, and `/offers`. Catalog views show loading, empty, and retryable error states. Missing service prices display “Price on request.”
- Offers show their inclusive validity dates, discount terms, and applicable service names. A discounted INR price is calculated only when an eligible service has an unambiguous price from the public API (the API returns a price only when one price row exists without a vehicle size filter). Services without that price retain the discount terms without a fabricated final price.
- The authenticated `/admin` shell now provides category, service, service-price, and offer screens. It supports create/edit, active state changes, soft deactivation, and a per-vehicle-size price grid. Forms use reactive controls, field validation, save feedback, and catalog loading/error/empty states.
- Catalog request/response models are typed and centralized in the catalog feature. API calls match the Phase 3 endpoints in [api.md](api.md); no backend changes were required.
- Added Vitest coverage for catalog API filtering and price updates, plus role-guard denial. Existing auth interceptor and authenticated-guard tests remain in the suite.

## Verification

- `npm run build` passed using the Node 22 container. Initial bundle: 321.79 kB raw; catalog routes are lazy loaded.
- `npm test` passed: 3 test files and 5 tests.

## Limits

- Public offers do not include a computed discounted total or a vehicle size selector. The frontend can show final discounted prices only for eligible services whose public response includes one unambiguous price; customers should enquire for prices that vary by vehicle size.
- The admin size grid currently presents the established `SMALL`, `MEDIUM`, `LARGE`, and `SUV` size labels. The backend accepts size-class strings, so any additional workshop-specific size class requires a frontend option update.
- No browser-based responsive/accessibility walkthrough or live API session was performed; checks were the production Angular build and Vitest suite.
