## ADDED Requirements

### Requirement: Pending Reminder List
The system SHALL show pending reminders for the active family on Android.

#### Scenario: Empty pending reminders
- **WHEN** the active family has no pending reminders
- **THEN** the Android reminders screen shows an empty state that invites the user to create a reminder

#### Scenario: Pending reminders are shown
- **WHEN** the active family has pending reminders
- **THEN** the Android reminders screen shows each pending reminder with title, vehicle context, and due target when available

### Requirement: Reminder Creation Form
The system SHALL allow the Android user to create a reminder for a vehicle using MVP fields.

#### Scenario: Valid reminder with due date
- **WHEN** the user submits a reminder with title, vehicle, and valid due date
- **THEN** the reminder is saved and appears in the pending reminders list

#### Scenario: Valid reminder with due odometer
- **WHEN** the user submits a reminder with title, vehicle, and non-negative due odometer
- **THEN** the reminder is saved and appears in the pending reminders list

#### Scenario: Missing due target error
- **WHEN** the user submits a reminder without due date and without due odometer
- **THEN** the screen shows a validation error and the reminder is not saved

#### Scenario: Blank title error
- **WHEN** the user submits a reminder with a blank title
- **THEN** the screen shows a validation error and the reminder is not saved

#### Scenario: Missing vehicle error
- **WHEN** the user submits a reminder without selecting a vehicle
- **THEN** the screen shows a validation error and the reminder is not saved

### Requirement: Reminder Completion
The system SHALL allow the Android user to mark a pending reminder as completed.

#### Scenario: Complete pending reminder
- **WHEN** the user marks a pending reminder as completed
- **THEN** the reminder is persisted as completed and removed from the pending reminders list

### Requirement: Local Reminder Persistence
The system SHALL persist Android MVP reminders in local storage through the domain reminder repository contract.

#### Scenario: Reminder survives app restart
- **WHEN** a reminder is created and the app process is restarted
- **THEN** the pending reminders list returns the previously created reminder from local storage

#### Scenario: Completed reminder remains completed after restart
- **WHEN** a reminder is marked completed and the app process is restarted
- **THEN** the reminder does not appear in the pending reminders list
