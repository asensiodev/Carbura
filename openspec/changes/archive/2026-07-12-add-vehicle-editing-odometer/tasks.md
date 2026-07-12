## 1. Domain TDD

- [x] 1.1 Add failing shared domain tests for valid vehicle edits, identity preservation, blank names, and negative odometers.
- [x] 1.2 Add failing shared domain tests for normal odometer increases and the confirmation-required, confirmed, and cancelled decrease paths.
- [x] 1.3 Implement the minimum shared update input, result, and `UpdateVehicleUseCase` behavior needed to pass the domain tests.
- [x] 1.4 Refactor creation and update validation only where sharing reduces duplication, then keep all domain tests green.

## 2. Local-First Data TDD

- [x] 2.1 Add a failing repository test proving that saving an existing vehicle updates its editable values without changing its identity.
- [x] 2.2 Add a failing local sync test proving that an edited vehicle is immediately readable and marked pending with a refreshed update timestamp.
- [x] 2.3 Make the minimum repository or SQLDelight changes needed to pass the update and sync metadata tests.

## 3. Presentation TDD

- [x] 3.1 Add failing Garage ViewModel tests for opening and pre-filling vehicle editing, validation errors, successful save, and error recovery.
- [x] 3.2 Add failing Garage ViewModel tests for quick odometer updates and lower-value confirmation, confirmation, and cancellation.
- [x] 3.3 Extend the garage MVI state, intents, effects, and ViewModel dependencies with the minimum behavior needed to pass the tests.
- [x] 3.4 Refactor reusable vehicle form state and update orchestration while keeping presentation tests green.

## 4. Android UI

- [x] 4.1 Reuse or minimally adapt the vehicle form to render pre-filled edit state, validation, saving, and errors.
- [x] 4.2 Add discoverable Android actions for editing a vehicle and quickly updating its odometer.
- [x] 4.3 Add the odometer decrease confirmation UI showing both current and proposed values and supporting confirm or cancel.
- [x] 4.4 Wire the update use case through dependency injection and ensure successful edits refresh the visible garage state.

## 5. Verification

- [x] 5.1 Run focused domain, data, and garage presentation tests and confirm they pass.
- [x] 5.2 Run `./gradlew qualityCheck test assembleDebug` and resolve any regressions.
- [x] 5.3 Manually verify full editing, quick odometer increase, confirmed decrease, cancelled decrease, validation, and offline persistence on Android.
- [x] 5.4 Confirm a later sync sends the edited vehicle and clears its pending state without changing vehicle identity.
