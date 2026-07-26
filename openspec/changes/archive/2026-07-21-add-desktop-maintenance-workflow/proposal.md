## Why

Desktop Garage and Reminders now share persistent local vehicle data, but maintenance history remains unavailable even though its state machine, domain orchestration, and SQLDelight repository already support JVM Desktop. Migrating this workflow completes the core local vehicle-care loop while preserving generated reminder consistency.

## What Changes

- Replace the Desktop Maintenance placeholder with a vehicle-aware maintenance history workspace.
- Add Garage-to-Maintenance navigation that carries the selected vehicle through explicit shell state.
- Reuse shared Garage overview state for vehicle selection and the shared Maintenance history ViewModel for per-vehicle loading and mutations.
- Support maintenance creation with canonical/custom types, dates, odometer, cost, workshop, notes, and supported next-due dates.
- Preserve automatic ITV/insurance reminders and the existing future-maintenance reminder offer.
- Support confirmed maintenance deletion and associated generated-reminder cleanup.
- Clearly distinguish stored reminders from unavailable native Desktop notifications.

## Capabilities

### New Capabilities
- `desktop-maintenance-workflow`: Persistent local vehicle selection, maintenance history, creation, generated reminder choices, deletion, and Garage integration in Compose Desktop.

### Modified Capabilities

## Impact

- Adds the Maintenance feature dependency and Koin module to `app:desktop`.
- Extends Desktop shell state with an optional selected maintenance vehicle.
- Adds a Desktop-native Maintenance workspace while retaining shared presentation and domain rules.
- Adds cross-feature integration tests for Garage, Maintenance, Reminders, and SQLDelight persistence without changing Android behavior.
