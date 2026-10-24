# Phase 4 backend delivery status

## Implemented

- Flyway V5 makes user email nullable while retaining case-insensitive uniqueness for present emails. `NO_LOGIN` is a supported customer account status; login rejects it through the same unauthorized credential response as other rejected logins.
- Admin customer create, paginated list/search/status filter, detail with active vehicles, update, and invite endpoints. Customer records are always created with role `CUSTOMER`; phone values are normalized and duplicate customer numbers return 409.
- Admin invitation adds/confirms an email, issues the existing hashed, single-use, 30-minute reset token as a set-password link, and reset completion changes `NO_LOGIN` to `ACTIVE`.
- Customer and admin vehicle CRUD, registration normalization and conflict response, paginated admin registration lookup, customer ownership filtering, and soft deletion that preserves job references.
- Admin customer and vehicle writes insert audit log entries. API payloads use DTOs and MapStruct; repositories fetch customer/user associations needed for response mapping.

## Verification

- `.\mvnw.cmd -B verify` was attempted from `backend/` but could not start: this environment has no valid `JAVA_HOME` configured. No test or compilation result is claimed.
- Testcontainers + MockMvc Phase 4 integration coverage was added for ownership, authentication/roles, customer search and duplicate phone handling, registration normalization/conflicts, soft deletion, and the NO_LOGIN/invite activation flow; it could not be executed without Java. Docker is installed, but its engine pipe is inaccessible in this environment.

## Notes

- This repository already has Flyway V4 for Phase 3 size-class constraints, so the requested Phase 4 migration is V5 to preserve applied migration history.
- `/api/v1/me/**` is already restricted to `CUSTOMER` in the security filter chain. ADMIN access is 403; anonymous access is 401. Ownership-specific vehicle reads/updates/deletes query by both customer and vehicle ID and return 404 on mismatch.
- Vehicle DELETE always soft-hides the active record (including when historical jobs reference it) so the foreign-key relationship and job history remain valid.
