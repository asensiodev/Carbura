## Why

Desktop maintenance history becomes difficult to scan as records accumulate, while the current timeline offers no way to locate a service, workshop, note, or date. Search can improve navigation entirely in shared presentation state without changing persistence or record ordering.

## What Changes

- Add shared maintenance search query events and immutable derived search state.
- Match type labels, workshop, notes, performed date, and next-due date using trimmed case-insensitive substring search.
- Preserve source ordering and distinguish no records from no matching records.
- Add a Desktop search field, clear action, and filtered-empty state.
- Retain the current query across refreshes and mutations, while switching vehicles starts a fresh search context.

## Capabilities

### New Capabilities
- `desktop-maintenance-search`: Search behavior and Desktop interaction for a selected vehicle's maintenance history.

### Modified Capabilities

None.

## Impact

- Extends shared maintenance presentation state/events and tests.
- Updates the Desktop maintenance workspace and integration coverage.
- Does not change repositories, SQL queries, schemas, synchronization, or Android UI.
