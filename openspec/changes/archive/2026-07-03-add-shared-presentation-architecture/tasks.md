## 1. Dependencies

- [x] 1.1 Add AndroidX Lifecycle ViewModel KMP dependencies.
- [x] 1.2 Add Koin core and Compose ViewModel dependencies.
- [x] 1.3 Add Navigation 3 runtime and UI dependencies.

## 2. Shared DI And Navigation

- [x] 2.1 Add shared Koin module for the garage dependencies.
- [x] 2.2 Add platform initialization for Android.
- [x] 2.3 Add typed Navigation 3 route definitions.

## 3. Garage Refactor

- [x] 3.1 Replace `GarageController` with shared `GarageViewModel`.
- [x] 3.2 Model garage changes with intents and immutable state.
- [x] 3.3 Update Android Compose screen to consume the shared ViewModel through Koin.
- [x] 3.4 Update tests to exercise the shared ViewModel.

## 4. Verification

- [x] 4.1 Run `./gradlew test`.
- [x] 4.2 Run `./gradlew assembleDebug`.
- [x] 4.3 Run `git diff --check` and inspect working tree status.
