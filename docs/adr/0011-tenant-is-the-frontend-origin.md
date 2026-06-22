# 11. The tenant is the frontend origin, resolved from `Origin`/`Referer`

Date: 2026-06-22

## Status

Accepted

Supersedes [0002](0002-local-profile-tenant-override.md) and [0006](0006-frontend-host-derived-from-tenant-value.md)

## Context

The tenant was historically resolved from `request.remoteHost` — the reverse-DNS of the
caller / last proxy — and stored as a bare string (`acme`, or whatever `remoteHost`
produced). This had three long-standing problems:

- `remoteHost` is the *network peer*, not the frontend the user is actually on. It is not a
  routable domain, which is why ADR 0006 had to derive QR-code URLs from a *separate*
  mechanism (`frontend.base-url` / request origin) rather than from `tenant.value`.
- It could not represent a port, so local development needed a configured fixed-value
  override (ADR 0002) just to produce `localhost:3000`.
- `Tenant.parse()` did subdomain extraction that the filter never actually called — dead,
  misleading code (noted as a latent inconsistency in ADR 0002).

The deployment topology has also changed and now contradicts ADR 0006's premise. The API is
a **single shared host** (`backend.city-game.net`) serving every tenant; each tenant has its
**own frontend domain** (`foo.city-game.net`, `bar.city-game.net`, …). The frontend is
therefore always a *different origin* from the API, and the thing that identifies a tenant is
**which frontend origin made the call**.

That origin is exactly what the user's browser is on, and it is carried on the request:

- `fetch()` calls (including GET) run in CORS mode and send the **`Origin`** header.
- `<img src>` loads (used for QR-code PNGs) run in *no-cors* mode and send **no `Origin`**,
  but do send **`Referer`**, whose origin component — under the default
  `strict-origin-when-cross-origin` policy — is the bare cross-origin we want.

Both forms carry scheme + host + port, which is precisely the identifier we want:
`https://foo.city-game.net` in production, `http://localhost:3000` in local development
(frontend `:3000` → API `:8080`, already cross-origin so the browser sends it automatically).

## Decision

**The tenant *is* the frontend origin.** `Tenant.value` holds a canonical origin string
(`scheme://host[:port]`, no path/query/fragment, no trailing slash — exactly what a browser
emits in `Origin`).

**Resolution order** (in a thin `TenantFilter` that extracts the raw string, plus a
`HandlerMethodArgumentResolver` that validates and constructs the `Tenant`):

1. If `tenant.override.enabled` and the `X-TENANT-OVERRIDE` header is present → use it.
   (Retained for tests and curl/Postman tooling, which carry no browser headers.)
2. Else the **`Origin`** header.
3. Else the origin extracted from the **`Referer`** header (path/query stripped, origin
   rebuilt as `scheme://host[:port]`). This is what makes `<img>`-loaded QR codes work.
4. Else → **400** (`InvalidTenantOriginException`, a `DomainException`).

**Validation lives in the `Tenant` type.** The constructor enforces the canonical-origin
invariant in an `init {}` block and throws `InvalidTenantOriginException` (→ 400) on a
malformed or missing value; `Tenant.fromOrigin(raw)` is the named factory. The value object
can therefore never hold an invalid origin, in production or in tests. Input is **rejected,
not normalised** (a trailing slash is an error, not something we trim) so the persisted key
stays byte-identical to what the browser sends. `Tenant.parse()` (subdomain extraction) is
removed.

**Validation/error mechanics reuse the existing pipeline.** Because the argument resolver
runs *inside* the DispatcherServlet, a thrown `DomainException` is handled by the existing
`HttpExceptionHandler` `@RestControllerAdvice` and rendered as a `ProblemDetail` — no JSON
hand-written in a filter. Endpoints that take no `Tenant` parameter (actuator, swagger, the
OAuth surface) are unaffected and never 400 on a missing origin.

**QR codes and frontend URLs derive from `tenant.value`.** `FrontendUriFactory` builds URLs
as `UriComponentsBuilder.fromUriString(tenant.value).path(...)`. The request-derived origin
and the `frontend.base-url` property are **both removed** — the tenant *is* the origin, so QR
origin and tenant identity are the same string and can never drift. (CORS for the `fetch()`
path is a direct consequence — see ADR 0012.)

No allowlist of valid origins yet: any well-formed origin is accepted as a tenant. A bogus
origin yields only an empty, isolated partition. Onboarding gating can be added later.

## Consequences

- **`tenant.value` is now a full origin** (`https://foo.city-game.net`). Existing persisted
  rows use the old bare-string format and are discarded — this is a greenfield change with no
  production data and no Flyway data migration.
- **The `<img>` QR path depends on `Referer`.** If a frontend ever sends
  `Referrer-Policy: no-referrer` (or otherwise strips it), `<img>`-loaded QR requests lose
  their only tenant signal and 400. The default browser policy is safe; this constraint is
  accepted and called out here as the one known fragility.
- **Test fallout is mechanical but wide.** `Tenant("acme")` is now illegal; fixtures move to
  real origins (`Tenant("https://acme.city-game.net")`), `BDD.aTenant()` generates a random
  valid origin, and controller-slice tests stop injecting the `tenant` request attribute —
  they register the resolver and send an `Origin` header, closer to the real request flow.
- **`frontend.base-url` and `tenant.override.value` are gone.** Local dev "just works"
  because the browser sends its own origin; tooling uses the `X-TENANT-OVERRIDE` header.
- **Controllers drop `@RequestAttribute tenant: Tenant`** in favour of a plain `tenant: Tenant`
  parameter resolved centrally (~13 controllers, a mechanical edit).
