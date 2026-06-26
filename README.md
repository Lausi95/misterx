# City Game API

[![Deployment Pipeline](https://github.com/Lausi95/city-game-api/actions/workflows/deploy.yml/badge.svg)](https://github.com/Lausi95/city-game-api/actions/workflows/deploy.yml)

Backend REST API for **City Game** — a real-world, location-based hide-and-seek game played across a city.

**Frontend:** [city-game-frontend](https://github.com/Lausi95/city-game-frontend) · **API Docs:** [Swagger UI](https://api.city-game.eu/swagger-ui/index.html)

---

## What is City Game?

An organizer creates a game, defines the map area and timeframe, and sets up **agents** (the hunted) and **teams** (the hunters). Agents and team members register themselves via QR codes. Once the game starts, agents scatter across the city and teams hunt them down — scanning an agent's QR code records a **finding** and scores a point. The team with the most found agents at the end wins.

### Domain Vocabulary

| Term | Meaning |
|------|---------|
| **Organizer** | The person who creates and manages a game. The only actor that holds an account and authenticates. |
| **Game** | A time-bounded event with a title, start/end time, and a geographic map. |
| **Map** | The bounded play area: two geographic corners (SW + NE) defining a rectangle, divided into a configurable grid of cells. |
| **Agent** | A field player that teams try to locate. Type `MISTERX` scores points when found; type `UTILITY` does not. |
| **Team** | A group of participants hunting agents. Members register via a team setup QR code. |
| **Finding** | The event of a team scanning an agent's QR code, recorded once with a timestamp. |
| **Board** | The live game view: the map grid overlaid with agent positions (MISTERX positions shown as grid cells, never exact coordinates). |
| **Leaderboard** | Teams ranked by number of active MISTERX agents found, with ties broken by earliest find time. |
| **Tenant** | An isolated game deployment, identified by the frontend's origin (`scheme://host[:port]`). All data is tenant-scoped. |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.2.10 / Java 21 |
| Framework | Spring Boot 4.0.1 |
| Build | Gradle (Kotlin DSL) |
| Database | PostgreSQL + Flyway migrations |
| Persistence | Spring Data JPA / Hibernate |
| Auth | OAuth2 Resource Server (Keycloak JWT) |
| Testing | JUnit 5 · MockK · Testcontainers |
| Container | Distroless Java 21, non-root |

---

## Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters):

```
adapter/in/web/           ← REST controllers, filters, exception handlers
adapter/out/persistence/  ← JPA entities, Spring Data repos, persistence adapters
application/port/in/      ← inbound use-case ports (interfaces)
application/port/out/     ← outbound repository ports (interfaces)
application/domain/       ← domain models, value objects, services
common/                   ← shared types: Tenant, GeoLocation, QrCode
```

Business logic lives in `application/domain/`. Adapters depend on ports; ports never depend on adapters. Architectural decisions are documented in [`docs/adr/`](docs/adr/).

---

## API Surface

There are two distinct client surfaces:

**Organizer surface** (`/games/**`) — authenticated via Keycloak JWT, used by the frontend to manage games, agents, and teams.

**Participant surface** — public endpoints serving the team client. The game and team are supplied via request headers (`X-GameId`, `X-TeamId`) rather than the path or a security principal:

| Endpoint | Purpose |
|----------|---------|
| `GET /board` | Live map with agent positions for the active game |
| `GET /leaderboard` | Team rankings for the active game |
| `POST /find` | Record a finding when a team scans an agent QR code |
| `GET /my-agent` | Agent's own view (location updates, QR setup) |
| `GET /my-team` | Team's own view (members, found agents) |

Full interactive documentation: [api.city-game.eu/swagger-ui/index.html](https://api.city-game.eu/swagger-ui/index.html)

---

## Running Locally

### Prerequisites

- Java 21+
- Docker (for PostgreSQL via Docker Compose)

### Start the application

```bash
# Start PostgreSQL
docker compose up -d

# Run the application with the local profile (auth disabled)
./gradlew bootRun --args='--spring.profiles.active=local'
```

The `local` profile disables OAuth2 entirely so you can exercise all endpoints — including the organizer surface — without a running Keycloak instance.

The API will be available at `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

### Run the tests

```bash
./gradlew test
```

Integration tests use Testcontainers and require a running Docker daemon. They spin up PostgreSQL automatically — no manual setup needed.

```bash
# Generate a coverage report (HTML at build/reports/jacoco/test/html/)
./gradlew jacocoTestReport
```

---

## Authentication

The organizer management surface (`/games/**`) is protected by **OAuth2 JWT**. All other endpoints are intentionally public — participant clients are identified by request headers, not a security principal.

To run with authentication enabled, set the Keycloak issuer URI:

```yaml
# application.yml / environment variable
keycloak-realm: https://<your-keycloak-host>/realms/<realm-name>
```

There is no role or permission gating — any valid JWT from the configured issuer grants organizer access.

For local development, use the `local` Spring profile (see above) to skip auth entirely.

---

## Multi-Tenancy

The **tenant is the frontend origin** — the `scheme://host[:port]` of the calling browser (e.g. `https://foo.city-game.net`). It is resolved from the `Origin` request header (falling back to the origin of `Referer`). Every database query is scoped to the resolved tenant; there is no row-level security fallback.

In tests and tooling (curl, Postman), supply the tenant via the `X-TENANT-OVERRIDE` header. This is gated by the `tenant.override.enabled` config flag and is inert in production.

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Open a pull request against `main`

> **Note:** Merging to `main` triggers an automatic deployment to production. PRs are reviewed before merge.

### Conventions (short version)

- Use **MockK** for mocking, not Mockito
- Bug fixes: write a failing test first, then fix
- Domain exceptions extend `DomainException`; not-found variants extend `NotFoundDomainException`
- IDs and typed strings use `@JvmInline value class` (e.g. `GameId`, `AgentId`)
- All repository queries must include tenant filtering
- List endpoints must accept `Pageable` — no unbounded collections

See [`CLAUDE.md`](CLAUDE.md) for the full coding conventions and [`docs/adr/`](docs/adr/) for architectural decisions.

---

## License

MIT — see the [LICENSE](LICENSE) file.

---

## Contact

Tom Lausmann — tomlausmann@gmail.com
