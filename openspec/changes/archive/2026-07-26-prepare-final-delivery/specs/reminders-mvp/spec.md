## ADDED Requirements

### Requirement: Visible manual reminder notification timing
The system SHALL schedule a manually created reminder for its visible due date when the product does not expose a configurable notice period.

#### Scenario: User creates a future manual reminder
- **WHEN** the user creates a manual reminder with a future due date and no visible notice-period control
- **THEN** Android schedules the notification for that due date instead of immediately after creation

#### Scenario: Legacy notice period has already elapsed
- **WHEN** Android schedules an existing manual reminder whose hidden notice instant has elapsed but whose due date remains in the future
- **THEN** Android schedules the notification for the future due date instead of delivering it immediately
