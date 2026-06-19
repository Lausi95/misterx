# 2. Fixed-value tenant override for local development

Date: 2026-06-19

## Status

Accepted

## Context

`TenantFilter` resolves the tenant from `request.remoteHost` — the hostname of the
caller. In production the caller resolves to a stable hostname that doubles as the
tenant identifier. In local development this breaks down: `localhost` resolves to a
loopback IP (`127.0.0.1`) and the port is not part of `remoteHost`, so the value the
frontend dev server is reached at (`localhost:3000`) can never be recovered from the
request. Local runs therefore can't produce the tenant the developer wants.

An override path already exists — `tenant.override.enabled=true` plus the
`X-TENANT-OVERRIDE` header — but it requires every local request to carry the header,
which the frontend dev server does not send.

Options considered:

- **Reuse the header override only** — require the frontend to always send
  `X-TENANT-OVERRIDE: localhost:3000`. Rejected: pushes config into the frontend and
  every request.
- **A configured default for the header override** — header wins, fall back to a
  configured value when absent. This is effectively what we chose, framed differently.
- **New fixed-value property** — a configured tenant value applied server-side, no
  header needed. Chosen.

## Decision

Add `tenant.override.value`, grouped under the existing `tenant.override.*` block. The
filter's resolution, gated by `tenant.override.enabled`:

1. If `enabled` and the `X-TENANT-OVERRIDE` header is present → use the header.
2. Else if `enabled` and `tenant.override.value` is set → use that value.
3. Otherwise → `request.remoteHost`.

`tenant.override.value` is set **only** in `application-local.yml` (`value: localhost:3000`).
It is left unset everywhere else, so it is inert outside the `local` profile. The `local`
profile is activated via the IDE run configuration — there is no Gradle/`bootRun` change,
so it never auto-applies.

Header precedence over value is deliberate: the test profile already enables the override
and drives it via the header; keeping the header ahead of the configured value means tests
are unaffected and could still override a fixed value if ever run under the `local` profile.

No dedicated test was added; the behaviour is verified manually during local runs.

## Consequences

- **Local dev "just works"** under the `local` profile without the frontend sending any
  header.
- **No production risk.** With `value` unset and `enabled=false` outside test/local,
  resolution falls through to `remoteHost` exactly as before.
- **Untested precedence.** Controller tests bypass the filter (they inject the `tenant`
  request attribute directly), so the header > value > remoteHost ordering has no automated
  coverage. A future regression here would surface only at runtime.
- **Latent inconsistency left in place.** `Tenant.parse()` performs subdomain-prefix
  extraction and CLAUDE.md describes tenant resolution that way, but the filter returns the
  raw `remoteHost` and never calls `parse()`. This change does not touch that gap; it is
  noted here so the next reader knows the discrepancy predates this decision.
