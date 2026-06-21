# 4. SW-origin (row, column) cell coordinates for MISTERX positions on the Board

Date: 2026-06-21

## Status

Accepted

## Context

The `GET /board` endpoint exposes a MISTERX agent's position as the grid **Cell** that
contains its last-known location, never the exact coordinates (that obfuscation is the
whole point of the type distinction — UTILITY agents are shown exactly, MISTERX only to
cell resolution). A `Map`'s grid is `rows x columns` over the rectangle from `cornerA` (SW)
to `cornerB` (NE). "The cell" is ambiguous without fixing an indexing contract, and clients
will hard-code that contract to render the board — so changing it later silently breaks
every client.

Options considered:

- **Return cell bounds or a center `GeoLocation`** instead of indices. Rejected: the Board
  response already carries the full grid (corners + rows/columns), so indices are
  self-sufficient and the smallest honest representation; bounds/center would be derived
  redundancy.
- **NW-origin, row grows south** (screen/raster convention, row 0 at top). Rejected:
  `cornerA` is defined as SW, so a SW origin maps onto the existing corner semantics with
  no axis flip. Raster-style indexing would force a mental inversion between the map's
  geographic corners and its cell grid.

## Decision

A Cell is `{ row, column }`, both 0-indexed integers. The origin `(0, 0)` is the SW corner
(`cornerA`): `column` increases **eastward** (longitude), `row` increases **northward**
(latitude). The NE corner (`cornerB`) falls in cell `(rows - 1, columns - 1)`.

A MISTERX whose last-known location lies **outside** the `cornerA`-`cornerB` rectangle maps
to no cell and is **omitted** from the Board (clamping to an edge cell would misreport its
position).

## Consequences

- Clients render MISTERX cells directly against the grid in the same response; no extra
  lookup.
- The contract is hard to reverse once clients ship — this ADR is the reference for why it
  is SW-origin rather than the more common raster NW-origin.
- Off-map MISTERX agents silently disappear from the board, the same way agents with no
  location or inactive agents do. There is intentionally no "off-map" signal in v1.
