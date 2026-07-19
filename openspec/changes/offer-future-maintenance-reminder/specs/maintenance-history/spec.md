## ADDED Requirements

### Requirement: Future Maintenance Reminder Consent
The maintenance creation form SHALL pause a valid future-dated submission until the user chooses whether the save includes a reminder.

#### Scenario: Save future maintenance with reminder
- **WHEN** the user accepts the reminder offer
- **THEN** the system saves the maintenance through the domain creation use case and requests a reminder for the saved record

#### Scenario: Save future maintenance without reminder
- **WHEN** the user declines the reminder offer using the save-only action
- **THEN** the system saves the maintenance through the domain creation use case without requesting a planned-maintenance reminder
