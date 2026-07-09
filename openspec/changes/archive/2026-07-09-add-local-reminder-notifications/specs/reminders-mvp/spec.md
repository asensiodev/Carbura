## ADDED Requirements

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
