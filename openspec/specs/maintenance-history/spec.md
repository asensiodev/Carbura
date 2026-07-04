## ADDED Requirements

### Requirement: Vehicle Maintenance History
The system SHALL show the maintenance records for a selected vehicle ordered by service date descending.

#### Scenario: Empty maintenance history
- **WHEN** the selected vehicle has no maintenance records
- **THEN** the Android maintenance screen shows an empty history state

#### Scenario: Ordered maintenance history
- **WHEN** the selected vehicle has multiple maintenance records
- **THEN** the Android maintenance screen shows them ordered by date descending

### Requirement: Maintenance Record Creation Form
The system SHALL allow the Android user to create a maintenance record for the selected vehicle using the MVP fields supported by the domain.

#### Scenario: Valid maintenance creation
- **WHEN** the user submits a maintenance record with type, valid date and non-negative odometer value
- **THEN** the record is saved through the domain maintenance creation use case and appears in the history

#### Scenario: Blank maintenance type error
- **WHEN** the user submits a maintenance record without a type
- **THEN** the screen shows a validation error and the record is not added to the history

#### Scenario: Invalid maintenance odometer error
- **WHEN** the user submits a maintenance record with a negative odometer value
- **THEN** the screen shows a validation error and the record is not added to the history

#### Scenario: Optional cost display
- **WHEN** the user submits a valid maintenance record with cost
- **THEN** the saved record shows the cost in the history list

### Requirement: Temporary In-Memory Maintenance Storage
The system SHALL keep maintenance records in memory for the initial Android MVP slice until persistent storage is introduced.

#### Scenario: Records remain available during the current process
- **WHEN** a maintenance record is created successfully during the current app process
- **THEN** subsequent history reads for the same vehicle return that record
