# city-game-api

Location-based city game backend — manages games, agents, teams, and real-time location tracking across multiple
tenants.

## Stack

Kotlin 2.2.10 · Spring Boot 4.0.1 · Java 21 · Gradle  
PostgreSQL · Flyway (SQL migrations) · Spring Data JPA  
JUnit 5 · MockK · Testcontainers  
Architecture: Hexagonal (Ports & Adapters)

## Architecture

```
adapter/in/web/           ← REST controllers, filters, exception handlers
adapter/out/persistence/  ← JPA entities, Spring Data repos, persistence adapters
application/port/in/      ← inbound use case ports (interfaces)
application/port/out/     ← outbound repository ports (interfaces)
application/domain/       ← domain models, value objects, services
common/                   ← shared types: Tenant, GeoLocation, QrCode
```

Business logic lives in `application/domain/`. Adapters depend on ports; ports never depend on adapters.

## Development

```bash
./gradlew test              # run tests with JaCoCo coverage
./gradlew bootRun           # start locally (requires Docker for compose.yaml PostgreSQL)
./gradlew jacocoTestReport  # generate HTML coverage report
```

Integration tests require a running Docker daemon — Testcontainers spins up PostgreSQL (and optionally Keycloak).

## Conventions

### Testing

- Use MockK for mocking, not Mockito — Mockito is explicitly excluded from the build.
- Bug fixes: write a failing test that reproduces the bug before making the fix.
- Mark integration tests with `@IntegrationTest` (custom annotation wrapping
  `@SpringBootTest(webEnvironment = RANDOM_PORT)`).
- Use `@ActiveProfiles("test")` and `application-test.yml` for test-specific config overrides.
- Use `bdd/BDD.kt` random data helpers when constructing test fixtures.

### Domain & Naming

- Use case interfaces: `*UseCase`. Implementations: `*Service` in `application/domain/service/`.
- Value objects: use `@JvmInline value class` for IDs and typed strings (e.g., `GameId`, `AgentId`) to prevent
  stringly-typed bugs.
- Domain exceptions extend `DomainException`; not-found variants extend `NotFoundDomainException`. Never throw raw
  `RuntimeException` from domain code.
- Response DTOs are `*Resource` classes; persistence entities are `*Entity`; repositories are `*EntityRepository`.

### Persistence & JPA

- Never add `final` or `sealed` to JPA entities — the `allOpen` Kotlin compiler plugin is required for Hibernate proxy
  generation. Marking entities final breaks lazy loading.
- All repository queries must include tenant filtering (e.g., `findByIdAndTenant`). Tenant isolation is enforced at the
  query level; there is no row-level security fallback.
- Entity ↔ domain model conversion belongs on the persistence adapter via `toEntity()` / `to<Domain>()` methods, not on
  the domain model itself.
- `spring.jpa.open-in-view` is disabled — do not rely on lazy loading outside a transaction boundary.

### Multi-Tenancy

- The tenant **is the frontend origin** (`scheme://host[:port]`, e.g. `https://foo.city-game.net`). It is resolved from
  the request by `TenantArgumentResolver` (via `TenantOriginExtractor`): `X-TENANT-OVERRIDE` header when
  `tenant.override.enabled` → `Origin` header → origin of `Referer` → 400. The same origin is the base URL of the
  tenant's QR codes (`FrontendUriFactory`). `TenantFilter` only puts the origin into the logging MDC. See ADR 0011/0012.
- Validation lives in the `Tenant` type (`init {}` → `InvalidTenantOriginException`), so a `Tenant` can never hold an
  invalid origin. Controllers declare a plain `tenant: Tenant` parameter (no `@RequestAttribute`).
- Every service method receives an explicit `Tenant` argument. Never assume a default or fall back to a hardcoded tenant
  value.

### Performance

- List endpoints must accept `Pageable` and use `@PageableDefault`. Do not return unbounded collections.
- `AgentLocationCache` (ConcurrentHashMap) caches the latest agent position in memory. Read from it before hitting the
  database for hot location queries.

### API Style

- `POST` → 201 Created with a `Location` header pointing to the new resource; echo the created ID in a custom `X-*`
  header.
- `PATCH` → 202 Accepted for partial updates.
- All response objects include a `links` map for HATEOAS-style navigation.
- Annotate every endpoint with `@Operation`, `@ApiResponse`, and `@Tag` — Swagger UI is auto-generated from these
  annotations.
