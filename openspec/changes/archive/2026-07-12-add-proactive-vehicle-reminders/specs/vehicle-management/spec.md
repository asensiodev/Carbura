## ADDED Requirements

### Requirement: Optional Vehicle Due Targets
The system SHALL allow the Android user to provide optional next ITV date, insurance renewal date, and next service odometer values during vehicle creation and editing.

#### Scenario: Save valid optional due targets
- **WHEN** the user saves a valid vehicle with any supported optional due target
- **THEN** the due target is persisted with that vehicle and remains available when editing it again

#### Scenario: Reject negative next service odometer
- **WHEN** the user enters a negative next service odometer
- **THEN** the system shows a validation error and does not save the vehicle changes

#### Scenario: Clear optional due target
- **WHEN** the user removes an optional due target and saves the vehicle
- **THEN** the vehicle stores that target as empty
