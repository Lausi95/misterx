# OAuth2 protects only the organizer tree (`/games/**`)

The app is an OAuth2 resource server validating Keycloak-issued JWTs (issuer already
configured in `application.yml`). Only the organizer/management tree — `/games/**`
(games, agents, teams, team members) — requires authentication. Every other endpoint
(`/board`, `/leaderboard`, `/location`, `/find`, `/find-qr`, `/my-team`, `/my-agent`,
`/team-register`) is intentionally public.

**Why the asymmetry is deliberate, not a hole:** participant-facing endpoints identify the
caller from request headers (`X-GameId`, `X-AgentId`, `X-TeamId`, `X-MemberId`), never from a
security principal. Players join via QR/registration flows and carry no token, so requiring
authentication there would be incoherent. Organizers are the only actors with accounts, and
they only act through `/games/**`.

The Spring Security rule is therefore the *inverse* of the usual scaffold:
`requestMatchers("/games/**").authenticated()` then `anyRequest().permitAll()` (stateless
session, CSRF disabled — it is a token resource server). Authentication only: any valid
realm token authorizes any `/games/**` call; no role→authority mapping and no `@PreAuthorize`.

**Accepted trade-off — cross-tenant tokens.** Keycloak is a *single* realm while tenants are
derived from the request hostname (`TenantFilter`), so one valid token is technically valid on
every tenant's hostname. We deliberately do **not** bind the token to the hostname tenant:
isolation stays purely at the query level. This leaves a known gap (an organizer for tenant A
could operate on tenant B by targeting B's hostname with their token). Closing it would require
Keycloak to issue a tenant claim plus custom validation; deferred until multi-organizer tenancy
is real.

Verified by a dedicated `@WebMvcTest` (filters enabled) using `spring-security-test`'s `jwt()`
post-processor. Existing controller slices use `@AutoConfigureMockMvc(addFilters = false)` and
are unaffected.
