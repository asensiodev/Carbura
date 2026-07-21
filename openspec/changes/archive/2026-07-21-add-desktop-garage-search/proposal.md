## Why

Desktop Garage becomes slower to scan as a family adds more vehicles, especially when names are similar. A shared text search can locate vehicles without changing persistence, ordering, or vehicle ownership.

## What Changes

- Add shared Garage search query and clear events.
- Derive visible vehicles by case-insensitive name, license plate, and vehicle type matching.
- Preserve source order and distinguish an empty Garage from no search matches.
- Add a responsive Desktop search field, clear control, and no-matches state.
- Retain the query across Garage refreshes and mutations.

## Capabilities

### New Capabilities
- `desktop-garage-search`: Shared vehicle matching behavior and Desktop Garage search interaction.

### Modified Capabilities

None.

## Impact

- Extends shared Garage overview presentation state/events and tests.
- Updates Desktop Garage rendering and integration coverage.
- Does not change repositories, schemas, synchronization, or Android UI.
