## ADDED Requirements

### Requirement: Desktop Vehicle Planning Fields
The system SHALL allow the Desktop user to provide optional next ITV date, insurance renewal date, and next service odometer values during vehicle creation and editing.

#### Scenario: Save planning targets during creation
- **WHEN** the Desktop user creates a valid vehicle with one or more supported planning targets
- **THEN** the system persists those targets with the created vehicle

#### Scenario: Edit and clear planning targets
- **WHEN** the Desktop user changes or clears supported planning targets on an existing vehicle
- **THEN** the system persists the updated optional values for the same vehicle

#### Scenario: Reject invalid service odometer
- **WHEN** the Desktop user submits a negative next service odometer
- **THEN** the system shows a validation error and does not persist the vehicle mutation

### Requirement: Desktop Vehicle Reminder Confirmation
The Desktop app MUST obtain an explicit decision before reconciling reminders derived from vehicle planning targets.

#### Scenario: Save with generated reminders
- **WHEN** planning targets produce reminder suggestions and the Desktop user confirms saving with reminders
- **THEN** the system saves the vehicle and reconciles the generated reminders without creating duplicates

#### Scenario: Save without generated reminders
- **WHEN** planning targets produce reminder suggestions and the Desktop user chooses save only
- **THEN** the system saves the vehicle fields without creating or updating the suggested reminders

### Requirement: Desktop Quick Odometer Update
The system SHALL allow the Desktop user to update a vehicle's current odometer without editing unrelated vehicle fields.

#### Scenario: Increase odometer from vehicle card
- **WHEN** the Desktop user submits a valid value greater than or equal to the current odometer
- **THEN** the system persists and displays the new odometer value for that vehicle

#### Scenario: Reject invalid quick odometer
- **WHEN** the Desktop user submits a blank, non-numeric, or negative quick odometer value
- **THEN** the system shows validation feedback and keeps the persisted odometer unchanged

### Requirement: Desktop Odometer Decrease Confirmation
The Desktop app MUST require explicit confirmation before persisting a quick odometer value lower than the current value.

#### Scenario: Confirm quick odometer decrease
- **WHEN** the Desktop user submits a lower odometer and confirms the displayed old and proposed values
- **THEN** the system persists the lower value for that vehicle

#### Scenario: Cancel quick odometer decrease
- **WHEN** the Desktop user cancels the lower-odometer confirmation
- **THEN** the system keeps the current odometer unchanged and returns to the quick-update workflow

### Requirement: Desktop Planning Notification Disclosure
The Desktop app SHALL disclose that native reminder notifications remain unavailable when vehicle planning creates date-based reminders.

#### Scenario: Generated date reminder on Desktop
- **WHEN** the Desktop user saves an ITV or insurance planning target with generated reminders
- **THEN** the reminder remains available in the application and the Desktop UI does not claim that a native system notification was scheduled
