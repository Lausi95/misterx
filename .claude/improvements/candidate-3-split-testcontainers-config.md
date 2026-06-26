# Candidate 3 — Split `TestcontainersConfiguration` to skip Keycloak in JPA tests

**Strength:** Worth exploring  
**Estimated saving:** 8–15 s per JPA test context on a cold CI runner

## Problem

`TestcontainersConfiguration` always creates both a `PostgreSQLContainer` and a `KeycloakContainer`.
`@DatabaseIntegrationTest` (a `@DataJpaTest` slice) imports this configuration — meaning the two
JPA repository test files start a Keycloak container even though the JPA slice never loads the
security layer and Keycloak is never contacted.

```
@DatabaseIntegrationTest
  = @DataJpaTest + @Import(TestcontainersConfiguration::class)

TestcontainersConfiguration
  @Bean PostgreSQLContainer   ← needed
  @Bean KeycloakContainer     ← wasted: never used by @DataJpaTest
  @Bean DynamicPropertyRegistrar (Keycloak URL → spring.security.oauth2.*)
  @Bean Keycloak (RestClient for token exchange)
```

Files involved:
- `src/test/kotlin/net/lausi95/citygame/TestcontainersConfiguration.kt`
- `src/test/kotlin/net/lausi95/citygame/DatabaseIntegrationTest.kt`
- `src/test/kotlin/net/lausi95/citygame/IntegrationTest.kt`
- `src/test/kotlin/net/lausi95/citygame/adapter/persistence/finding/AgentFindingEntityRepositoryTest.kt`
- `src/test/kotlin/net/lausi95/citygame/adapter/persistence/team/TeamEntityRepositoryTest.kt`

## Proposed solution

Split `TestcontainersConfiguration` into two `@TestConfiguration` classes:

**`DbContainersConfig`** — Postgres only:
```kotlin
@TestConfiguration(proxyBeanMethods = false)
class DbContainersConfig {
    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer =
        PostgreSQLContainer(DockerImageName.parse("postgres:latest"))
}
```

**`SecurityContainersConfig`** — Keycloak + Postgres (extends Db, or composes it):
```kotlin
@TestConfiguration(proxyBeanMethods = false)
@Import(DbContainersConfig::class)
class SecurityContainersConfig {
    @Bean
    fun keycloakContainer(): KeycloakContainer =
        KeycloakContainer().withRealmImportFile("/keycloak.json")

    @Bean
    fun registerKeycloakProperties(kc: KeycloakContainer): DynamicPropertyRegistrar = ...

    @Bean
    fun keycloak(...): Keycloak = ...
}
```

Then update:
- `@DatabaseIntegrationTest` → `@Import(DbContainersConfig::class)` (drop Keycloak)
- `@IntegrationTest` → `@Import(SecurityContainersConfig::class)` (unchanged behaviour)
- Delete `TestcontainersConfiguration.kt`

## Design vocabulary (from `/codebase-design` skill)

- `TestcontainersConfiguration` is a **shallow module**: its interface (everything a caller must know
  to use it) forces both PostgreSQL and Keycloak on callers that only need one.
- Splitting deepens the interface: `DbContainersConfig` has a smaller interface with the same Postgres
  leverage, and callers get exactly what they depend on.
- The seam here is `@Import` — each annotation is an adapter slot.

## How to explore in a future session

Invoke the grilling skill to walk the remaining design decisions:

```
/grilling Split TestcontainersConfiguration into DbContainersConfig (Postgres only) and
SecurityContainersConfig (Keycloak + Postgres) to avoid starting Keycloak in @DataJpaTest
contexts. Key open questions: should SecurityContainersConfig compose DbContainersConfig via
@Import, or duplicate the Postgres bean? Should the old TestcontainersConfiguration.kt be
deleted or kept as a typealias? Are there any other test classes beyond
TenantResolutionIntegrationTest.kt that use @IntegrationTest?
```

Then implement and verify by running `./gradlew test` — Keycloak container should not appear
in the log output for `AgentFindingEntityRepositoryTest` or `TeamEntityRepositoryTest`.
