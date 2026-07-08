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
The system SHALL allow the Android user to create a maintenance record for the selected vehicle using the MVP fields supported by the domain.

#### Scenario: Valid maintenance creation
- **WHEN** the user submits a maintenance record with type, picker-selected valid date and non-negative odometer value
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

### Requirement: Local Maintenance Persistence
The system SHALL persist Android MVP maintenance records in local storage through the domain maintenance repository contract.

#### Scenario: Records survive app restart
- **WHEN** a maintenance record is created and the app process is restarted
- **THEN** the vehicle history returns the previously created maintenance record from local storage

### Requirement: Maintenance Record Deletion
The system SHALL allow the Android user to delete a maintenance record from a vehicle history after confirmation.

#### Scenario: Delete maintenance record
- **WHEN** the user confirms deletion of a maintenance record
- **THEN** the record is removed from local persistence and no longer appears in the history
