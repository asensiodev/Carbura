## 1. Model And Migration TDD

- [x] 1.1 Add failing model, SQLDelight repository, and mapper tests for nullable vehicle ITV date, insurance date, and next service odometer fields.
- [x] 1.2 Extend the shared `Vehicle` model and SQLDelight vehicle schema, queries, mappers, and local migration to pass the local round-trip tests.
- [x] 1.3 Add the versioned Supabase migration with nullable vehicle planning columns and update setup documentation if required.
- [x] 1.4 Add failing sync DTO and manager tests for pushing, pulling, and clearing vehicle planning fields.
- [x] 1.5 Extend sync entities, DTOs, remote/local mappers, and gateways to pass planning-field synchronization tests.

## 2. Proactive Reminder Domain TDD

- [x] 2.1 Add failing tests for deriving ITV, insurance, and service suggestions from populated vehicle targets and no suggestions from empty targets.
- [x] 2.2 Implement typed suggestion kinds and the minimum shared suggestion derivation logic needed to pass the tests.
- [x] 2.3 Add failing tests for deterministic generated reminder IDs, repeated-save deduplication, changed-target updates, and preservation of unrelated reminders.
- [x] 2.4 Add failing tests for clearing generated targets, deleting only owned reminders, and cancelling owned date notifications.
- [x] 2.5 Implement shared confirmed-suggestion reconciliation through vehicle/reminder repositories and the notification scheduler.
- [x] 2.6 Add tests proving date suggestions schedule or reschedule notifications while odometer-only suggestions do not.

## 3. Vehicle Validation And Save TDD

- [x] 3.1 Extend creation and update use-case tests for valid planning fields, negative next service odometer rejection, and cleared optional targets.
- [x] 3.2 Update vehicle creation/edit inputs and validation while preserving existing identity and odometer-decrease behavior.
- [x] 3.3 Add orchestration tests for saving vehicle fields with confirmed suggestions and saving fields without reminders when suggestions are declined.

## 4. Garage Presentation TDD

- [x] 4.1 Add failing Garage ViewModel tests for entering and pre-filling all optional planning fields in create and edit flows.
- [x] 4.2 Add failing Garage ViewModel tests for suggestion preview, explicit confirmation, decline, validation, and successful refresh.
- [x] 4.3 Extend garage MVI state, events, effects, and ViewModel orchestration to pass the planning and confirmation tests.
- [x] 4.4 Refactor shared create/edit form state only where it reduces duplication and keep all presentation tests green.

## 5. Android UI

- [x] 5.1 Add optional ITV and insurance date pickers and next service odometer input to vehicle creation and editing.
- [x] 5.2 Add an accessible reminder suggestion preview that clearly supports confirm and decline before vehicle save completes.
- [x] 5.3 Show validation, loading, success, and scheduling behavior consistently with the existing garage flow.
- [x] 5.4 Expose upcoming generated reminder context to garage presentation state for the later vehicle-card redesign without redesigning the card now.

## 6. Verification

- [x] 6.1 Run focused domain, data, sync, notification, and garage presentation tests.
- [x] 6.2 Run `./gradlew qualityCheck test assembleDebug` and resolve regressions.
- [x] 6.3 Apply and manually verify the Supabase migration in the configured development project.
- [x] 6.4 Manually verify create/edit suggestions, confirmation, decline, deduplication, changed dates, cleared targets, notification rescheduling, offline persistence, and later sync on Android.
