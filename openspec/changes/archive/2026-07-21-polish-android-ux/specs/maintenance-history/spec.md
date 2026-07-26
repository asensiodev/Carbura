## ADDED Requirements

### Requirement: Selected Vehicle Context
The maintenance destination SHALL identify the selected vehicle and show a compact summary of persisted vehicle information before its history.

#### Scenario: Open vehicle destination
- **WHEN** the user opens a vehicle from Garage
- **THEN** the destination shows the vehicle name and maintenance history without requiring the user to infer context from navigation

### Requirement: Current Maintenance Date Default
The maintenance form SHALL initialize a new record with the device's current local calendar date through a testable date provider.

#### Scenario: Open maintenance creation
- **WHEN** the user opens the new-maintenance form
- **THEN** the performed date defaults to the current local date rather than a fixed build-time value

### Requirement: Recoverable Maintenance Presentation
Maintenance history SHALL provide recoverable load failure, retry, mutation progress, field-associated validation, and layouts that preserve long type names and actions.

#### Scenario: History load fails
- **WHEN** local maintenance history cannot be loaded
- **THEN** the destination keeps vehicle context visible and offers Retry

#### Scenario: Maintenance record has long content
- **WHEN** a record has a long custom type or the user enables large text
- **THEN** its type, date, cost, and actions remain readable without overlap
