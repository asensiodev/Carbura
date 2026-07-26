# future-maintenance-reminder-offer Specification

## Purpose
TBD - created by archiving change offer-future-maintenance-reminder. Update Purpose after archive.
## Requirements
### Requirement: Future maintenance submission requires a reminder choice
When a valid maintenance date is later than the current local date, the system SHALL ask the Android user whether to save only or save and create a reminder before persisting the maintenance.

#### Scenario: Future date offers a reminder
- **WHEN** the user submits maintenance with a valid `performedOn` later than the current local date
- **THEN** the screen presents an accessible reminder choice and does not persist the maintenance until the user chooses

#### Scenario: Today or past date saves directly
- **WHEN** the user submits maintenance with a valid `performedOn` equal to or earlier than the current local date
- **THEN** the system follows the existing maintenance creation flow without presenting the future-date reminder choice

#### Scenario: Dismissal preserves the form
- **WHEN** the future-date reminder choice is dismissed without selecting save-only or save-with-reminder
- **THEN** no maintenance or reminder mutation starts and the entered form values remain available

### Requirement: Planned maintenance reminder requires consent
The system SHALL create a planned-maintenance reminder only when the user explicitly selects save and create reminder.

#### Scenario: User accepts reminder creation
- **WHEN** the user chooses save and create reminder for future-dated maintenance
- **THEN** the maintenance is saved and one pending reminder uses the saved maintenance date as its due date

#### Scenario: User declines reminder creation
- **WHEN** the user chooses save only for future-dated maintenance
- **THEN** the maintenance is saved and no planned-maintenance reminder is created

#### Scenario: User declines after a partial reminder failure
- **WHEN** a prior save-with-reminder attempt persisted the deterministic reminder but did not complete and the user retries with save only
- **THEN** the system cancels and tombstones the partial planned reminder before reporting success

#### Scenario: Date is no longer future after a partial reminder failure
- **WHEN** a prior save-with-reminder attempt persisted the deterministic reminder but did not complete and the user retries with a date that is not future
- **THEN** the system cancels and tombstones the partial planned reminder before reporting success

### Requirement: Planned maintenance reminders converge by source
The system SHALL derive a deterministic planned-reminder identity from the source maintenance record and SHALL clean up that reminder when the source record is deleted.

#### Scenario: Confirmation is retried
- **WHEN** creation is retried with the same maintenance record identity
- **THEN** the reminder repository and scheduler converge on one planned-maintenance reminder

#### Scenario: Source maintenance is deleted
- **WHEN** a maintenance record with a planned-maintenance reminder is deleted
- **THEN** the planned reminder is tombstoned and its scheduled notification is canceled

### Requirement: Planned and next-due reminders remain distinct
The system SHALL keep a future `performedOn` reminder separate from an ITV or insurance reminder derived from `nextDueDate`.

#### Scenario: Both dates are supplied with consent
- **WHEN** eligible maintenance has both a future `performedOn`, a `nextDueDate`, and consent for the planned reminder
- **THEN** each date is represented by its own deterministic reminder identity and due date

