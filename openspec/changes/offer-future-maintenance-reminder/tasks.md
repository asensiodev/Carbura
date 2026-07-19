## 1. Domain Reminder Lifecycle

- [x] 1.1 Add tests for deterministic planned-maintenance reminder identity, day-of scheduling, and idempotent retry.
- [x] 1.2 Implement planned-maintenance reminder creation from the normalized saved maintenance record.
- [x] 1.3 Extend maintenance deletion tests and implementation to cancel and tombstone both planned and next-due reminder identities.
- [x] 1.4 Reconcile a partially persisted planned reminder when a retry ends with an explicit save-only choice.

## 2. Shared Presentation

- [x] 2.1 Add ViewModel tests proving valid future dates pause without mutation while today, past, and invalid dates retain existing submission behavior.
- [x] 2.2 Add state and events for offer, dismissal, save-only, and save-with-reminder actions.
- [x] 2.3 Inject planned-reminder creation and preserve stable IDs, cancellation propagation, retry behavior, form reset, feedback, and sync.
- [x] 2.4 Add tests for accepted, declined, dismissed, duplicated, failed, and cancelled planned-reminder operations.

## 3. Android Experience

- [x] 3.1 Add localized future-maintenance reminder dialog and success copy.
- [x] 3.2 Add Compose tests for visibility, confirm, save-only, large text, and accessible actions; cover dismissal in shared presentation tests.
- [x] 3.3 Verify the existing vehicle card redesign remains intact and Garage regressions stay green.

## 4. Verification

- [x] 4.1 Run affected common, Desktop, and Android unit tests.
- [x] 4.2 Run affected Android instrumented tests on a connected device.
- [x] 4.3 Run `qualityCheck`, `:app:android:assembleDebug`, OpenSpec strict validation, and `git diff --check`.
- [ ] 4.4 Manually verify save-only and save-with-reminder behavior when a runnable authenticated environment is available.
