## ADDED Requirements

### Requirement: Vehicle Editing
The system SHALL allow the Android user to edit an existing vehicle's name, type, optional license plate, and current odometer while preserving its identity and active family.

#### Scenario: Save valid vehicle edits
- **WHEN** the user submits valid changes for an existing vehicle with a non-blank name and non-negative odometer
- **THEN** the system persists the edited values for the same vehicle and shows the updated vehicle in the garage

#### Scenario: Reject invalid edited name
- **WHEN** the user submits vehicle edits with a blank name
- **THEN** the system shows a validation error and does not persist the edits

#### Scenario: Reject negative edited odometer
- **WHEN** the user submits vehicle edits with a negative odometer
- **THEN** the system shows a validation error and does not persist the edits

### Requirement: Quick Odometer Update
The system SHALL provide an Android action to update an existing vehicle's current odometer without requiring the user to edit unrelated fields.

#### Scenario: Increase odometer
- **WHEN** the user submits an odometer value greater than or equal to the vehicle's current value
- **THEN** the system persists the new value and shows it for that vehicle

#### Scenario: Invalid quick odometer value
- **WHEN** the user submits a blank, non-numeric, or negative odometer value
- **THEN** the system shows a validation error and keeps the current odometer unchanged

### Requirement: Odometer Decrease Confirmation
The system MUST require explicit user confirmation before persisting an odometer value lower than the vehicle's current value.

#### Scenario: Request confirmation for lower odometer
- **WHEN** the user submits an odometer value lower than the vehicle's current value without confirming the decrease
- **THEN** the system keeps the current value unchanged and asks the user to confirm the old and proposed values

#### Scenario: Confirm lower odometer
- **WHEN** the user explicitly confirms the requested odometer decrease
- **THEN** the system persists the lower value for the vehicle

#### Scenario: Cancel lower odometer
- **WHEN** the user cancels the odometer decrease confirmation
- **THEN** the system keeps the current odometer unchanged

### Requirement: Local-First Vehicle Updates
The system SHALL make successful vehicle edits available from local storage immediately and mark them for synchronization.

#### Scenario: Edit vehicle while offline
- **WHEN** a valid vehicle edit is saved without remote connectivity
- **THEN** the updated vehicle remains available locally and pending for a later synchronization attempt
