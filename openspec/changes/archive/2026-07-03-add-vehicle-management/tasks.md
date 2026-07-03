## 1. Feature Wiring

- [x] 1.1 Add required dependencies so `feature:garage` can use domain, model and Compose UI.
- [x] 1.2 Wire `app:android` to render the garage feature screen.

## 2. Garage State And Repository

- [x] 2.1 Add an in-memory vehicle repository implementation for the MVP slice.
- [x] 2.2 Add garage screen state and intent handling that uses `CreateVehicleUseCase`.
- [x] 2.3 Add tests for empty state, valid creation and validation errors.

## 3. Android UI

- [x] 3.1 Implement the Android garage screen with empty state, form and list.
- [x] 3.2 Show validation feedback for blank names and invalid odometer values.

## 4. Verification

- [x] 4.1 Run `./gradlew test`.
- [x] 4.2 Run `./gradlew assembleDebug`.
- [x] 4.3 Run `git diff --check` and inspect working tree status.
