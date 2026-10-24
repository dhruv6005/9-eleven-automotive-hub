# Phase 4 frontend delivery status

## Implemented

- Replaced the customer portal placeholder with a dashboard shell, vehicle count summary, and “My Vehicles” list. Customers can add, edit, and soft-delete their own vehicles with a confirmation dialog.
- Vehicle forms normalize registration numbers to uppercase and remove spaces/hyphens as the user types. Size class options come from the shared public vehicle-size endpoint and each class has a short contextual hint.
- Added an admin Customers workspace with paginated customer search/status filtering, a phone-first create/edit form, customer detail, vehicle list and add/edit/delete, and portal invitation action.
- Added typed customer, vehicle, size-option, and page models plus a dedicated API service for the Phase 4 routes.
- Added service coverage for customer search/status query serialization, vehicle creation, and the shared size-class endpoint. Existing authenticated and role guards continue to protect `/portal` and `/admin`.
- Added responsive layouts, loading/error/empty states, field feedback, keyboard focus styling, and accessible confirmation/dialog semantics.

## Verification

- `npm run build` and `npm test` could not run because `npm` is absent from this shell. The production build was attempted via the installed Angular CLI but failed before compilation because this checkout has `@esbuild/linux-x64` installed while the host requires `@esbuild/win32-x64`.
- The test suite was attempted through the local Vitest entry point but could not start because optional dependency `@rollup/rollup-win32-x64-msvc` is missing. The test result is therefore unavailable, not passing.
- `tsc --noEmit -p tsconfig.app.json` passed. Angular template/type compilation via `ngc -p tsconfig.app.json` passed after correcting the detected template issues.

## Limits

- No live API/browser session or responsive accessibility walkthrough was performed. API behavior follows the Phase 4 contract in `docs/api.md` and `docs/phase-4-backend-status.md`.
- Customer status options reflect the API's user account statuses; the list remains searchable across name, phone, email, and registration as supported by the backend.
