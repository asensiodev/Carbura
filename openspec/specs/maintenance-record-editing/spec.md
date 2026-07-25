# maintenance-record-editing Specification

## Purpose
Define safe cross-platform editing of active maintenance records and atomic convergence of their generated and planned reminders.

## Requirements
### Requirement: Active maintenance records are editable
The system SHALL allow Android and Desktop users to edit an active maintenance record from its vehicle history.

#### Scenario: User opens an active record for editing
- **WHEN** the user activates the edit action on a maintenance record
- **THEN** the maintenance form is populated with that record's editable values

#### Scenario: User cancels editing
- **WHEN** the user dismisses the edit form before submission
- **THEN** no persistence mutation occurs and edit state is cleared

### Requirement: Maintenance updates preserve identity and ownership
The system SHALL update only an active record matching the expected record ID, family ID, and vehicle ID and SHALL preserve its ID, family, vehicle, and currency.

#### Scenario: Scoped record exists
- **WHEN** valid edited values are submitted for the matching active record
- **THEN** only editable fields change and the updated record remains in the same vehicle history

#### Scenario: Scoped record is unavailable
- **WHEN** the record is missing, deleted, or belongs to another family or vehicle
- **THEN** the update returns not-found and no record is inserted or resurrected

### Requirement: Maintenance updates reuse shared validation
The system SHALL validate and normalize edited type, dates, odometer, cost, workshop, notes, and next-due date using the same domain rules as creation.

#### Scenario: Edited values are invalid
- **WHEN** shared maintenance validation rejects an edited value
- **THEN** no record or reminder mutation is persisted and the edit form remains available

### Requirement: Maintenance update reminders converge atomically
The system SHALL reconcile deterministic automatic and existing active planned reminders in the same transaction as the maintenance update.

#### Scenario: Automatic reminder remains eligible
- **WHEN** an edited ITV or insurance record has a next-due date
- **THEN** the same deterministic automatic reminder ID is upserted with the updated details and desired schedule

#### Scenario: Automatic reminder becomes ineligible
- **WHEN** an edit removes eligibility or clears the next-due date
- **THEN** the deterministic automatic reminder and desired notification are cancelled

#### Scenario: Existing planned reminder remains future
- **WHEN** an edited record already has an active planned reminder and its performed date remains future
- **THEN** the same planned reminder ID follows the updated date and type

#### Scenario: Planned reminder is no longer future
- **WHEN** an edited record has an active planned reminder and its performed date becomes today or past
- **THEN** the planned reminder and desired notification are cancelled

### Requirement: Maintenance edit cancellation remains control flow
The system SHALL propagate coroutine cancellation during update and SHALL keep retryable edit state without reporting a persistence error.

#### Scenario: Update coroutine is cancelled
- **WHEN** cancellation interrupts maintenance editing
- **THEN** active mutation state clears, no success effect is emitted, and the edit form remains retryable
