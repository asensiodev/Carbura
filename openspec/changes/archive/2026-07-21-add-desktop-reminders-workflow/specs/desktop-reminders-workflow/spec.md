## ADDED Requirements

### Requirement: Desktop Reminders lists persistent pending reminders
The Desktop application SHALL load and display pending reminders for the active Desktop local family from the persistent SQLDelight database.

#### Scenario: Pending reminders exist
- **WHEN** the user opens Reminders with pending local reminders
- **THEN** the workspace displays them in shared domain order with vehicle and due-target context

#### Scenario: No pending reminders exist
- **WHEN** the local family has vehicles but no pending reminders
- **THEN** the workspace displays an empty state with an action to create a reminder

#### Scenario: Reminder loading fails
- **WHEN** the reminder or vehicle repositories cannot load the workspace
- **THEN** the workspace displays an error state with a retry action

### Requirement: Desktop user can filter reminders by vehicle
The Desktop Reminders workspace SHALL expose the shared multi-vehicle filter behavior without altering the underlying pending reminder collection.

#### Scenario: User selects vehicle filters
- **WHEN** the user selects one or more vehicles
- **THEN** the workspace displays only reminders associated with the selected vehicles

#### Scenario: Selected filters have no matches
- **WHEN** no pending reminders match the selected vehicles
- **THEN** the workspace displays a filter-specific empty state and allows filters to be cleared

### Requirement: Desktop user can create a manual reminder
The Desktop Reminders workspace SHALL provide a creation form backed by shared reminder validation and persistence rules.

#### Scenario: Valid reminder is submitted
- **WHEN** the user submits a title, vehicle, and at least one valid due target
- **THEN** the reminder is persisted, the form closes, and the refreshed list displays it

#### Scenario: Invalid reminder is submitted
- **WHEN** the user submits incomplete or invalid reminder data
- **THEN** the form remains open and displays validation feedback without persisting a reminder

### Requirement: Desktop user can complete a reminder
The Desktop Reminders workspace SHALL allow a pending reminder to be completed through the shared notification-aware mutation.

#### Scenario: Completion succeeds
- **WHEN** the user marks a pending reminder complete
- **THEN** the reminder is removed from the pending list and success feedback is displayed

#### Scenario: Completion fails
- **WHEN** persistence cannot complete the reminder
- **THEN** the reminder remains pending and failure feedback is displayed

### Requirement: Desktop user can delete a reminder
The Desktop Reminders workspace SHALL require confirmation before deleting a pending reminder.

#### Scenario: Deletion is confirmed
- **WHEN** the user confirms reminder deletion
- **THEN** the reminder is soft-deleted, removed from the pending list, and success feedback is displayed

#### Scenario: Deletion is cancelled
- **WHEN** the user dismisses the deletion confirmation
- **THEN** the reminder remains unchanged and visible

### Requirement: Desktop Reminders integrates with Garage
The Desktop Reminders workspace SHALL use the same local family as Desktop Garage and SHALL navigate through the Desktop shell when a vehicle is required.

#### Scenario: Local family has no vehicles
- **WHEN** the user opens Reminders before adding a vehicle
- **THEN** the workspace explains that a vehicle is required and offers navigation to Garage

#### Scenario: User requests Garage
- **WHEN** the user activates the Garage action from Reminders
- **THEN** the Desktop shell selects the Garage destination without opening a new window

### Requirement: Desktop notification capability is explicit
The Desktop Reminders workspace SHALL distinguish persistent in-app reminders from unavailable native operating-system notifications.

#### Scenario: User views reminder capability information
- **WHEN** the user opens the Desktop Reminders workspace
- **THEN** the interface states that reminders are stored locally and that native Desktop alerts are not enabled in this version
