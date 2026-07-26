## ADDED Requirements

### Requirement: Desktop date selection remains runtime-compatible
The Desktop app SHALL provide an in-app date selector that opens and selects canonical dates with the resolved production dependency graph.

#### Scenario: User opens a vehicle date selector
- **WHEN** the user selects a date field while creating or editing a vehicle
- **THEN** a localized calendar opens without a runtime linkage error and the confirmed day is stored as an ISO date
