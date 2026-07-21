## 1. Model Layer

- [x] 1.1 Extend typed identifiers for user profiles and maintenance types.
- [x] 1.2 Add shared models for family and user profile.
- [x] 1.3 Replace the vehicle placeholder with a typed vehicle model and `VehicleType` enum.
- [x] 1.4 Add shared models for maintenance type and maintenance record.
- [x] 1.5 Add shared reminder model and reminder status fields.

## 2. Domain Contracts

- [x] 2.1 Expand repository contracts for vehicles, maintenance records and reminders.
- [x] 2.2 Add explicit domain result/error types for validation use cases.
- [x] 2.3 Add create vehicle use case contract and implementation.
- [x] 2.4 Add create maintenance record use case contract and implementation.
- [x] 2.5 Add vehicle history query use case.
- [x] 2.6 Add automatic reminder use case for ITV and insurance records.

## 3. Tests

- [x] 3.1 Add fake repositories for common tests.
- [x] 3.2 Add tests for valid vehicle creation.
- [x] 3.3 Add tests for blank vehicle name and negative odometer validation.
- [x] 3.4 Add tests for valid and invalid maintenance record creation.
- [x] 3.5 Add tests for newest-first vehicle history.
- [x] 3.6 Add tests for automatic reminder creation and skip cases.

## 4. Verification

- [x] 4.1 Run `./gradlew test`.
- [x] 4.2 Run `./gradlew assembleDebug`.
- [x] 4.3 Run `git diff --check`.
- [x] 4.4 Confirm OpenSpec tasks are complete and ready to archive.
