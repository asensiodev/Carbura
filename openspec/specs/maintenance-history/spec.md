## Purpose

Define the Android MVP workflow for creating, viewing, persisting, and deleting maintenance records for a selected vehicle.
## Requirements
### Requirement: Vehicle Maintenance History
The system SHALL show the maintenance records for a selected vehicle ordered by service date descending.

#### Scenario: Empty maintenance history
- **WHEN** the selected vehicle has no maintenance records
- **THEN** the Android maintenance screen shows an empty history state

#### Scenario: Ordered maintenance history
- **WHEN** the selected vehicle has multiple maintenance records
- **THEN** the Android maintenance screen shows them ordered by date descending

### Requirement: Maintenance Record Creation Form
The system SHALL allow the Android user to create a maintenance record for the selected vehicle using a canonical maintenance type, picker-selected performed date, optional next due date for ITV or insurance, and the remaining MVP fields supported by the domain. A Custom type SHALL retain user-entered maintenance labels.

#### Scenario: Valid canonical maintenance creation
- **WHEN** the user submits a canonical maintenance record with picker-selected valid date and non-negative odometer value
- **THEN** the canonical maintenance type code is saved through the domain maintenance creation use case and the record appears in the history

#### Scenario: ITV next due date
- **WHEN** the user selects ITV and supplies a picker-selected next ITV date
- **THEN** the date is stored as `nextDueDate` and maintenance-generated reminder orchestration runs

#### Scenario: Insurance next due date
- **WHEN** the user selects insurance and supplies a picker-selected renewal date
- **THEN** the date is stored as `nextDueDate` and maintenance-generated reminder orchestration runs

#### Scenario: Blank custom maintenance type error
- **WHEN** the user selects Custom and submits without a custom type label
- **THEN** the screen shows a validation error and the record is not added to the history

#### Scenario: Invalid maintenance odometer error
- **WHEN** the user submits a maintenance record with a negative odometer value
- **THEN** the screen shows a validation error and the record is not added to the history

#### Scenario: Optional cost display
- **WHEN** the user submits a valid maintenance record with cost
- **THEN** the saved record shows the cost in the history list

#### Scenario: Successful automatic reminder feedback
- **WHEN** maintenance creation also creates a generated reminder
- **THEN** Android confirms that both maintenance and its reminder were created

### Requirement: Local Maintenance Persistence
The system SHALL persist Android MVP maintenance records in local storage through the domain maintenance repository contract.

#### Scenario: Records survive app restart
- **WHEN** a maintenance record is created and the app process is restarted
- **THEN** the vehicle history returns the previously created maintenance record from local storage

### Requirement: Maintenance Record Deletion
The system SHALL allow the Android user to delete a maintenance record from a vehicle history after confirmation.

#### Scenario: Delete maintenance record
- **WHEN** the user confirms deletion of a maintenance record
- **THEN** the record is retained as a tombstone pending synchronization and no longer appears in the active history

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

### Requirement: Maintenance Reminder Source Cleanup
Deleting a maintenance record SHALL also delete only the deterministic reminder generated from that source record and cancel its local alerts.

#### Scenario: Delete maintenance with generated reminder
- **WHEN** the user confirms deletion of an ITV or insurance record that generated a reminder
- **THEN** the record and generated reminder are retained as synchronization tombstones, their active UI entries disappear, and the generated reminder alerts are canceled
