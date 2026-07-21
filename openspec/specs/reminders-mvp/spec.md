## Purpose
Define the Android MVP reminder workflow for pending reminders, creation, completion, and local persistence.

## Requirements

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

### Requirement: Reminder Deletion
The system SHALL allow the Android user to delete a pending reminder after confirmation.

#### Scenario: Delete pending reminder
- **WHEN** the user confirms deletion of a pending reminder
- **THEN** the reminder is removed from local persistence and no longer appears in the pending reminders list

### Requirement: Local Reminder Notifications
The system SHALL schedule local Android notifications for pending reminders with due dates.

#### Scenario: Reminder with due date schedules notification
- **WHEN** the Android user creates a pending reminder with a valid due date
- **THEN** the app schedules a local notification for that reminder

#### Scenario: Odometer-only reminder does not schedule date notification
- **WHEN** the Android user creates a pending reminder without a due date and with only a due odometer
- **THEN** the app does not schedule a date-based local notification

#### Scenario: Completing reminder cancels notification
- **WHEN** the Android user marks a reminder as completed
- **THEN** the app cancels any scheduled local notification for that reminder

#### Scenario: Deleting reminder cancels notification
- **WHEN** the Android user deletes a reminder
- **THEN** the app cancels any scheduled local notification for that reminder

#### Scenario: Deleting vehicle cancels reminder notifications
- **WHEN** the Android user deletes a vehicle with pending reminders
- **THEN** the app cancels scheduled local notifications for those reminders

### Requirement: Android Notification Permission
The system SHALL handle Android notification permission requirements before relying on local notifications.

#### Scenario: Android notification permission is requested where required
- **WHEN** the Android app runs on a platform version that requires runtime notification permission
- **THEN** the app requests notification permission from the user before posting local reminder notifications

#### Scenario: Permission denial does not block reminders
- **WHEN** notification permission is denied
- **THEN** reminder creation, completion, deletion, local persistence, and sync remain usable
