## ADDED Requirements

### Requirement: Vehicle Reminder Suggestions
The system SHALL derive optional reminder suggestions from a vehicle's next ITV date, insurance renewal date, and next service odometer.

#### Scenario: Date targets produce suggestions
- **WHEN** a vehicle has a next ITV date or insurance renewal date
- **THEN** the system previews one corresponding date reminder suggestion for each populated target

#### Scenario: Service odometer produces suggestion
- **WHEN** a vehicle has a non-negative next service odometer
- **THEN** the system previews a service reminder suggestion with that odometer target

#### Scenario: Empty targets produce no suggestions
- **WHEN** all optional vehicle due targets are empty
- **THEN** the system does not preview or create a vehicle-generated reminder

### Requirement: Explicit Suggestion Confirmation
The Android app MUST obtain explicit user confirmation before creating or updating vehicle-generated reminders.

#### Scenario: Confirm reminder suggestions
- **WHEN** the user confirms the previewed vehicle reminder suggestions while saving
- **THEN** the system saves the vehicle and reconciles the confirmed generated reminders

#### Scenario: Decline reminder suggestions
- **WHEN** the user declines the previewed suggestions
- **THEN** the system saves the vehicle fields without creating or updating those suggested reminders

### Requirement: Duplicate-Free Generated Reminders
The system SHALL maintain at most one vehicle-generated reminder for each vehicle and target kind.

#### Scenario: Repeated save keeps one generated reminder
- **WHEN** the same confirmed vehicle target is saved more than once
- **THEN** the system updates the existing generated reminder instead of creating a duplicate

#### Scenario: Changed target updates generated reminder
- **WHEN** a confirmed vehicle target changes
- **THEN** the corresponding generated reminder uses the new due target and retains its stable identity

### Requirement: Generated Reminder Lifecycle
The system SHALL reconcile generated reminders when confirmed vehicle due targets change or are cleared without modifying unrelated reminders.

#### Scenario: Clear generated date target
- **WHEN** the user clears a vehicle date target and confirms saving the reminder changes
- **THEN** the system deletes the corresponding generated reminder and cancels its scheduled notification

#### Scenario: Clear generated odometer target
- **WHEN** the user clears the next service odometer and confirms saving the reminder changes
- **THEN** the system deletes the corresponding generated odometer reminder

#### Scenario: Preserve unrelated reminders
- **WHEN** generated vehicle reminders are reconciled
- **THEN** manual reminders and reminders generated from maintenance records remain unchanged

### Requirement: Generated Reminder Notifications
The system SHALL use the existing local notification behavior for confirmed vehicle-generated date reminders.

#### Scenario: Confirmed date suggestion schedules notification
- **WHEN** a vehicle-generated ITV or insurance reminder is confirmed with a due date
- **THEN** the app schedules or reschedules its local notification using the configured notice period

#### Scenario: Odometer suggestion has no date notification
- **WHEN** a vehicle-generated service reminder has only an odometer target
- **THEN** the app persists the reminder without scheduling a date-based notification
