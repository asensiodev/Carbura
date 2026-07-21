## 1. Reminder Policy TDD

- [x] 1.1 Add failing tests for deterministic maintenance reminder and alert identities.
- [x] 1.2 Add failing tests for ITV 60/30/7 and insurance 45/37/7 alert policies.
- [x] 1.3 Add typed maintenance alert kinds and shared fixed policy derivation.
- [x] 1.4 Make maintenance reminder creation deterministic and expose one logical reminder with its alert plan.

## 2. Multi-Alert Android Scheduling

- [x] 2.1 Add scheduler contract tests for manual single-alert and generated multi-alert plans.
- [x] 2.2 Extend the notification scheduler contract to schedule and cancel all alert instances for one logical reminder.
- [x] 2.3 Implement unique Android alarm identities per reminder and alert kind, skipping past alert instants.
- [x] 2.4 Add localized ITV and insurance notification copy with qualified insurance notice guidance.
- [x] 2.5 Verify completing or directly deleting a reminder cancels every scheduled alert instance.

## 3. Maintenance Domain Orchestration

- [x] 3.1 Add failing creation-input tests for canonical maintenance types and optional next due dates.
- [x] 3.2 Extend maintenance creation input and validation with canonical type code, custom label, and optional next due date.
- [x] 3.3 Add a shared use case that persists maintenance, creates the optional reminder, and schedules its alert plan.
- [x] 3.4 Retain deterministic maintenance identity across retry after partial failure.
- [x] 3.5 Add deletion tests proving source cleanup affects only its deterministic generated reminder and alerts.
- [x] 3.6 Extend maintenance deletion orchestration to tombstone the generated reminder and cancel its alerts.

## 4. Maintenance Presentation TDD

- [x] 4.1 Add ViewModel tests for canonical type selection, conditional next due date, reminder-created feedback, form reset, retry, and duplicate submission.
- [x] 4.2 Extend maintenance MVI state and events with canonical type, custom label, next due date, and reminder creation result.
- [x] 4.3 Wire maintenance creation and deletion orchestration through Koin.
- [x] 4.4 Add accessible canonical maintenance type selection and conditional ITV/insurance date pickers to Android.
- [x] 4.5 Add localized success, validation, type, date-field, and explanatory copy.
- [x] 4.6 Keep the long maintenance form scrollable, IME-safe, and operable at large text.

## 5. Reminder And Data Verification

- [x] 5.1 Add repository round-trip tests for canonical ITV/insurance records with `nextDueDate`.
- [x] 5.2 Add tests proving generated reminders remain one pending card despite multiple alert instances.
- [x] 5.3 Verify generated record and reminder pending-sync state and existing DTO round trips.
- [x] 5.4 Confirm manual and vehicle-planning reminder creation, completion, deletion, and scheduling remain unchanged.

## 6. End-To-End And Final Verification

- [x] 6.1 Add an app-module deterministic journey seam with clean repositories and no production authentication switches.
- [x] 6.2 Add a journey test that creates a vehicle, records ITV with next date, and observes one generated reminder.
- [x] 6.3 Run focused domain, data, maintenance, reminders, scheduler, and journey tests.
- [x] 6.4 Run `./gradlew qualityCheck test assembleDebug` and resolve all regressions.
- [x] 6.5 Install on Android and verify ITV and insurance creation, alert scheduling, generated reminder display, and source deletion cleanup.
- [x] 6.6 Confirm no database migration, recurrence, maintenance editing, Desktop notification, or family collaboration behavior entered scope.
