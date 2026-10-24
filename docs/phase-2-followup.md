# Phase 2 follow-up

## Changes

- Auth rate limiting uses `X-Forwarded-For` only when the socket peer is the explicitly configured Compose Nginx address. Nginx replaces caller-supplied forwarding headers with its observed peer address. Spring's generic forwarded-header processing is disabled so the API does not trust forwarding headers before the limiter checks the proxy peer.
- Added a backend test proving forwarded IP bucket selection through the trusted proxy and rejection of spoofed forwarding data from an untrusted socket peer.
- Added Angular's Vitest test target, `npm test` script, and first unit tests for the authentication guard and interceptor.
- Updated `AGENTS.md` to reflect completed Phase 1 and Phase 2 scope, and removed the duplicate root `phase-1-status.md` and `er-diagram.md`; the canonical documents remain under `docs/`.

## Verification

Checks run on 24 October 2026:

- Backend: `mvn -B verify` passed in a Maven 3.9.11 / Java 21 container with the Docker socket mounted. All 12 tests passed, including the 8 PostgreSQL Testcontainers auth scenarios and the new trusted-proxy/IP-spoofing rate-limit test; no tests were skipped.
- Frontend production build: `npm run build` passed using Node 22 and npm 11.21.1. Angular generated the production application bundle (311.85 kB initial raw bundle).
- Frontend unit tests: `npm test` passed. Vitest reported 2 test files and 2 tests passed (guard and interceptor).
- Compose config parse: `docker compose --profile mail config -q` passed after the trusted proxy network changes.
- Root duplicate check: the root `phase-1-status.md` and `er-diagram.md` are removed; their canonical `docs/` copies remain.

## Limits

- The trusted proxy address and Compose subnet are a single-instance local setup. If the network addresses change, update `TRUSTED_PROXY_ADDRESSES` to the actual Nginx address and keep the API from trusting any other peers.
- Bucket4j buckets remain process-local; distributed deployments need shared rate-limit storage and explicit proxy-chain configuration.
