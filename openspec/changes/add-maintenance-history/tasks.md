## 1. Feature Wiring

- [x] 1.1 Add required dependencies so `feature:maintenance` can use domain, model, testing, lifecycle and Compose UI.
- [x] 1.2 Wire `app:android` to switch between garage and vehicle maintenance history routes.

## 2. Maintenance Data And Presentation

- [x] 2.1 Add an in-memory maintenance repository implementation for the MVP slice.
- [x] 2.2 Add shared maintenance history MVI contracts and ViewModel using domain use cases.
- [x] 2.3 Add tests for empty history, valid creation, validation errors and date-descending ordering.

## 3. Android UI And Navigation

- [x] 3.1 Implement the Android maintenance screen with empty state, form and history list.
- [x] 3.2 Update the garage screen/ViewModel to emit vehicle selection and navigate to history.
- [x] 3.3 Show validation feedback for blank type and invalid odometer values.

## 4. Verification

- [x] 4.1 Run `./gradlew test`.
- [x] 4.2 Run `./gradlew assembleDebug`.
- [x] 4.3 Run `git diff --check` and inspect working tree status.
