## Why

Desktop Garage supports basic vehicle CRUD but omits the planning and quick-odometer workflows already provided by the shared vehicle presentation layer. Completing these workflows removes a significant platform parity gap without duplicating domain logic or introducing platform credentials.

## What Changes

- Add optional next ITV date, insurance renewal date, and next service odometer fields to Desktop vehicle creation and editing.
- Present shared vehicle-reminder suggestions before saving and require an explicit save-only or save-with-reminders decision.
- Add a focused Desktop quick-odometer action for each vehicle.
- Require explicit confirmation before a quick odometer update decreases the current value.
- Preserve local-first persistence, mutation feedback, and existing Desktop notification-availability messaging.
- Add Desktop integration coverage for planning fields, reminder reconciliation, quick increases, and confirmed decreases.

## Capabilities

### New Capabilities
- `desktop-garage-planning`: Desktop vehicle planning fields, generated-reminder confirmation, and quick odometer updates.

### Modified Capabilities

None.

## Impact

- Affects Desktop Garage Compose UI and Desktop integration tests.
- Reuses `VehicleFormViewModel`, its existing events/effects, and current vehicle/reminder use cases.
- Does not change database schemas, domain APIs, remote synchronization contracts, or native notification support.
