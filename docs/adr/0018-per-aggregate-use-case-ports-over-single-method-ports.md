# 18. Inbound ports are per-aggregate use cases, not single-method ports

Date: 2026-06-26

## Status

Accepted

## Context

The inbound application seam was expressed as 25 single-method port interfaces, grouped
by domain noun into `application/port/in/{agent,agentlocation,board,finding,game,leaderboard,team}/`.
The agent aggregate alone had six: `CreateAgentUseCase`, `DeleteAgentUseCase`, `GetAgentUseCase`,
`GetAgentsUseCase`, `GetMyAgentUseCase`, `UpdateAgentUseCase`. Each declared one method (or two,
counting a nested `Command`/`Query` data class).

ADR 0017 applied the same analysis to the outbound persistence seam and collapsed ~30
single-method ports to 6 aggregate repositories. The inbound seam had not yet been addressed.
The same conditions hold:

- **No port ever had a second adapter.** Every use-case port is implemented by exactly one
  `@Service` or `@Component` class. There is no alternative HTTP adapter, no CLI adapter, no
  test-only fake. Each seam is *hypothetical* — nothing varies across it.
- **The fragmentation taxes navigation.** Understanding "what can be done with an Agent"
  means opening six files. A caller (controller or test) reconstructs the aggregate's
  behavioural contract from scattered one-line interfaces.
- **Controllers inject many ports.** `AgentController` wires six use-case ports in its
  constructor. Understanding which operations belong to one aggregate requires reading
  the controller's dependency list.

Per our design vocabulary: one adapter means a hypothetical seam; two adapters means a real
one. These seams had one adapter each, so the segregation bought no substitutability — only
surface area. The single-method ports were *shallow*: interface almost as large as the
implementation.

Options considered:

- **A — Collapse to one use-case port per aggregate (chosen).** Replace each aggregate's
  single-method ports with one `*UseCase` interface carrying all its verbs. One deep seam
  per aggregate, at the location the sole adapter already occupies.
- **B — Keep single-method ports.** Preserve maximal interface segregation. Rejected: it
  segregates against variation that does not exist and never has, at a real cost to locality
  and navigability.
- **C — One use-case spanning several aggregates.** Fewer, broader interfaces. Rejected: it
  would couple unrelated aggregates behind one seam and lose the aggregate as the unit of
  application logic.

## Decision

The inbound application seam is **one use-case port per aggregate**:
`AgentUseCase`, `TeamUseCase`, `GameUseCase`, `FindingUseCase`. Each replaces that
aggregate's single-method `*UseCase` interfaces and lives in the same
`application/port/in/<aggregate>/` package.

Conventions:

1. **Method names keep the verb-noun form.** Unlike the outbound repositories (where
   `agentRepository.save(agent)` reads naturally), use-case methods are called from
   controllers where the receiver is not visible at the call site. `createAgent(command)`
   is unambiguous; `create(command)` is not.
2. **`Command`/`Query` inner classes are renamed with the verb** to stay unambiguous when
   referenced from outside the interface: `CreateAgentCommand`, `UpdateAgentCommand`,
   `GetBoardQuery`, `GetMyAgentQuery`. The verb prefix replaces the former outer-class
   qualifier (`CreateAgentUseCase.Command` → `AgentUseCase.CreateAgentCommand`).
3. **`UpdateAgentLocationUseCase` folds into `AgentUseCase`** as `updateLocation()`.
   Location is an Agent concept; its use case belongs alongside the other agent verbs.
   The `port/in/agentlocation/` package is removed.
4. **`GetBoardUseCase` and `GetLeaderboardUseCase` fold into `GameUseCase`** as
   `getBoard()` and `getLeaderboard()`. Both are game-scoped read models with one
   implementation each; they belong alongside the other game verbs. The `port/in/board/`
   and `port/in/leaderboard/` packages are removed.
5. **`FindingUseCase` is its own aggregate port**, not split across `AgentUseCase` and
   `TeamUseCase`. `AgentFinding` is a real aggregate with its own repository and domain
   model; scattering its verbs across other aggregates would hide that cohesion.
6. **Service implementations merge symmetrically**: the single-method `@Service` classes
   for each aggregate merge into one class per aggregate (`AgentService`, `TeamService`,
   `GameService`, `FindingService`). A delegation layer on top of the existing shallow
   services would introduce a new shallow module; merging eliminates the indirection.
7. **Test classes merge symmetrically**: one `*ServiceTest` per merged service, using
   `@Nested` inner classes to group by method where the file grows large.

`TeamMember` keeps its verbs inside `TeamUseCase` (alongside `Team` verbs) rather than
getting its own `TeamMemberUseCase`. It is part of the Team aggregate's application
surface — registration, membership queries — and has no independent use-case lifecycle.

This is an inbound-seam change only. Outbound `*Repository` ports are untouched.
It is behaviour-preserving: no service logic changes, only the interface it is declared
through.

## Consequences

- **One seam per aggregate.** A reader learns an aggregate's full application contract from
  a single interface file. Leverage for callers (one type to inject), locality for
  maintainers (one place the contract lives).
- **Controllers inject one use case per aggregate they touch.** `AgentController` went
  from six constructor parameters to one `AgentUseCase` (plus `FindingUseCase` for the
  finding-teams query it still needs).
- **Tests mock one use case per aggregate.** MockK stubs only the methods a test exercises;
  coarser interfaces add no stubbing burden.
- **Interface segregation is given up deliberately.** If a real second adapter ever appears
  for part of an aggregate's behaviour (a CLI handler, a webhook adapter, a test fake), the
  relevant methods can be split back out at that point — when a second adapter makes the
  seam real. Until then, the aggregate use case is the right grain. This is the rationale
  that should stop a future review from re-suggesting single-method ports.
- **25 interface files → 4. 25 service classes → 4.** The `port/in/agentlocation/`,
  `port/in/board/`, and `port/in/leaderboard/` packages are removed entirely.
