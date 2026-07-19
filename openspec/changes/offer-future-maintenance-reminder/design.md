## Context

`MaintenanceHistoryViewModel` currently sends every valid form directly through `CreateMaintenanceWithReminderFromInputUseCase`. A future `performedOn` date is accepted, but only ITV and insurance can generate reminders, and those reminders derive from the separate `nextDueDate` field without explicit consent.

The new behavior crosses shared presentation, domain reminder orchestration, Android UI, local persistence, notification scheduling, and source deletion. It must preserve the existing canonical next-due reminder behavior and the cancellation guarantees established by `harden-coroutine-cancellation`.

## Goals / Non-Goals

**Goals:**

- Pause submission when a valid `performedOn` is later than the current local date.
- Let the user save with or without one planned-maintenance reminder.
- Support every maintenance type with an idempotent reminder tied to the source record.
- Schedule the reminder for the maintenance date and clean it up with the source record.
- Keep the confirmation accessible, state-backed, cancellation-safe, and retryable.

**Non-Goals:**

- Prevent future-dated maintenance records.
- Reinterpret or replace `nextDueDate` reminders for ITV and insurance.
- Add maintenance editing, configurable notification offsets, new persistence columns, or remote schema changes.
- Make database persistence and Android alarm scheduling transactional; durable side-effect convergence remains owned by `make-side-effects-cancellation-convergent`.

## Decisions

### Ask before persistence

`SubmitMaintenance` will compare a syntactically valid `performedOn` with `LocalDateProvider.currentDate()`. A future date exposes state for an Android confirmation dialog instead of starting a mutation. Confirm and decline events then execute the same stable-ID maintenance creation path with an explicit reminder policy.

This is preferred over prompting after persistence because both visible actions can accurately describe the resulting mutation. Dialog dismissal does not save anything, avoiding accidental persistence through back or outside-tap dismissal.

### Keep unresolved consent in UiState

The reminder offer will be represented by `showFutureReminderOffer` in `MaintenanceHistoryUiState`, not a one-shot effect. The form remains the source of the pending values until the user chooses an action.

This survives recomposition and does not lose an actionable decision when effect collection restarts. The flag is cleared before mutation and can be offered again through an ordinary retry after a persistence failure.

### Give planned reminders a separate deterministic identity

The domain will derive `planned-maintenance-reminder:<maintenance-record-id>` from the saved record. This is distinct from the existing `maintenance-reminder:<maintenance-record-id>` identity used for ITV/insurance `nextDueDate` reminders.

Separate identities allow both intentions to coexist and make repeated confirmation idempotent through repository upsert and alarm replacement. Deleting the source record cancels and tombstones both possible reminder identities.

Every retry that does not create a planned reminder also reconciles any reminder that may have been persisted by an earlier failed save-with-reminder attempt. This covers both an explicit save-only choice and a direct retry after the date is changed to today or the past. The reconciliation first checks for the deterministic reminder locally, then cancels and tombstones it when present, so ordinary saves do not create needless tombstones.

### Use the saved normalized record

The ViewModel first invokes the existing maintenance orchestration. When consent was granted, it passes `MaintenanceCreationResult.record` to a focused `CreatePlannedMaintenanceReminderUseCase`. The reminder uses the saved family, vehicle, type, ID, and `performedOn` value rather than reconstructing them from UI strings.

The reminder is a normal pending `Reminder` titled `Mantenimiento programado`, with `dueDate = performedOn` and a day-of notification (`notifyDaysBefore = 0`). Reusing the normal reminder repository and scheduler makes it visible in the Reminders screen and compatible with sync.

### Preserve explicit cancellation handling

Cancellation is rethrown before failure state is produced. Transient mutation state is cleared in non-suspending `finally` logic. A non-cancellation failure does not claim success and leaves the stable pending maintenance ID available for retry.

## Risks / Trade-offs

- [A future record appears in a history of completed work] -> Preserve the user's requested workflow for now and make the confirmation explicitly describe it as future maintenance; a future product change can split planned and completed maintenance.
- [ITV or insurance can produce two reminders] -> Use distinct source identities and explain that `performedOn` and `nextDueDate` represent separate targets.
- [Maintenance persistence can succeed before reminder scheduling fails] -> Reuse a stable record ID and deterministic reminder ID so retry converges; durable atomicity is deferred to the side-effect outbox change.
- [A day-of notification may be too late for some users] -> Keep this feature minimal and predictable; configurable lead time remains outside this change.
- [Generic reminder copy omits the maintenance subtype] -> Prefer stable localized copy now because custom type labels are not retained directly on `MaintenanceRecord`; richer source metadata would require a data-model change.

## Migration Plan

1. Add and test the deterministic planned-reminder domain operation and cleanup.
2. Add state and events for future-date consent while preserving existing form behavior.
3. Add the Android dialog and localized feedback.
4. Run common, Desktop, Android unit, instrumented, quality, and build verification.
5. No data migration is required; existing records and reminders remain unchanged.

Rollback removes the new UI and planned-reminder creation path. Any already-created planned reminders remain normal reminders that users can complete or delete.

## Open Questions

None. The user selected the primary maintenance date as the trigger and requested an explicit choice.
