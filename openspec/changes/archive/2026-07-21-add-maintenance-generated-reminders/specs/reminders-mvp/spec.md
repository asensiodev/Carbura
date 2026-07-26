## MODIFIED Requirements

### Requirement: Pending Reminder List
The system SHALL show pending reminders for the active family on Android and SHALL represent a maintenance-generated expiration as one logical reminder even when several local notification moments are scheduled for it.

#### Scenario: Empty pending reminders
- **WHEN** the active family has no pending reminders
- **THEN** the Android reminders screen shows an empty state that invites the user to create a reminder

#### Scenario: Pending reminders are shown
- **WHEN** the active family has pending reminders
- **THEN** the Android reminders screen shows each logical pending reminder with title, vehicle context, and due target when available

#### Scenario: Generated reminder has multiple alerts
- **WHEN** an ITV or insurance reminder has several scheduled notification moments
- **THEN** the Android reminders screen shows one pending card for the expiration rather than one card per alert

### Requirement: Local Reminder Notifications
The system SHALL schedule local Android notifications for pending reminders with due dates, including all fixed alert moments associated with a maintenance-generated logical reminder.

#### Scenario: Manual reminder with due date schedules notification
- **WHEN** the Android user creates a manual pending reminder with a valid due date
- **THEN** the app schedules its configured single local notification

#### Scenario: Maintenance-generated reminder schedules notification plan
- **WHEN** an ITV or insurance reminder is generated from maintenance
- **THEN** the app schedules each future alert in its fixed maintenance policy under the same logical reminder identity

#### Scenario: Odometer-only reminder does not schedule date notification
- **WHEN** the Android user creates a pending reminder without a due date and with only a due odometer
- **THEN** the app does not schedule a date-based local notification

#### Scenario: Completing reminder cancels notifications
- **WHEN** the Android user marks a reminder as completed
- **THEN** the app cancels every scheduled local notification for that logical reminder

#### Scenario: Deleting reminder cancels notifications
- **WHEN** the Android user deletes a reminder
- **THEN** the app cancels every scheduled local notification for that logical reminder

#### Scenario: Deleting vehicle cancels reminder notifications
- **WHEN** the Android user deletes a vehicle with pending reminders
- **THEN** the app cancels every scheduled local notification for those reminders
