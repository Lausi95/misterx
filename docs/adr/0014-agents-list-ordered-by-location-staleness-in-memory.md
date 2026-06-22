# 14. The agents list is ordered by location staleness, sorted and paginated in memory

Date: 2026-06-22

## Status

Accepted

## Context

An Organizer watching a Game wants the agents list (`GET /games/{gameId}/agents`) to
surface the agents whose whereabouts are least certain. The requested ordering is by
**location staleness**: the agent whose last-known location is *oldest* appears first, and
an agent that has never reported a location is treated as infinitely stale and sits at the
very top. Ties break by `alias`.

The ordering key is awkward for the existing machinery for three reasons:

1. **It is not a column on the agent.** A last-known location lives in a separate
   `agent_location` table and is attached to the `Agent` *after* the agent is loaded — the
   service enriches each agent via `AgentLocationCache` (falling back to the latest
   persisted `agent_location` row). There is nothing on the `agent` table to `ORDER BY`.
2. **The list paginates at the database level.** `findByGameIdAndTenant(gameId, tenant,
   pageable)` slices the page in SQL, which happens *before* any location is known — so the
   database cannot order a page by a key it cannot see.
3. **Spring's `Pageable.sort` cannot express it anyway.** The key is derived, and the
   ordering needs `NULLS FIRST` semantics that a `?sort=` token does not carry.

Note that "age" never has to be computed. `now` is constant across every row in one
request, so ordering by age-descending is identical to ordering by location timestamp
**ascending, nulls first** — no arithmetic, no clock reads in the comparator.

A game holds on the order of ~20 agents (MISTERX + UTILITY combined), and that is not
expected to grow by orders of magnitude. That bound is what makes the decision below safe.

Options considered:

- **A — Sort in memory (chosen).** Fetch *all* agents for the game, enrich each with its
  location through the existing cache-backed path, order the full set, then slice the
  requested page in memory. No schema change, no write-path change; reuses the enrichment
  that already runs. Costs a full fetch-and-enrich per request and moves pagination out of
  SQL.
- **B — Database join on `MAX(timestamp)`.** A custom query left-joining each agent to the
  newest `agent_location` row, ordered `NULLS FIRST`, keeping SQL pagination. Avoids the
  full fetch but is the most complex query, bypasses `AgentLocationCache` for this path, and
  needs careful tenant filtering. Unjustified at ~20 agents.
- **C — Denormalize `last_location_at` onto the agent.** A real, indexable column updated on
  every location write and cleared on location delete; the existing SQL pagination then just
  works. Scales to any size, but adds a Flyway migration, a backfill, and couples the hot
  location write-path to the agent row for a benefit we do not need at this scale.

## Decision

`GET /games/{gameId}/agents` returns agents in a **fixed, server-imposed order**, computed
in memory:

1. `GetAgentsPort` gains an unpaginated `getAgentsForGame(gameId, tenant): List<Agent>`
   (backed by `findByGameIdAndTenant(gameId, tenant)` returning a `List`, tenant-filtered
   like every other query).
2. `GetAgentsService` loads that list, enriches each agent with its location via the
   existing `GetAgentLocationPort` (cache first, then the latest persisted row), and orders
   the full set by:
   - **location timestamp ascending, nulls first** — never-located agents first, then the
     oldest last-known location;
   - then **`alias` ascending**, using a German, case-insensitive collation (`Collator`
     for `Locale.GERMAN` at `SECONDARY` strength) so it folds case and orders umlauts next
     to their base letter — the in-memory equivalent of the `de-DE-x-icu` column collation
     the teams list uses (ADR 0015);
   - then **`agentId` ascending** as a final stable tiebreak.
3. The service then slices the requested page itself and returns a `PageImpl(content,
   pageable, totalAgents)`. It honours the incoming `Pageable`'s page number and size but
   **ignores its sort** — the ordering is not client-selectable.

## Consequences

- **The endpoint fetches and enriches every agent in the game on each request, then
  paginates in memory** — a deliberate deviation from the conventions that list endpoints
  page in the database and never materialise unbounded collections. It is acceptable *only*
  because the agent count per game is small and bounded (~20). If a game could hold
  thousands of agents this must move to option C (denormalised `last_location_at`); the
  ordering contract above would not change.
- **The client cannot choose the sort.** `?sort=` on this endpoint is ignored. This is
  intentional: the key is derived and needs nulls-first semantics that `Pageable.sort`
  cannot express. Re-introducing client-selectable sorting is a future decision, not a
  regression.
- Pagination is **stable** — the `agentId` final tiebreak gives a total order, so pages do
  not reshuffle between requests even when two agents share both staleness and alias.
- The ordering reflects the **latest persisted location**. Because every location write
  updates the database and the cache together, the cache is never ahead of or behind the
  persisted maximum, so the in-memory order matches what a database query would produce.
