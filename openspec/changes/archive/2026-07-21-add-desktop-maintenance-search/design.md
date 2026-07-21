## Context

The selected vehicle's complete maintenance history is already loaded into `MaintenanceHistoryUiState.records` in descending date order. Search is platform-independent presentation behavior and fits the shared MVI boundary even though only Desktop will render a control in this increment.

## Goals / Non-Goals

**Goals:**
- Represent query input and derived results in immutable shared state.
- Keep source-empty and filtered-empty behavior distinct.
- Preserve repository ordering, query state across reloads, and current mutation behavior.
- Provide a clear responsive Desktop search control and clear action.

**Non-Goals:**
- Add repository or SQL filtering.
- Search formatted cost or odometer values.
- Add advanced type, date-range, sorting, or persistent cross-vehicle filters.
- Add the search control to Android in this increment.

## Decisions

### Derive visible records in shared state

`searchQuery`, `visibleRecords`, and `hasNoMatchingRecords` will live in `MaintenanceHistoryUiState`; ViewModel events update only the query. A Desktop-local `remember` filter was rejected because matching rules are platform-independent and require deterministic tests.

### Match normalized textual and ISO date fields

Search will trim the query and use case-insensitive substring matching against maintenance type label, workshop, notes, performed date, and next-due date. Numeric fields are excluded to avoid discrepancies between stored and formatted values.

### Preserve complete source state

`isEmpty` continues to depend on `records`, never `visibleRecords`. Refresh and mutations replace source records while retaining `searchQuery`, so active search remains explicit and can hide nonmatching new records.

## Risks / Trade-offs

- [Risk] Localized type labels can differ from stored labels. -> Match both the stored label and stable maintenance type code name.
- [Risk] A query can hide a newly created or edited record. -> Keep the search field visible and provide a clear action in the filtered-empty state.
- [Risk] In-memory filtering scales linearly. -> Vehicle history is already loaded in memory and expected to remain small; avoid repository complexity until measured.
