## 1. Domain And Persistence

- [x] 1.1 Add scoped active-record and active-reminder lookup contracts
- [x] 1.2 Add a scoped SQLDelight maintenance UPDATE that cannot insert or resurrect records
- [x] 1.3 Persist record and automatic/planned reminder mutations atomically
- [x] 1.4 Implement typed maintenance update parsing, validation, ownership, and reminder reconciliation
- [x] 1.5 Add domain and Desktop persistence tests for update and reminder convergence

## 2. Shared Presentation

- [x] 2.1 Add shared maintenance edit state, events, mutation type, and updated effect
- [x] 2.2 Populate, cancel, submit, retry, and stale-record edit flows in the shared ViewModel
- [x] 2.3 Register update dependencies in the Maintenance Koin module
- [x] 2.4 Add shared ViewModel and domain tests for successful, invalid, and stale-record updates

## 3. Platform Interfaces

- [x] 3.1 Add accessible Android maintenance edit actions and create/edit form behavior
- [x] 3.2 Add Desktop maintenance edit controls and reuse the shared edit form state
- [x] 3.3 Extend Android and Desktop tests for edit interactions and persistence

## 4. Verification

- [x] 4.1 Run focused domain, data, feature, Android, and Desktop tests
- [x] 4.2 Run quality checks and the exact CI verification command
- [x] 4.3 Validate the OpenSpec change strictly and launch Desktop on macOS
