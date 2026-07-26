## ADDED Requirements

### Requirement: Planned Maintenance Reminder Presentation
The system SHALL present a consented future-maintenance reminder as one normal pending reminder with the source vehicle and maintenance date.

#### Scenario: Planned maintenance reminder is pending
- **WHEN** the user consents while saving future-dated maintenance
- **THEN** the Reminders screen shows one pending reminder for that vehicle with `dueDate` equal to the maintenance date

#### Scenario: Planned maintenance notification is scheduled
- **WHEN** the planned-maintenance reminder is created with a future due date
- **THEN** the app schedules its local notification for the maintenance date
