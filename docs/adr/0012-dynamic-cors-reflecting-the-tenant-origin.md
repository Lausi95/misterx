# 12. App-level CORS that reflects the resolved tenant origin

Date: 2026-06-22

## Status

Accepted

## Context

ADR 0011 makes the API a single shared host (`backend.city-game.net`) called cross-origin by
every tenant frontend (`foo.city-game.net`, …). Every `fetch()` from a frontend is therefore
a cross-origin request, and the browser will refuse to expose the response to JavaScript
unless the API returns an `Access-Control-Allow-Origin` header matching the caller's origin.

The application has **no CORS configuration today**, and none exists at the Traefik edge
either — so cross-origin `fetch()` calls would be blocked. (`<img>`-loaded QR codes are
unaffected: no-cors image rendering does not require CORS headers.)

The set of allowed origins is dynamic and unbounded — it is exactly the set of valid tenants,
which ADR 0011 currently leaves open (any well-formed origin). A static allowlist would
therefore have to be kept in sync with tenant onboarding, and `Access-Control-Allow-Origin: *`
is illegal once credentials are involved.

## Decision

Add CORS **in the application** (not at the edge), and make it **reflect the same origin the
tenant resolver accepts** — the thing that is a valid tenant is exactly the thing CORS allows.
The allowed origin echoes the request `Origin` after it passes the same well-formed-origin
validation used to construct the `Tenant`. One source of truth for "what is a legitimate
caller."

## Consequences

- **Single source of truth.** CORS and tenant resolution can never disagree about which
  origins are legitimate; both flow from the same validation.
- **When an allowlist is introduced for tenants (ADR 0011 defers this), CORS inherits it for
  free** — the reflected origin is gated by the same check.
- **CORS lives in the app, not Traefik.** If edge-level CORS is ever added, it must not
  double-set the headers; this decision deliberately keeps the policy next to the tenant
  logic it mirrors.
- **`<img>` QR loads need no CORS** and are unaffected; only the `fetch()` path depends on
  this.
