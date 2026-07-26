## Why

Reminder, maintenance, and vehicle workflows combine local database mutations with Android notification scheduling in separate suspend steps. Cancellation, process death, or scheduler failure between those steps can leave persisted state and alarms inconsistent, while UI retry can duplicate work rather than complete the original operation.

## What Changes

- Introduce a durable local outbox for deterministic notification schedule and cancellation operations.
- Commit reminder state changes and their pending notification operations atomically in SQLDelight transactions.
- Drain pending notification operations after relevant mutations and during authenticated startup/foreground recovery.
- Make outbox replay idempotent so cancellation or process death after applying an alarm but before acknowledging the operation remains safe.
- Ensure manual reminders, generated maintenance reminders, proactive vehicle reminders, reminder completion/deletion, maintenance deletion, and vehicle deletion converge after partial execution.
- Preserve local-first responsiveness: database success remains authoritative while notification delivery retries independently.
- Add cancellation, process-recreation, ordinary scheduler failure, and idempotent replay tests at every side-effect boundary.

## Capabilities

### New Capabilities
- `side-effect-convergence`: Durable, idempotent convergence between local reminder state and Android notification side effects across cancellation, failure, and process recreation.

### Modified Capabilities

None.

## Impact

- Adds SQLDelight schema and migration work for pending notification operations.
- Affects local reminder/vehicle/maintenance persistence transactions and their domain orchestration use cases.
- Adds an Android outbox processor that uses the existing reminder notification scheduler.
- Affects authenticated startup and foreground recovery triggers.
- Adds KMP-compatible outbox models/interfaces and JVM Desktop test coverage; no Desktop application is introduced.
