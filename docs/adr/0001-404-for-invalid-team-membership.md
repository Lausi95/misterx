# 1. Return 404 (not 403) when a supplied team membership is invalid

Date: 2026-06-19

## Status

Accepted

## Context

The player-facing `GET /my-team` endpoint resolves the caller's team from request
headers: `X-GameId`, `X-TeamId` (both required) and `X-MemberId` (optional). A client
that registered via `POST /team-register` stores its returned `X-TeamMemberId` and may
replay it here so the server can confirm the saved membership is still valid — e.g. the
team was deleted and recreated, or the member row was purged.

When `X-MemberId` is supplied, the server checks the member exists **and** belongs to the
given team and game (same tenant). The question is what to return when that check fails
(member missing, or pointing at a different team/game).

Options considered:

- **403 Forbidden** — distinct from "team not found"; signals "the team exists but your
  saved membership is invalid, re-register".
- **404 Not Found** — treat an invalid membership like any other missing resource, reusing
  the existing `NotFoundDomainException` → 404 mapping.
- **409 Conflict** — frame it as a client/server state conflict (unconventional here).
- **200 OK + `memberValid: false`** — never fail; add a flag to the shared `TeamResource`.

A related decision: the same endpoint already returns **404** when the team does not exist,
or exists but does not belong to `X-GameId`.

## Decision

Return **404 Not Found** for a failed membership check, the same status used for a missing
or wrong-game team. The endpoint reuses the existing domain helpers (`teamNotFound`,
`teamMemberNotFound`), both of which extend `NotFoundDomainException` and map to 404 via
`HttpExceptionHandler`. No machine-readable discriminator (`type` URI or custom extension
property) is added; the `ProblemDetail` `details` prose is the only differentiator.

## Consequences

- **Uniform client handling.** The client treats every 404 from `/my-team` the same way
  (re-register / restart). It does not need to — and cannot reliably — distinguish
  "team gone" from "membership gone" without string-matching the prose `details`. This was
  explicitly accepted: branching on the cause was deemed unnecessary for the client.
- **No new error contract.** We avoid introducing a 403 path and a new `ProblemDetail`
  shape, keeping the endpoint consistent with the rest of the API's not-found behaviour.
- **`TeamResource` stays unchanged**, shared as-is with `GET /games/{gameId}/teams/{teamId}`.
- **Reversal cost.** Splitting the two cases later (e.g. moving membership failures to 403,
  or adding a machine-readable `code`) is a client-facing contract change. If a future
  client needs to act differently per cause, revisit this decision and add a stable
  discriminator rather than parsing prose.
