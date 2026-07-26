## ADDED Requirements

### Requirement: Maintenance-Generated Logical Reminder
The system SHALL create one deterministic logical reminder when an ITV or insurance maintenance record is saved with a next due date.

#### Scenario: ITV maintenance has a next due date
- **WHEN** the user saves canonical ITV maintenance with a valid next ITV date
- **THEN** the maintenance record and one reminder identified by the source maintenance record are persisted locally and become eligible for synchronization

#### Scenario: Insurance maintenance has a next due date
- **WHEN** the user saves canonical insurance maintenance with a valid renewal date
- **THEN** the maintenance record and one reminder identified by the source maintenance record are persisted locally and become eligible for synchronization

#### Scenario: Eligible maintenance has no next due date
- **WHEN** the user saves ITV or insurance maintenance without a next due date
- **THEN** the maintenance record is saved without creating a generated reminder

#### Scenario: Unsupported maintenance type has a date
- **WHEN** the user saves another maintenance type
- **THEN** no maintenance-generated reminder is created

### Requirement: Fixed Multi-Alert Policy
The system SHALL schedule several local Android notification moments for one maintenance-generated logical reminder according to its canonical maintenance type.

#### Scenario: ITV alert policy
- **WHEN** an ITV reminder is generated for a future expiration
- **THEN** future notifications are scheduled for 60, 30, and 7 days before expiration without creating additional reminder cards

#### Scenario: Insurance alert policy
- **WHEN** an insurance reminder is generated for a future expiration
- **THEN** future notifications are scheduled for 45, 37, and 7 days before expiration without creating additional reminder cards

#### Scenario: Alert instant is already past
- **WHEN** one fixed notification instant is not in the future at scheduling time
- **THEN** that instant is skipped while later valid notification instants remain scheduled

### Requirement: Insurance Decision Guidance
Insurance alert copy SHALL explain the renewal decision window in user-facing language and SHALL direct the user to review the policy rather than assert a universal legal deadline.

#### Scenario: Early insurance alert is delivered
- **WHEN** the 45-day insurance notification is shown
- **THEN** it explains that the policy expires on the due date and advises reviewing the policy now because the usual notice period is at least one month

#### Scenario: Notice-window alert is delivered
- **WHEN** the 37-day insurance notification is shown
- **THEN** it explains that approximately one week remains before the usual one-month notice threshold

### Requirement: Idempotent Generated Reminder Lifecycle
Maintenance-generated reminder creation, scheduling, and source deletion SHALL use deterministic identities and SHALL not mutate manual or vehicle-planning reminders.

#### Scenario: Creation is retried after partial failure
- **WHEN** maintenance creation is retried after the source record was persisted but reminder work did not complete
- **THEN** the same maintenance record, logical reminder, and alert identities are reused without duplicate logical reminders

#### Scenario: Source maintenance is deleted
- **WHEN** the user confirms deletion of maintenance that owns a generated reminder
- **THEN** that generated reminder is tombstoned, every scheduled alert for it is canceled, and unrelated reminders remain unchanged

#### Scenario: Generated reminder is completed or deleted directly
- **WHEN** the user completes or deletes the logical reminder from Reminders
- **THEN** every scheduled alert associated with that reminder is canceled
