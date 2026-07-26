## Context

`GarageOverviewUiState` already owns the ordered vehicle list and survives refreshes and local mutations. Vehicle search is platform-independent presentation logic, although only Desktop will expose the control in this increment.

## Goals / Non-Goals

**Goals:**
- Keep query input and derived visible vehicles in immutable shared state.
- Match vehicle name, license plate, and type without changing source ordering.
- Distinguish no vehicles from no matching vehicles.
- Retain an active query through refresh and mutation updates.

**Non-Goals:**
- Add repository or SQL filtering.
- Search odometer or planning targets.
- Persist search between application launches.
- Add Android search UI.

## Decisions

### Derive visible vehicles in shared state

The ViewModel updates only `searchQuery`; `visibleVehicles` and `hasNoMatchingVehicles` are pure projections. Desktop-local filtering was rejected to keep matching deterministic and testable.

### Match stable display fields

Search uses trimmed case-insensitive substring matching against name, optional license plate, and a stable vehicle-type label. Blank search returns the complete source list.

### Preserve source-empty semantics

`isEmpty` continues to represent an actually empty Garage. The Desktop body renders a separate no-matches panel and keeps search clearing available.

## Risks / Trade-offs

- [Risk] An active query can hide a newly created or edited vehicle. -> Keep the search control visible and provide an explicit clear action.
- [Risk] In-memory filtering is linear. -> The complete list is already in memory and expected to remain small.
