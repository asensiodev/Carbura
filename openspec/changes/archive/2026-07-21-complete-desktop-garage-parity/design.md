## Context

Desktop Garage currently renders a platform-native Compose workspace over the shared `GarageOverviewViewModel` and `VehicleFormViewModel`. The shared form state already models planning targets, reminder suggestions, save confirmation, quick odometer updates, and odometer-decrease confirmation, but the Desktop workspace exposes only the basic CRUD subset.

## Goals / Non-Goals

**Goals:**
- Expose the existing shared planning and quick-odometer workflows through Desktop-native controls.
- Keep all validation, persistence, reminder reconciliation, and synchronization decisions in shared ViewModels and use cases.
- Make every confirmation and mutation state explicit and keyboard-accessible on Desktop.
- Preserve the existing local-only Desktop notification limitation and communicate it accurately.

**Non-Goals:**
- Add new vehicle fields or change persistence schemas.
- Implement native Desktop reminder notifications.
- Add Desktop authentication or remote synchronization.
- Reuse Android composables in the Desktop application.

## Decisions

### Keep Desktop as a thin renderer over shared MVI

`GarageWorkspace` will dispatch the existing `VehicleFormEvent` variants and render `VehicleFormUiState` rather than calling repositories directly. This avoids divergent validation and reminder behavior. Creating Desktop-specific use cases was rejected because the shared state machine already contains the required contracts.

### Integrate planning fields into create and edit forms

The optional ITV date, insurance renewal date, and service odometer controls will live with the existing create/edit vehicle forms. A separate planning screen was rejected because it would duplicate form state and make atomic vehicle-plus-reminder confirmation harder to understand.

### Render shared confirmation state as modal dialogs

Reminder suggestions and odometer-decrease confirmation will use Desktop Material dialogs. Reminder confirmation remains visible until the user explicitly chooses save-only or save-with-reminders, matching the shared state machine. Odometer confirmation dismissal dispatches the existing cancellation event.

### Use a focused quick-odometer dialog per vehicle

Each vehicle card will expose an update-odometer action that opens the shared quick-update state. The dialog displays the current value, validates through the ViewModel, and remains independent from unrelated edit fields.

### Treat success effects and idle state as separate synchronization points in tests

Integration tests will wait for both the expected success effect and the corresponding idle mutation state before chaining another operation. Shared effects can be emitted immediately before a `finally` block clears mutation state, so relying only on an effect creates scheduler-dependent tests.

## Risks / Trade-offs

- [Risk] Adding fields may make the existing Desktop form visually dense. → Group basic details and planning fields with clear labels and responsive rows.
- [Risk] Desktop can create date reminders but cannot schedule native alerts. → Preserve the existing notification-availability message and avoid promising system notifications.
- [Risk] Dialog state can drift from shared form state. → Do not maintain duplicate confirmation booleans; render dialogs directly from `VehicleFormUiState`.
- [Risk] Chained integration mutations can race with ViewModel cleanup. → Await idle mutation state after success effects.
