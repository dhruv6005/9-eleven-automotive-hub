# Phase 3 frontend delivery status

## Implemented

- The landing page loads active service categories from the public catalog API and links each category into `/services`.
- Public catalog routes include `/services` with category filters, `/services/:slug` detail, and `/offers`. Catalog views show loading, empty, and retryable error states. The service list shows “From ₹X” using `fromPrice`, or “Price on request” when unset. Detail includes a supported size selector and per-size table.
- Offers show validity dates, discount terms, and server-computed discounted service prices for the selected vehicle size. The selector uses labels from the public size-class endpoint.
- The authenticated `/admin` shell now provides category, service, service-price, and offer screens. It supports create/edit, active state changes, soft deactivation, and a per-vehicle-size price grid. Forms use reactive controls, field validation, save feedback, and catalog loading/error/empty states.
- Catalog request/response models are typed and centralized in the catalog feature. Admin price grid options load from the public size-class endpoint rather than a hardcoded list.
- Added Vitest coverage for catalog API filtering and price updates, plus role-guard denial. Existing auth interceptor and authenticated-guard tests remain in the suite.

## Verification

- `npm run build` passed using the Node 22 container. Initial bundle: 321.79 kB raw; catalog routes are lazy loaded.
- `npm test` passed: 3 test files and 5 tests.

## Limits

- No browser-based responsive/accessibility walkthrough or live API session was performed; checks were the production Angular build and Vitest suite. See the combined follow-up for current verification results.
