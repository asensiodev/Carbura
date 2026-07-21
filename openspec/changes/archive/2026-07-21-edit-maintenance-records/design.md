## Context

Maintenance creation currently persists through `INSERT OR REPLACE`, while deletion creates tombstones and deterministic automatic/planned reminders. Reusing creation for editing could insert missing records, resurrect tombstones, change ownership, or leave reminder and notification state inconsistent. Android and Desktop both consume the same shared Maintenance ViewModel, making the edit contract a shared domain concern.

## Goals / Non-Goals

**Goals:**

- Update only active records matching the expected ID, family, and vehicle.
- Preserve record identity, ownership, and currency.
- Reuse creation parsing and normalization for editable fields.
- Reconcile automatic and existing planned reminders atomically with the record update.
- Provide consistent edit behavior on Android and Desktop.
- Preserve cancellation and retry convergence.

**Non-Goals:**

- Moving records between vehicles or families.
- Editing currency, adding odometer-decrease rules, or redesigning maintenance types.
- Creating a planned reminder during an unrelated edit when none existed previously.
- Editing deleted records or resurrecting completed/deleted planned reminders.

## Decisions

### Use a scoped SQL UPDATE and typed result

The repository will expose an update operation constrained by record ID, expected family, expected vehicle, and `deletedAt IS NULL`. It returns whether an active row was updated. `UpdateMaintenanceRecordResult` distinguishes success, validation failure, and not-found without revealing cross-family existence.

Unrestricted upsert was rejected because it cannot protect tombstones or ownership.

### Build updates from the authoritative existing record

The use case loads the active record through the same scope, parses editable strings with the existing creation parser, and copies normalized editable fields onto the authoritative record. ID, family, vehicle, and currency are never accepted as replacement values from UI state.

### Reconcile deterministic reminders in one transaction

The automatic reminder is derived from the updated type and next-due date. An active planned reminder is preserved and updated only while the performed date remains future; otherwise it is deleted. A missing planned reminder is not silently created. The record and all reminder/outbox mutations are committed atomically.

### Keep edit mode in shared presentation

The shared ViewModel will populate the existing form from a selected record, expose edit mode, submit through the update use case, retain values on validation/persistence failure, and emit `MaintenanceUpdated` on success. Android and Desktop only render edit controls and form labels.

## Risks / Trade-offs

- [Custom labels are reconstructed from the persisted type ID] -> Preserve current storage behavior and defer a custom-label schema redesign.
- [A record can disappear during editing] -> Scoped update returns not-found; the ViewModel exits edit mode and reloads history.
- [Recovery cancellation can occur after commit] -> Stable IDs and desired-state revisions keep retries convergent.
- [Existing planned reminder creation is not fully atomic in the create flow] -> Make editing atomic now and leave create-flow consolidation to a focused follow-up.
