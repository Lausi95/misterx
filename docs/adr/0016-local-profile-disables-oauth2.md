# The `local` profile disables OAuth2 entirely

To let the frontend and backend be tested together without running Keycloak, the `local`
profile turns authentication **off**: a `@Profile("local")` `SecurityFilterChain` makes every
request — including the otherwise-protected `/games/**` organizer tree (see ADR 0007) — public,
while CORS, stateless sessions and disabled CSRF stay identical to the default chain. The
default chain that wires `oauth2ResourceServer { jwt {} }` is now annotated `@Profile("!local")`,
so it (and the JWT validation) still runs in `prod` and in integration tests (`test` profile,
real Keycloak Testcontainer) exactly as before.

Because the base `application.yml` keeps `issuer-uri: ${keycloak-realm}` (needed by `prod` and by
the dynamically-overridden `test` profile), Spring would still build an eager `JwtDecoder` and
reach out to Keycloak at startup. So `application-local.yml` also excludes
`OAuth2ResourceServerAutoConfiguration` via `spring.autoconfigure.exclude` — without it the app
could not boot with Keycloak down, which is the whole point.

## Considered Options

- **`permitAll` but keep `oauth2ResourceServer` configured** — rejected: the eager `JwtDecoder`
  still fetches OIDC metadata from Keycloak at startup, so the app would not boot offline.
- **Run a local Keycloak (e.g. via compose)** — rejected: keeps auth on locally but adds a
  heavyweight dependency to the inner dev loop, which is exactly what we wanted to avoid.

## Consequences

- The `local` profile no longer exercises the security chain, so an auth regression can pass
  locally and only surface in `prod`/integration tests. The `test` profile (real Keycloak) and
  the `WebSecurityConfigurationTest` slice remain the source of truth for auth behaviour; a
  parallel `@ActiveProfiles("local")` slice pins the auth-*off* behaviour so the bypass can't
  silently change shape.
- `local` config diverges from `prod` on a security-relevant axis. The divergence is confined to
  one profile file plus one profile-annotated bean, and `local` is never an activatable profile
  in any deployed artifact.
