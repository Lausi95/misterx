# 6. Generated QR codes derive the frontend origin from the request host

Date: 2026-06-21

## Status

Accepted

## Context

The QR-code endpoints (`setup-qr` for agents and teams, and the new root-level `find-qr`)
encode a frontend URL — `setup-agent`, `setup-team`, `find` — that the recipient opens in a
browser. The original `setup-qr` endpoints hardcoded `.host("localhost:3000")`, so every
environment emitted QR codes pointing at localhost. We needed the frontend origin to be
correct per deployment.

The intended production topology is **one domain per tenant** (`foo.city-game.net`,
`bar.city-game.net`, …), with the API reached at that same domain. So the host of the incoming
request *is* the tenant's frontend domain — the right source of truth for the QR URL. The only
exception is local development, where the API (`:8080`) and the frontend (`:3000`) are
different origins.

We explicitly did **not** derive the host from `tenant.value`: the tenant is resolved from
`request.remoteHost` (the caller's reverse-DNS / last proxy), which is *not* a routable
frontend domain in production. Using it would have produced syntactically valid but dead URLs.

Options considered:

- **Derive from `tenant.value`.** Rejected — `request.remoteHost` is the caller's host, not the
  frontend domain.
- **A fixed `frontend.base-url` config property for every environment.** Workable but adds a
  per-tenant knob that must track the (already request-visible) domain in production.
- **Derive `{scheme}://{host}` from the request, with an optional override.** Chosen.

## Decision

QR-code endpoints build the frontend URL from the **incoming request's scheme and host**
(via `ServletUriComponentsBuilder`, which honours `X-Forwarded-*` so the public domain is used
behind a proxy), then set the path (`/find`, `/setup-agent`, `/setup-team`) and query params.

An **optional `frontend.base-url`** property overrides the request-derived origin where the
frontend lives elsewhere. It is set to `http://localhost:3000` in the `local` profile and left
unset in production, where the request host already is the tenant's domain. Applied uniformly
to `agent/setup-qr`, `team/setup-qr`, and `find-qr`.

The `alias` query value (free-form, mutable text) is percent-encoded via URI-variable
expansion — encoding only the variable, never the structural parts of the URL (notably not the
`host:port` of a `localhost:3000` override).

## Consequences

- **Production correctness depends on the one-domain-per-tenant topology** and on
  `X-Forwarded-Host` being set correctly by the edge. If the frontend ever moves to a separate
  origin from the API (e.g. `app.acme.com` vs `api.acme.com`), that environment must set
  `frontend.base-url` — the override exists precisely for this.
- Local and test behaviour is explicit via the override, not luck: local QR codes point at
  `localhost:3000` regardless of which port the API runs on.
- The hardcoded `localhost:3000` is removed from all three endpoints; the frontend origin is no
  longer baked into application code.
