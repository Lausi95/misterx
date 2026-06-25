# 17. Persistence ports are per-aggregate repositories, not single-method ports

Date: 2026-06-25

## Status

Accepted

## Context

The outbound persistence seam was expressed as ~30 single-method port interfaces, grouped
by aggregate into `application/port/out/{agent,team,finding,game,agentlocation}/`. The agent
aggregate alone had six: `SaveAgentPort`, `GetAgentPort`, `GetAgentsPort`,
`CheckAgentExistsPort`, `CountAgentsByGamePort`, `DeleteAgentPort`. Each declared one (or two,
counting a default helper) method.

The intent of that fragmentation is interface segregation: a service depends only on the
verbs it uses, and any verb could in principle be satisfied by its own adapter. In this
codebase that payoff never materialised:

- **No port ever had a second adapter.** Every aggregate's ports are implemented by exactly
  one `@Component` persistence adapter (`AgentPersistenceAdapter` implements all six agent
  ports). There is no in-memory adapter, no alternative backing store, no read-replica
  variant. The seam at each single-method port is *hypothetical* — nothing varies across it.
- **Tests mock the ports, they do not re-implement them.** ~83 MockK mocks stand in for
  ports across the service tests; not one fake or in-memory implementation exists. Mocking
  works just as well against a coarse interface as a fine one.
- **The fragmentation taxes navigation.** Understanding "what can persistence do with an
  Agent" means opening six files. A reader (human or AI) reconstructs the aggregate's storage
  contract from scattered one-line interfaces.

Per our design vocabulary: one adapter means a hypothetical seam; two adapters means a real
one. These seams had one adapter each, so the segregation bought no substitutability — only
surface area. The single-method ports were *shallow*: interface almost as large as the
implementation.

Options considered:

- **A — Collapse to one repository port per aggregate (chosen).** Replace each aggregate's
  single-method ports with one `*Repository` interface carrying all its verbs. One deep seam
  per aggregate, at the location the sole adapter already occupies.
- **B — Keep single-method ports.** Preserve maximal interface segregation. Rejected: it
  segregates against variation that does not exist and never has, at a real cost to locality
  and navigability.
- **C — One repository spanning several aggregates.** Fewer, broader interfaces (e.g. a
  single `GameplayRepository`). Rejected: it would couple unrelated aggregates behind one
  seam and lose the aggregate as the unit of persistence.

## Decision

The outbound persistence seam is **one DDD repository port per aggregate**:
`AgentRepository`, `TeamRepository`, `TeamMemberRepository`, `FindingRepository`,
`GameRepository`, `AgentLocationRepository`. Each replaces that aggregate's single-method
`*Port` interfaces and lives in the same `application/port/out/<aggregate>/` package.

Conventions:

1. **Method names drop the redundant aggregate noun.** The receiver already names the
   aggregate, so methods read `agentRepository.save(agent, tenant)`,
   `.get(id, tenant)` / `.getOrNull(id, tenant)`, `.exists(id, tenant)`,
   `.delete(id, tenant)`, `.forGame(gameId, tenant)`, `.byIds(ids, tenant)`,
   `.countByGame(gameId, tenant)`.
2. **Get-or-throw and assert helpers stay as interface default methods.** The adapter
   implements only the primitives (`getOrNull`, `exists`); `get` and `requireExists` are
   defaults expressed in terms of them, so the not-found/assert behaviour has one home and is
   not re-implemented per adapter. (The assert helper is named `requireExists`, not `require`,
   to avoid shadowing Kotlin's stdlib `require` inside its own body.)
3. **Spring Data JPA interfaces are renamed `*EntityJpaRepository`** (e.g.
   `AgentEntityRepository` → `AgentEntityJpaRepository`). This marks them unambiguously as
   persistence machinery — the adapter's tool — and distinguishes them from the DDD
   `*Repository` ports, which are the domain-facing seam. `*Entity` / `*Resource` /
   adapter conventions are otherwise unchanged.

`TeamMember` keeps its own `TeamMemberRepository` rather than folding into `TeamRepository`.
It is persisted independently today (own entity, own adapter, saved and queried without
loading its `Team`). Whether `TeamMember` should become part of the `Team` aggregate root is
a separate domain-model decision, deliberately out of scope here.

This is an outbound-seam change only. Inbound `*UseCase` ports are untouched. It is
behaviour-preserving: the pre-existing `get(...)` helper still throws a plain
`IllegalStateException` (via `error(...)`) rather than a domain `NotFoundDomainException`;
that latent inconsistency is preserved here and left for a separate fix.

## Consequences

- **One seam per aggregate.** A reader learns an aggregate's full persistence contract from a
  single interface file. Leverage for callers (one type to inject), locality for maintainers
  (one place the contract lives).
- **Services inject one repository instead of several ports.** A service that needed
  `GetAgentPort` + `SaveAgentPort` now takes one `AgentRepository`. A service spanning several
  aggregates (e.g. `FindAgentService`) injects one repository per aggregate it touches and
  uses only the methods it needs — the same dependency surface, fewer constructor parameters.
- **Tests mock one repository per aggregate.** MockK stubs only the methods a test exercises,
  so coarser interfaces add no stubbing burden; the ~83 single-port mocks become a smaller
  set of repository mocks.
- **Interface segregation is given up deliberately.** If a real second adapter ever appears
  for part of an aggregate (an in-memory fake, a cache-only read path, a separate store), the
  relevant methods can be split back out at that point — when a second adapter makes the seam
  real. Until then, the repository is the right grain. This is the rationale that should stop
  a future review from re-suggesting single-method ports.
- **No wiring change.** Adapters are `@Component`s injected by type and no port had multiple
  implementors, so collapsing the interfaces introduces no bean ambiguity and needs no
  `@Configuration`.
- ADR 0014's prose refers to `GetAgentsPort.getAgentsForGame`; that is now
  `AgentRepository.forGame`. ADR 0014 is left unedited as a point-in-time record; this ADR
  carries the rename.
