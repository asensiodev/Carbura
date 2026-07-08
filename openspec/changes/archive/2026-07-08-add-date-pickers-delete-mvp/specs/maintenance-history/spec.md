## MODIFIED Requirements

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

### Requirement: Maintenance Record Deletion
The system SHALL allow the Android user to delete a maintenance record from a vehicle history after confirmation.

#### Scenario: Delete maintenance record
- **WHEN** the user confirms deletion of a maintenance record
- **THEN** the record is removed from local persistence and no longer appears in the history
