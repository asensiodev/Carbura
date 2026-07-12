## Why

Vehicles cannot currently be corrected after creation or kept current as their odometer changes. Editing and safe odometer updates are required before Carbura can provide reliable kilometre-based proactive reminders.

## What Changes

- Allow Android users to edit an existing vehicle's name, type, optional license plate, and odometer.
- Provide a quick odometer update action from the vehicle flow.
- Prevent accidental odometer decreases unless the user explicitly confirms the lower value.
- Persist edits through the shared domain and local-first repository, marking the vehicle pending for synchronization.
- Refresh the visible garage and vehicle context after a successful update while preserving clear validation and error states.
- Add domain, data, and presentation tests through the project's TDD workflow.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `vehicle-management`: Add vehicle editing and quick odometer update requirements, including validation and explicit confirmation for decreases.

## Impact

- Shared vehicle repository contract and domain use cases.
- SQLDelight vehicle update query and local-first repository implementation.
- Garage ViewModel state, intents, and Android Compose UI.
- Dependency injection for new use cases.
- Existing sync v0 behavior for local vehicle updates; no remote schema change is expected.
- Domain, repository, ViewModel, and UI-facing acceptance tests.
