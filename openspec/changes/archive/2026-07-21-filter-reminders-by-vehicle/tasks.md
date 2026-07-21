## 1. Shared Presentation

- [x] 1.1 Add selected vehicle filter IDs and a derived visible reminder list to shared UI state.
- [x] 1.2 Add dedicated vehicle filter toggle and clear events to the reminders ViewModel.
- [x] 1.3 Reconcile selected filters when available vehicles reload.
- [x] 1.4 Add shared tests for default, single, multiple, clear, and stale-selection behavior.

## 2. Android UI

- [x] 2.1 Add rounded Material 3 vehicle filter chips in a horizontally scrolling row.
- [x] 2.2 Style selection without checkmark icons and preserve accessible selected semantics.
- [x] 2.3 Render filtered reminders and a filter-specific empty state.
- [x] 2.4 Add Compose tests for selection, multi-selection, clearing, horizontal overflow, accessibility, and empty results.

## 3. Verification

- [x] 3.1 Run affected common and Android unit tests.
- [x] 3.2 Run affected Android instrumented tests on an available emulator or device.
- [x] 3.3 Run `qualityCheck`, `:app:android:assembleDebug`, strict OpenSpec validation, and `git diff --check`.
