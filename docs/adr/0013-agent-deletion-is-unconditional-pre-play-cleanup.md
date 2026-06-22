# 13. Agent deletion is unconditional pre-play cleanup, not mid-game retirement

Date: 2026-06-22

## Status

Accepted

## Context

An Organizer needs to remove an Agent that should not be in the Game — typically an
agent that was created but never showed up before play, or one created by mistake or
during test setup. The request surfaced as "let an admin delete agents", with the
acceptance criteria that the agent's location data and finding entries are deleted too.

The system already has a softer mechanism: every Agent carries an `active` flag, and the
Leaderboard counts only findings of agents that currently exist, are MISTERX, and are
`active` (see ADR 0005). Deactivating an agent already pulls it out of play and out of
scoring, *retroactively and reversibly*, without losing data. So hard deletion is not
needed to take an agent out of a running game — deactivation does that.

The danger of hard deletion is the cascade onto `agent_finding`. A `Finding` is a Team's
recorded accomplishment, modelled as a permanent historical record (see `CONTEXT.md` and
ADR 0005). Deleting an agent mid-game would permanently erase *other actors'* scored
history and silently rewrite the Leaderboard, with no way back. That is a footgun if
deletion is offered as a general "retire this agent" tool.

We therefore had to decide what deletion is *for*, and whether to guard it.

Options considered:

- **Guarded delete** — block deletion (e.g. `409 Conflict`) when the agent has any
  findings, steering the Organizer to deactivation instead. Safer, but adds a domain rule,
  a new error path, and a second decision for the Organizer in the common case where there
  is nothing to guard.
- **Unconditional cascade, scoped by intent to pre-play cleanup.** Chosen. Deletion always
  removes the agent and whatever findings/locations exist. In the intended use (no-show /
  mistake / test cleanup) there are no findings, so the destructive cascade is a no-op in
  practice; deactivation remains the tool for taking a *played* agent out of the game.

## Decision

`DELETE /games/{gameId}/agents/{agentId}` performs an **unconditional** cascade, scoped by
intent — not by an enforced guard — to cleaning up agents outside active play:

1. Delete the agent's findings (`agent_finding` by `agent_id` + `tenant`).
2. Delete the agent's locations (`agent_location` by `agent_id` + `tenant`).
3. Evict the agent from `AgentLocationCache`.
4. Delete the agent (`agent` by `id` + `tenant`).

All four steps run in a single `@Transactional` unit. Cache eviction happens inside the
transaction: a rollback leaves the cache empty while the row still exists, which is
harmless because the next location read re-resolves from the database.

The endpoint mirrors the established Team-deletion pattern: it lives in the OAuth-protected
`/games/**` organizer tree (ADR 0007), returns `204 No Content`, is **idempotent** for a
missing agent (silently succeeds rather than 404), and returns `404` when the agent exists
but belongs to a different game (the same membership check Team deletion makes, ADR 0001).

Deletion is **not** the mechanism for taking a played agent out of the game — **deactivation
(`active = false`) is**, and it remains reversible and non-destructive.

## Consequences

- **Deleting an agent that has been found is permanently destructive**, by design. It
  removes other Teams' findings for that agent and lowers their Leaderboard scores with no
  way to restore them. This is acceptable only because deletion is intended for pre-play
  cleanup; for a played agent the Organizer must deactivate, not delete. Nothing in the API
  *enforces* this boundary — it is a usage contract, deliberately chosen over a guard to
  keep the common no-show-cleanup flow friction-free.
- The finding/location deletes are belt-and-suspenders: in the intended case they remove
  zero rows, but they guarantee no orphaned `agent_finding` / `agent_location` rows ever
  reference a deleted agent.
- **Re-creation race (known, accepted limitation).** The location-reporting path is
  participant-facing and unauthenticated (ADR 0007). A device still pinging after deletion
  could re-create `agent_location` rows and repopulate the cache for an agent that no longer
  exists. We do not guard the write path, because verifying agent existence on every
  location write would defeat `AgentLocationCache` (the hottest write path), and the
  pre-play-cleanup use case has no device pinging. If mid-game deletion is ever introduced,
  this must be revisited.
- This refines ADR 0005's statement that a finding "is never deleted when an agent is
  deactivated": deactivation still never deletes findings; **agent deletion does**. Because
  the Leaderboard already excludes findings whose agent no longer exists, a deleted agent's
  findings stop counting either way — the cascade simply removes the now-uncountable rows.
