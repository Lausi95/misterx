# City Game

A location-based city game backend managing games, agents, teams, and real-time
location tracking across multiple tenants.

## Language

**Tenant**:
An isolated customer/deployment partition. The canonical tenant identifier is the
**hostname of the caller** as resolved by `TenantFilter` (`request.remoteHost`), not a
subdomain prefix and not the `Host` header. Every repository query is filtered by tenant;
there is no row-level security fallback.
_Avoid_: Customer, organisation, realm, client

**Tenant override**:
A development/test-only mechanism for supplying the tenant explicitly instead of deriving
it from the caller's host. Gated by `tenant.override.enabled`; sourced from the
`X-TENANT-OVERRIDE` header (preferred) or the configured `tenant.override.value` (used by
the `local` profile). Inert in production.

**Agent**:
A non-team game character that teams try to locate. An agent belongs to exactly one Game,
has a `Type` (`MISTERX` or `UTILITY`), and may carry a last-known location. Teams that have
located an agent are its _finding teams_; the reverse view (agents a team has located) is
the team's _found agents_.
_Avoid_: Player, target, NPC
