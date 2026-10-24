# API reference — Phases 1 to 4

The versioned REST API is rooted at `/api/v1`. The published interactive reference becomes available at `/swagger-ui.html`; the raw OpenAPI document is `/api-docs`.

## Foundation endpoint

### `GET /api/v1/system/status`

Returns a small diagnostic payload confirming that the API process is responding.

Example:

```json
{
  "application": "9-Eleven Automotive Hub API",
  "phase": "phase-1-foundation",
  "status": "UP",
  "timestamp": "2026-10-23T12:00:00Z"
}
```

The timestamp is generated at request time. Every request receives an `X-Correlation-Id` response header. A caller-provided ID is accepted only if it is at most 80 characters and matches `[A-Za-z0-9._-]+`; otherwise the API generates a UUID.

## Error contract

Error responses use Spring `ProblemDetail`, compatible with RFC 7807 fields such as `type`, `title`, `status`, `detail` and `instance`. Bean validation errors include a `fieldErrors` object keyed by field name. Unexpected server errors do not expose exception messages or stack traces.

## Authentication and profile endpoints

| Method | Path | Access | Behavior |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Create a CUSTOMER account and send email verification. The request has no role field. |
| `POST` | `/api/v1/auth/login` | Public | Return a 15-minute JWT and set a 30-day secure refresh cookie. |
| `POST` | `/api/v1/auth/refresh` | Refresh cookie | Rotate the cookie token and issue a new JWT. Reuse of a revoked token revokes the family. |
| `POST` | `/api/v1/auth/logout` | Refresh cookie | Revoke the presented refresh token and clear the cookie. |
| `POST` | `/api/v1/auth/forgot-password` | Public | Send a reset link when an active account exists; response does not reveal account existence. |
| `POST` | `/api/v1/auth/reset-password` | Public | Consume a reset token, replace the password, and revoke active sessions. |
| `GET` | `/api/v1/auth/verify-email?token=...` | Public | Consume the single-use verification token and activate the customer account. |
| `POST` | `/api/v1/auth/change-password` | Authenticated | Change password; required for a first-login seeded admin. Returns a replacement access JWT. |
| `GET` | `/api/v1/me` | CUSTOMER | Read the authenticated customer's profile. |
| `PUT` | `/api/v1/me` | CUSTOMER | Update profile name, phone, and address. Email changes are not supported here. |
| `GET` | `/api/v1/admin/session` | ADMIN | Return the authenticated admin identity/role; also provides a small role-protected session check. |

Login accepts `{ "email": "...", "password": "..." }`. Customer registration accepts `name`, `email`, optional `phone`, and `password`. Passwords must be 12–72 characters, no more than 72 UTF-8 bytes, and contain lowercase, uppercase, numeric, and symbol characters.

## Catalog and offers (Phase 3)

All public catalog reads are anonymous and return `Cache-Control: public, max-age=60`. Public list responses are paginated where appropriate. Service list and detail DTOs include `prices`, an array of `{ "sizeClass", "price" }` ordered SMALL, MEDIUM, LARGE, SUV, and `fromPrice`, the lowest configured price or `null`. The legacy `price` property remains `null` unless the caller explicitly supplies `sizeClass`, in which case it is that class's price or `null`. No prices are seeded. Unknown size classes return 400. Public offer DTOs include `prices`, an array of `{ "serviceId", "serviceName", "sizeClass", "price" }` with discounted amounts computed on the server for applicable services and available price rows.

| Method | Path | Access | Behavior |
|---|---|---|---|
| `GET` | `/api/v1/public/categories` | Public | Active categories, ordered by sort order. |
| `GET` | `/api/v1/public/vehicle-size-classes` | Public | Supported classes and labels: SMALL, MEDIUM, LARGE, SUV. |
| `GET` | `/api/v1/public/services?category={slug}&sizeClass={class}&page=0&size=20` | Public | Active services, optionally filtered by category slug; page size is capped at 100. |
| `GET` | `/api/v1/public/services/{slug}?sizeClass={class}` | Public | One active service or 404. |
| `GET` | `/api/v1/public/offers` | Public | Active offers whose validity includes the current date. |
| `GET`, `POST` | `/api/v1/admin/categories` | ADMIN | List/create categories. |
| `PUT`, `DELETE` | `/api/v1/admin/categories/{id}` | ADMIN | Update or soft-hide a category (`active=false`). |
| `GET`, `POST` | `/api/v1/admin/services` | ADMIN | List/create services. |
| `PUT`, `DELETE` | `/api/v1/admin/services/{id}` | ADMIN | Update or soft-hide a service (`active=false`). |
| `GET` | `/api/v1/admin/services/{id}/prices` | ADMIN | List prices by vehicle size class. |
| `PUT`, `DELETE` | `/api/v1/admin/services/{id}/prices/{sizeClass}` | ADMIN | Set/update or remove a service price for one size class. Prices are non-negative INR decimals. |
| `GET`, `POST` | `/api/v1/admin/offers` | ADMIN | List/create offers and their linked service IDs. |
| `PUT`, `DELETE` | `/api/v1/admin/offers/{id}` | ADMIN | Update or soft-hide an offer (`active=false`). |

Offer types are `PERCENT` (1–100) and `FLAT`. Validity dates are inclusive. A flat discount cannot exceed the applicable service price; this is enforced by the offer pricing helper when calculating a price. Offer application is restricted to its linked services unless `appliesToAll` is true. Flyway V4 applies CHECK constraints to `service_prices.vehicle_size_class` and `vehicles.size_class` only when all existing non-null values in the respective column are supported; otherwise that column's constraint is skipped. All admin routes require an ADMIN access token; anonymous callers receive 401 and CUSTOMER callers receive 403.

The refresh cookie is named `refresh_token`, has `HttpOnly`, `Secure`, and `SameSite=Strict` attributes, and is scoped to `/api/v1/auth`. Send the cookie with refresh/logout calls. Access tokens are returned in the JSON response and sent as `Authorization: Bearer ...`. Do not store access tokens in local storage.

Account lockout begins after five failed login attempts and lasts 15 minutes. Auth routes share a process-local limit of 30 requests per minute per client IP. In Compose, the API honors a single literal `X-Forwarded-For` address only when the request's socket peer is the configured Nginx proxy; Nginx replaces caller-supplied forwarding headers. Direct clients cannot choose their rate-limit key by supplying that header. This limiter is intended for the current single-instance development setup.

## Customers and vehicles (Phase 4)

Admin customer create/update accepts `{ "name", "phone", "email"?, "address"?, "notes"? }`. Email changes use the invite endpoint. Phone values are normalized and duplicate customer phones return 409. Customers created without email use `NO_LOGIN`; invitation sends a single-use, 30-minute set-password link and password completion activates the account.

| Method | Path | Access | Behavior |
|---|---|---|---|
| `POST` | `/api/v1/admin/customers` | ADMIN | Create a CUSTOMER profile. |
| `GET` | `/api/v1/admin/customers?search=&status=&page=0&size=20` | ADMIN | Paginated search by name, phone, email, or vehicle registration and optional status. |
| `GET`, `PUT` | `/api/v1/admin/customers/{id}` | ADMIN | Read profile with vehicles / update profile and phone. |
| `POST` | `/api/v1/admin/customers/{id}/invite` | ADMIN | Add/confirm email and send a set-password link. |
| `GET`, `POST` | `/api/v1/me/vehicles` | CUSTOMER | List or create the authenticated customer's vehicles. |
| `GET`, `PUT`, `DELETE` | `/api/v1/me/vehicles/{id}` | CUSTOMER | Read/update/soft-delete an owned vehicle. Wrong-owner IDs return 404. |
| `GET`, `POST` | `/api/v1/admin/customers/{customerId}/vehicles` | ADMIN | List or create vehicles for a customer. |
| `GET`, `PUT`, `DELETE` | `/api/v1/admin/customers/{customerId}/vehicles/{id}` | ADMIN | Read/update/soft-delete a vehicle scoped to that customer. |
| `GET` | `/api/v1/admin/vehicles?search=` | ADMIN | Paginated registration-number lookup. |

Vehicle requests use `make`, `model`, optional `variant`, `year`, `color`, `vin`, required enum `sizeClass`, and `regNumber`. Registrations are uppercased and spaces/hyphens removed; duplicate active registrations return 409. Deletion sets `deleted_at`, preserving job references. Admin writes are recorded in `audit_log`. Anonymous requests receive 401; non-admin admin-route requests receive 403; ADMIN requests to `/me/**` receive 403 under the existing CUSTOMER-only rule.

Enquiries, jobs, approvals, invoices, media, and notifications remain planned for later phases.
