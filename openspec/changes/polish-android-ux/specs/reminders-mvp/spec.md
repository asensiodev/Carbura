## ADDED Requirements

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
