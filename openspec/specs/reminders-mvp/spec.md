## Purpose
Define the Android MVP reminder workflow for pending reminders, creation, completion, and local persistence.
## Requirements
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
- **THEN** the reminder is retained as a tombstone pending synchronization, its notification is canceled, and it no longer appears in the active reminder list

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

### Requirement: Android Notification Permission
The system SHALL handle Android notification permission requirements before relying on local notifications.

#### Scenario: Android notification permission is requested where required
- **WHEN** the Android app runs on a platform version that requires runtime notification permission
- **THEN** the app requests notification permission from the user before posting local reminder notifications

#### Scenario: Permission denial does not block reminders
- **WHEN** notification permission is denied
- **THEN** reminder creation, completion, deletion, local persistence, and sync remain usable

### Requirement: Reminder Vehicle Prerequisite
The Reminders screen SHALL prevent opening an unusable reminder form when the active family has no vehicles and SHALL provide a clear path to Garage.

#### Scenario: Family has no vehicles
- **WHEN** the user opens Reminders before creating a vehicle
- **THEN** the screen explains that a vehicle is required and offers navigation to Garage instead of opening the reminder form

### Requirement: Bounded Reminder Vehicle Selection
Reminder creation SHALL use an accessible bounded single-choice vehicle selector that remains usable with many vehicles.

#### Scenario: Family has many vehicles
- **WHEN** the user selects a vehicle while creating a reminder
- **THEN** the selector remains scrollable or bounded and the save action remains reachable

### Requirement: Recoverable Reminder Presentation
Reminders SHALL provide recoverable load failure, retry, mutation progress, responsive card layout, and user-facing fallback text for unavailable vehicles.

#### Scenario: Referenced vehicle is unavailable
- **WHEN** a reminder references a vehicle that cannot be resolved locally
- **THEN** the UI shows localized unavailable-vehicle text instead of an internal identifier

#### Scenario: Notification permission cannot be requested again
- **WHEN** Android no longer presents the notification permission prompt after denial
- **THEN** the screen explains how to enable notifications in system settings without blocking reminder management

### Requirement: Adaptive Reminder Creation Action
Reminders SHALL expose reminder creation through a thumb-reachable labeled action on compact non-empty screens while retaining prerequisite and empty-state actions.

#### Scenario: Pending reminders exist on a compact phone
- **WHEN** the reminder list is displayed and vehicle prerequisites are satisfied
- **THEN** a labeled creation action is available near the bottom end and does not obscure the final reminder card

### Requirement: Pending Reminder Vehicle Filtering
The system SHALL allow the Android user to filter pending reminders by one or more vehicles without changing reminder persistence or ordering.

#### Scenario: All reminders are selected by default
- **WHEN** the reminders screen loads with no vehicle filters selected
- **THEN** the screen shows all pending reminders in the existing due-target order

#### Scenario: Filter reminders by one vehicle
- **WHEN** the user selects one vehicle filter
- **THEN** the screen shows only pending reminders associated with that vehicle

#### Scenario: Filter reminders by multiple vehicles
- **WHEN** the user selects multiple vehicle filters
- **THEN** the screen shows pending reminders associated with any selected vehicle in the existing due-target order

#### Scenario: Clear vehicle filters
- **WHEN** the user selects the exclusive `All` filter or deselects the final selected vehicle
- **THEN** the screen clears vehicle filtering and shows all pending reminders

#### Scenario: Selected vehicle becomes unavailable
- **WHEN** vehicle data reloads without a previously selected filter vehicle
- **THEN** the screen removes that vehicle from the filter selection and falls back to `All` when no valid selections remain

#### Scenario: No reminders match selected vehicles
- **WHEN** pending reminders exist but none are associated with the selected vehicle filters
- **THEN** the screen shows a filter-specific empty state without presenting the family-wide empty state

### Requirement: Accessible Vehicle Filter Controls
The system SHALL present vehicle filters as horizontally scrollable rounded controls whose selected state does not depend on a checkmark icon.

#### Scenario: Vehicle filters overflow horizontally
- **WHEN** the available vehicle filter controls exceed the screen width
- **THEN** the user can scroll horizontally to reach every filter

#### Scenario: Filter selection is communicated
- **WHEN** a filter is selected
- **THEN** its visual styling and accessibility semantics communicate the selected state without displaying a selected checkmark icon
