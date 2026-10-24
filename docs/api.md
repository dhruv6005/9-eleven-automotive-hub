# API reference — Phases 1 and 2

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

The refresh cookie is named `refresh_token`, has `HttpOnly`, `Secure`, and `SameSite=Strict` attributes, and is scoped to `/api/v1/auth`. Send the cookie with refresh/logout calls. Access tokens are returned in the JSON response and sent as `Authorization: Bearer ...`. Do not store access tokens in local storage.

Account lockout begins after five failed login attempts and lasts 15 minutes. Auth routes share a process-local limit of 30 requests per minute per client IP. In Compose, the API honors a single literal `X-Forwarded-For` address only when the request's socket peer is the configured Nginx proxy; Nginx replaces caller-supplied forwarding headers. Direct clients cannot choose their rate-limit key by supplying that header. This limiter is intended for the current single-instance development setup.

## Planned API modules

Public catalogue and enquiry, customer vehicle/job routes, admin operations, approvals, invoices, media, and notification endpoints are specified in the full project prompt and will be introduced in their assigned phases.
