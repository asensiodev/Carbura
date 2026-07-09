## 1. Reminder Domain And Persistence

- [x] 1.1 Add reminder repository operations for pending reminder reads and completion.
- [x] 1.2 Add reminder creation/completion use cases with MVP validation rules.
- [x] 1.3 Add SQLDelight `reminders` table and queries for insert/update, pending list, and completion.
- [x] 1.4 Implement `LocalReminderRepository` in `core:data` with model/database mapping.
- [x] 1.5 Register reminder repository and use cases in DI.
- [x] 1.6 Add domain/data tests for reminder validation, persistence, ordering, and completion.

## 2. Reminders Feature UI

- [x] 2.1 Add shared MVI files: `RemindersUiState`, `RemindersEvent`, `RemindersEffect`, and `RemindersViewModel`.
- [x] 2.2 Load pending reminders and available vehicles for the active family.
- [x] 2.3 Implement reminder creation flow with title, vehicle, due date, and due odometer fields.
- [x] 2.4 Implement mark-completed flow and remove completed reminders from pending list.
- [x] 2.5 Add Android `RemindersScreen` with empty state, list state, create form, validation messages, and completion action.
- [x] 2.6 Add reminders navigation entry point from the Android app.
- [x] 2.7 Add Turbine/ViewModel tests for load, create, validation failures, and completion effect/state updates.

## 3. Edge-To-Edge Polish

- [x] 3.1 Enable Android edge-to-edge rendering in the activity entry point.
- [x] 3.2 Apply safe system-bar insets to onboarding, garage, maintenance history, and reminders screens.
- [x] 3.3 Verify added spacing uses design system tokens rather than raw feature-screen `dp` values.
- [x] 3.4 Smoke test login, vehicle creation, maintenance creation, and reminders on Android after inset changes.

## 4. Verification And Documentation

- [x] 4.1 Run `./gradlew test assembleDebug`.
- [x] 4.2 Update docs or delivery notes with reminder MVP scope and deferred items.
- [x] 4.3 Confirm OpenSpec requirements are satisfied before archiving.
