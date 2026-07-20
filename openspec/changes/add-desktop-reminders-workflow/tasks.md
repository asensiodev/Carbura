## 1. Desktop Local Mode

- [x] 1.1 Generalize Garage-specific Desktop local-mode wiring into an application-level Koin module
- [x] 1.2 Inject the shared Desktop local family into Garage and Reminders ViewModel resolution
- [x] 1.3 Add the Reminders feature and required Compose runtime dependency to Desktop compilation

## 2. Reminders Workspace

- [x] 2.1 Route the Reminders destination to a Desktop-native workspace with explicit shell navigation callbacks
- [x] 2.2 Implement loading, retry, no-vehicle, empty, filtered-empty, and pending-list states
- [x] 2.3 Implement shared multi-vehicle filtering controls
- [x] 2.4 Implement manual reminder creation with shared validation and persistence feedback
- [x] 2.5 Implement reminder completion and confirmed deletion with mutation progress and feedback
- [x] 2.6 Display accurate local storage and native notification availability information

## 3. Verification

- [x] 3.1 Add Desktop integration tests for no-auth resolution and vehicle/reminder create, filter, complete, and delete behavior
- [x] 3.2 Verify Reminders-to-Garage shell navigation behavior with a testable destination transition
- [x] 3.3 Run Desktop tests, quality checks, and the exact CI verification command
- [x] 3.4 Validate the OpenSpec change strictly and launch the Desktop Reminders workflow on macOS
