## 1. MVI Contracts

- [x] 1.1 Split garage `UiState`, `Event` and `Effect` into dedicated files.
- [x] 1.2 Replace `GarageIntent` naming with `GarageEvent`.

## 2. ViewModel Refactor

- [x] 2.1 Refactor `GarageViewModel` to expose `StateFlow<GarageUiState>` and one-off `Flow<GarageEffect>`.
- [x] 2.2 Route all UI input through `onEvent(event)`.
- [x] 2.3 Emit one-off effects for successful creation and validation feedback.

## 3. UI And Tests

- [x] 3.1 Update Android Compose screen to dispatch events and collect effects.
- [x] 3.2 Update tests to verify state and effects with Turbine.

## 4. Dispatchers

- [x] 4.1 Add shared `DispatcherProvider` contract.
- [x] 4.2 Add production and test dispatcher providers.
- [x] 4.3 Inject dispatchers into shared ViewModels.

## 5. Verification

- [x] 5.1 Run `./gradlew test`.
- [x] 5.2 Run `./gradlew assembleDebug`.
- [x] 5.3 Run `git diff --check` and inspect worktree status.
