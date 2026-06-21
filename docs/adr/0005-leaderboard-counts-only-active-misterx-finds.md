# 5. The Leaderboard counts only active MISTERX finds (scoring is retroactive)

Date: 2026-06-21

## Status

Accepted

## Context

`GET /leaderboard` ranks a Game's Teams by how many MISTERX Agents they have found. A
`AgentFinding` is a permanent historical record — it carries no `type` and is never deleted
when an agent is deactivated — so we had to decide which findings *count* toward a Team's
score. The natural reading of a leaderboard is "credit earned stays earned" (count every
historical find), which conflicts with treating the agent's *current* state as authoritative.

Options considered:

- **Count every historical MISTERX finding regardless of the agent's current `active`
  flag** (true historical record). Rejected by product decision: deactivating a MISTERX
  should pull it out of play entirely, including past scoring.
- **Count finds of agents that are currently MISTERX and `active`.** Chosen.

## Decision

A finding contributes to a Team's Leaderboard score **only if** its agent currently exists,
is of type `MISTERX`, and is `active`. The same predicate gates both the Team's count and the
agents listed under it. This is the same `active` filter the Board uses, applied to a
historical projection.

Ranking: counted-find count descending, then the timestamp of the Team's most-recent counted
finding ascending (earlier wins), then Team id as a stability tie-break. Teams with zero
counted finds occupy the bottom in Team-id order (no meaningful rank among them).

## Consequences

- **Scoring is retroactive.** Deactivating a MISTERX lowers the score of every Team that had
  found it, and removes it from their listed agents — a Team's rank can fall with no action of
  its own. This is intended: deactivation removes an agent from the game, past and present.
- A reactivated MISTERX restores those finds. Scores are always derived from current agent
  state, never frozen.
- The Leaderboard is **unpaginated** — a deliberate exception to the CLAUDE.md "lists must
  accept `Pageable`" rule, because the ranking is global (the full order must be computed
  before any slice) and Team counts per Game are small. It is treated as a composite resource,
  the same exception the Board takes.
