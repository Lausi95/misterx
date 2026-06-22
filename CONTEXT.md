# City Game

A location-based city game backend managing games, agents, teams, and real-time
location tracking across multiple tenants.

## Language

**Tenant**:
An isolated customer/deployment partition. The canonical tenant identifier is the
**frontend origin** — the `scheme://host[:port]` the calling frontend is served at, e.g.
`https://foo.city-game.net` or `http://localhost:3000`. It is resolved from the request's
`Origin` header (falling back to the origin of `Referer`), not from `request.remoteHost`, a
subdomain prefix, or the API's own `Host` (see ADR 0011). The same origin is the base URL of
that tenant's QR codes. Every repository query is filtered by tenant; there is no row-level
security fallback.
_Avoid_: Customer, organisation, realm, client; "host" / "hostname" (the tenant is a full
origin, not a bare host)

**Production profile (`prod`)**:
The Spring profile active in the deployed, containerised environment (`application-prod.yml`,
`SPRING_PROFILES_ACTIVE=prod`). It configures logstash-structured logging and production log
levels. Despite the historical name, the deployment is **Docker Compose on a single private
server behind Traefik — not Kubernetes**; the profile was renamed from `k8s` to remove that
false implication (see ADR 0009/0010). There is no Kubernetes cluster.
_Avoid_: k8s, kube, kubernetes (no cluster exists), staging (there is only production)

**Tenant override**:
A test/tooling-only mechanism for supplying the tenant explicitly instead of deriving it from
the browser's `Origin`/`Referer` headers — used by MockMvc/integration tests and curl/Postman,
which carry no browser origin. Gated by `tenant.override.enabled`; sourced from the
`X-TENANT-OVERRIDE` header, whose value is now a full origin (`http://localhost:3000`). Inert
in production. The former configured `tenant.override.value` fixed default was removed once the
browser began supplying its origin automatically (see ADR 0011).

**Organizer**:
The person who runs a Game: creates it and manages its Agents, Teams, and members. The only
actor that holds an account and authenticates; acts solely through the `/games/**` management
surface. Distinct from **participants** (Teams, team members, and the people operating Agents),
who hold no account and are identified by request headers from their QR/registration flow (see
ADR 0007).
_Avoid_: Admin, owner, host, user

**Agent**:
A non-team game character that teams try to locate. An agent belongs to exactly one Game,
has a `Type` (`MISTERX` or `UTILITY`), and may carry a last-known location. Teams that have
located an agent are its _finding teams_; the reverse view (agents a team has located) is
the team's _found agents_.
_Avoid_: Player, target, NPC

**Board**:
The live, caller-specific view of a Game's playfield: the Map (corners + grid) overlaid with the
currently-visible Agent positions. Distinct from the **Map**, which is static configuration (the
bounded area and its grid); the Board is gameplay state and changes as agents move and teams find
them. Served by `GET /board`, with the Game supplied via the `X-GameId` header and the optional
viewing Team via `X-TeamId` (this endpoint is the team-client surface and carries identity in
headers rather than the path).
_Avoid_: Map (that is the static config), Playfield, View

**Cell**:
One rectangle of a Map's grid, identified by `(row, column)` integer indices. The origin
`(0, 0)` is the SW corner (cornerA): `column` increases eastward (longitude), `row` increases
northward (latitude); the NE corner (cornerB) sits in cell `(rows-1, columns-1)`. A MISTERX
Agent's position is exposed on the Board as the Cell containing its last-known location (never
the exact coordinates). A location outside the cornerA–cornerB rectangle maps to no Cell.
_Avoid_: Tile, square, sector

**Finding**:
The single event of a Team locating an Agent, recorded once with the moment it happened (its
_found time_). A team can find a given agent only once. There is no separate "agent finds
team" event: a team's _found agents_ and an agent's _finding teams_ are two views of the same
findings, and both carry the same found time.
_Avoid_: Catch, capture, sighting, discovery

**Finding QR**:
The QR code an Agent presents so that a Team can trigger a **Finding** by scanning it. It
encodes a frontend URL that opens the find page for that Agent. The code carries only the
Agent's identity and a display label (its `alias`) — never the game, team, or member, because
the scanning client already holds its own game/team/member identity from earlier team setup.
Distinct from the **Setup QR**, which provisions an Agent or Team rather than recording a find.
_Avoid_: Find code, catch code, scan code

**Leaderboard**:
The ranked standing of all of a Game's Teams by how many **MISTERX** Agents they have found.
Only findings of agents that currently exist, are MISTERX, and are `active` are **counted** —
deactivating a MISTERX retroactively removes it from every Team's score (see ADR 0005). Teams
rank by counted-find count descending, then by the time of their most-recent counted finding
ascending (earlier "got there first" wins). Teams with no counted finds sit unordered at the
bottom. Served by `GET /leaderboard` with the Game supplied via the `X-GameId` header — the
same team-client surface as the Board.
_Avoid_: Scoreboard, ranking, standings
