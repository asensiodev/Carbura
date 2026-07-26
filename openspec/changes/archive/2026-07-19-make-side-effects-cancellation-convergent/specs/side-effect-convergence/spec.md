## ADDED Requirements

### Requirement: Reminder mutations durably record desired notification state
Every local mutation that creates, reschedules, completes, deletes, or indirectly removes a reminder SHALL atomically persist both the reminder state and its latest desired notification action.

#### Scenario: Reminder is created
- **WHEN** a manual, proactive vehicle, or maintenance-generated reminder is committed locally
- **THEN** the same transaction records a pending deterministic schedule action for that reminder

#### Scenario: Reminder is completed or deleted
- **WHEN** a reminder is completed, deleted, removed with maintenance, or removed with a vehicle
- **THEN** the same transaction records a pending deterministic cancel action for each affected reminder

#### Scenario: Coroutine is cancelled after commit
- **WHEN** the owning coroutine is cancelled after the local transaction commits but before notification processing finishes
- **THEN** the business mutation remains successful and its desired notification action remains pending for recovery

### Requirement: Notification outbox replay is idempotent and race-safe
The notification outbox processor SHALL safely replay desired actions and SHALL acknowledge only the exact revision it applied.

#### Scenario: Schedule is replayed
- **WHEN** the same pending schedule revision is applied more than once
- **THEN** Android contains one effective alarm set for the stable reminder identity

#### Scenario: Cancel is replayed
- **WHEN** the same pending cancel revision is applied more than once
- **THEN** no matching alarm remains and replay produces no duplicate side effect

#### Scenario: Desired state changes during processing
- **WHEN** a newer desired revision replaces an outbox row while an older revision is being applied
- **THEN** acknowledgement of the older revision does not remove or mark the newer revision complete

#### Scenario: Processor is cancelled after applying an action
- **WHEN** cancellation occurs after scheduler application but before outbox acknowledgement
- **THEN** the row remains pending and replay converges to the same scheduler state

### Requirement: Pending notification work recovers without UI retry
The system SHALL retry pending notification actions independently of repeating the user business mutation.

#### Scenario: Scheduler temporarily fails
- **WHEN** Android scheduling or cancellation returns a retryable failure
- **THEN** local reminder state remains committed and the desired action remains pending

#### Scenario: Application returns after process recreation
- **WHEN** the authenticated application starts or returns to foreground with pending notification actions
- **THEN** it requests a serialized outbox drain without requiring the user to recreate, complete, or delete the reminder again

#### Scenario: Background recovery runs
- **WHEN** unique Android background recovery executes
- **THEN** it drains current pending actions from SQLDelight and reports retry only while retryable work remains

### Requirement: Stale alarms do not display notifications
The Android notification receiver SHALL validate a fired alarm against current local reminder state and its revision before displaying a notification.

#### Scenario: Deleted reminder alarm fires before cancellation replay
- **WHEN** an old alarm fires after its reminder was deleted locally but before the cancel action was processed
- **THEN** the receiver suppresses the user-visible notification

#### Scenario: Completed reminder alarm fires
- **WHEN** an alarm fires for a reminder that is now completed
- **THEN** the receiver suppresses the user-visible notification

#### Scenario: Superseded schedule fires
- **WHEN** an alarm revision does not match the reminder's current notification revision
- **THEN** the receiver suppresses the obsolete notification

#### Scenario: Current active schedule fires
- **WHEN** the reminder is active and its revision matches the alarm intent
- **THEN** the receiver displays the existing localized reminder notification

### Requirement: Existing reminders are reconciled after migration
After introducing durable desired notification state, the system SHALL reconcile existing active and inactive reminder records so their Android alarm state converges without data loss.

#### Scenario: Existing active reminder is eligible
- **WHEN** the upgraded application encounters an existing active reminder with a schedulable due date
- **THEN** it records or applies the current deterministic schedule state

#### Scenario: Existing reminder is inactive
- **WHEN** the upgraded application encounters a completed or deleted reminder with a potentially stale alarm
- **THEN** it records or applies a deterministic cancel state

### Requirement: Convergence is cancellation-tested at every side-effect boundary
Tests SHALL inject cancellation, process-recreation equivalents, and scheduler failures between local commit, scheduler application, and outbox acknowledgement.

#### Scenario: Cancellation matrix is executed
- **WHEN** reminder creation, completion, deletion, maintenance orchestration, or vehicle deletion is tested
- **THEN** every suspend boundary demonstrates committed local state, durable pending intent, idempotent retry, and no duplicate reminder identity

#### Scenario: Ordinary scheduler failure is tested
- **WHEN** the scheduler fails without cancellation
- **THEN** the test proves the pending action survives and a later successful drain acknowledges it
