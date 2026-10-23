# API reference — Phase 1

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

## Planned API modules

Auth, public catalogue and enquiry, customer `/me` routes, admin routes, jobs, approvals, invoices, media and notification endpoints are specified in the supplied full project prompt and will be introduced in their assigned phases. They are not implemented by the foundation endpoint.
