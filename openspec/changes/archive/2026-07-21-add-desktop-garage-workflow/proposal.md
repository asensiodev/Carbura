## Why

The Desktop application currently presents Garage as an availability placeholder even though its shared vehicle domain, presentation logic, and persistent SQLDelight repositories already support JVM Desktop. Making Garage interactive provides the first useful Desktop workflow and validates that shared business logic can drive a native desktop experience.

## What Changes

- Replace the Desktop Garage placeholder with a responsive vehicle workspace.
- Reuse the shared Garage overview and vehicle form ViewModels for loading, creating, editing, and deleting vehicles.
- Persist Desktop vehicle changes in the existing platform-specific SQLDelight database.
- Add explicit empty, loading, error, validation, confirmation, and mutation feedback states.
- Keep Desktop Garage local-only until Desktop authentication and remote synchronization are introduced.

## Capabilities

### New Capabilities
- `desktop-garage-workflow`: Persistent local vehicle listing and CRUD interactions in the Compose Desktop application.

### Modified Capabilities

## Impact

- Adds the Garage feature dependency and Desktop dependency-injection wiring to `app:desktop`.
- Replaces the Garage branch of the Desktop shell with native Compose Desktop controls.
- Reuses `feature:garage`, `core:domain`, `core:model`, and `core:data` contracts without changing Android behavior.
- Adds Desktop-focused integration and presentation tests.
