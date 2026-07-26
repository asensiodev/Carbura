## Context

The Desktop application has persistent Garage and Reminders workspaces under one injected local family. Maintenance presentation, validation, record/reminder orchestration, and cancellation behavior already live in shared code, while the existing Compose screen is Android-only. The shared Maintenance ViewModel is intentionally scoped to one `VehicleId` and `FamilyId`, so Desktop must keep vehicle selection and navigation outside that detail state machine.

Desktop data bindings already provide local vehicle, maintenance, and reminder repositories plus no-op native notification scheduling. Record creation can therefore preserve atomic generated/planned reminder state, but the UI must not imply that macOS or Windows notifications will be delivered.

## Goals / Non-Goals

**Goals:**

- Provide vehicle selection and persistent per-vehicle maintenance history on Desktop.
- Support complete maintenance record creation through shared validation and orchestration.
- Preserve automatic ITV/insurance next-due reminders and future-maintenance reminder choices.
- Support confirmed deletion with generated/planned reminder cleanup.
- Navigate from a Garage vehicle directly to its Maintenance workspace through explicit shell state.
- Preserve Android behavior and shared architecture boundaries.

**Non-Goals:**

- Editing maintenance records, search, export, or completed-reminder history.
- Native Desktop notification delivery or background scheduling.
- Desktop authentication, remote synchronization, or multi-family switching.
- Copying Android UI or duplicating parsing and validation in Desktop Compose.
- Redesigning shared cost/odometer parsing or custom-type persistence as a platform-specific fix.

## Decisions

### Keep vehicle selection outside the history ViewModel

The workspace will use `GarageOverviewViewModel` for family-scoped vehicle loading and create a `MaintenanceHistoryViewModel` keyed by the selected vehicle. Selecting another vehicle replaces the detail ViewModel instance and dispatches `Started` for that vehicle.

Adding selection events to `MaintenanceHistoryViewModel` was rejected because it would mix workspace navigation with per-vehicle mutation retry identity and history state. Direct repository access from Compose was rejected because it bypasses the shared presentation boundary.

### Carry selected vehicle through Desktop shell state

The shell will retain an optional maintenance `VehicleId`. A Garage `NavigateToVehicleHistory` effect will set that ID and select Maintenance. Direct navigation to Maintenance can reuse the retained ID or allow the workspace to select the first available vehicle.

This keeps navigation explicit and testable without introducing Android Navigation or a global mutable singleton.

### Reuse shared maintenance orchestration unchanged

The Desktop form will dispatch existing field and submit events. It will not parse costs, dates, or odometers itself. The shared ViewModel and use cases remain responsible for validation, stable retry IDs, automatic next-due reminder generation, future-date offers, persistence, and deletion.

Platform-specific validation workarounds were rejected because they would cause Android and Desktop behavior to diverge. Broader validation hardening can be proposed as a separate shared-domain change.

### Present future reminder choices accurately

When shared state exposes a future-maintenance offer, Desktop will allow saving with a stored reminder, saving only the maintenance record, or dismissing without saving. Copy will explicitly say native Desktop alerts are unavailable.

### Use Desktop-native presentation only

History cards, vehicle controls, forms, and confirmation dialogs will live in `app:desktop`. Shared state, domain behavior, and persistence stay in feature/core modules. This follows the established Garage and Reminders boundary without forcing Android resources and system APIs into common code.

## Risks / Trade-offs

- [Switching vehicles recreates a ViewModel] -> Key the instance by vehicle ID and rely on shared load/mutation state isolation.
- [Unsubmitted form state is lost when switching vehicles] -> Treat vehicle selection as an explicit context change and disable it during active mutations.
- [Generated reminders do not produce native Desktop alerts] -> Describe them as stored reminders and retain durable outbox intent for future platform scheduling.
- [Current shared validation has coarse malformed-number messages] -> Reuse it consistently rather than introducing Desktop-only rules; track hardening separately.
- [Snapshot vehicle loading is not reactive] -> Load on workspace entry and use destination recreation after Garage changes.
