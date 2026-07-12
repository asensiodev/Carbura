## Why

Carbura currently relies on users to create reminders manually or provide a next due date after recording maintenance. Capturing key due information directly on a vehicle lets the app proactively suggest useful ITV, insurance, and service reminders at the moment users configure their garage.

## What Changes

- Add optional next ITV date, insurance renewal date, and next service odometer fields to vehicle creation and editing.
- Show the reminders Carbura will create or update before the user saves the vehicle.
- Create deterministic vehicle-generated reminders only after user confirmation, preventing duplicates across repeated edits.
- Update, reschedule, or remove the corresponding generated reminder when its vehicle due target changes or is cleared.
- Keep manually created reminders and maintenance-generated reminders independent from vehicle-generated reminders.
- Persist and synchronize the new vehicle due fields and generated reminders through the existing local-first flow.
- Surface upcoming vehicle reminder context for later garage-card and vehicle-detail UX work without redesigning the complete garage UI in this change.

## Capabilities

### New Capabilities

- `proactive-vehicle-reminders`: Optional vehicle due targets, confirmed reminder suggestions, deterministic generated reminders, and lifecycle behavior when vehicle targets change.

### Modified Capabilities

- `vehicle-management`: Extend vehicle creation and editing with optional ITV, insurance, and next-service due targets.
- `initial-data-model`: Store the new optional vehicle due targets in local and remote vehicle records.
- `sync-v0`: Synchronize the additional vehicle due fields while preserving existing local-first and last-write-wins behavior.

## Impact

- Shared `Vehicle` model, vehicle use cases, reminder orchestration, and repository interactions.
- SQLDelight schema and migration plus a versioned Supabase migration for vehicle due fields.
- Vehicle local/remote mappers, sync entities, DTOs, and sync tests.
- Garage MVI creation/editing state and Android date-picker/form UI.
- Existing reminder persistence and Android notification scheduling through stable generated reminder IDs.
- Domain, data, presentation, and migration tests following Red-Green-Refactor.
