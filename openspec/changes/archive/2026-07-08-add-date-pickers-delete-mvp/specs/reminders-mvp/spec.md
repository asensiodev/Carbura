## MODIFIED Requirements

### Requirement: Reminder Creation Form
The system SHALL allow the Android user to create a reminder for a vehicle using MVP fields.

#### Scenario: Valid reminder with due date
- **WHEN** the user submits a reminder with title, vehicle, and picker-selected valid due date
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

### Requirement: Reminder Deletion
The system SHALL allow the Android user to delete a pending reminder after confirmation.

#### Scenario: Delete pending reminder
- **WHEN** the user confirms deletion of a pending reminder
- **THEN** the reminder is removed from local persistence and no longer appears in the pending reminders list
