## Context

Carbura stores reminder state locally before synchronizing it remotely, but Android alarm scheduling is currently executed as a second suspend step in domain use cases. Creation, completion, deletion, maintenance orchestration, and vehicle deletion can therefore commit database state and then be cancelled before the corresponding alarm action completes. Retrying from the UI is not a durable recovery mechanism and can generate a new manual reminder identity.

Notification scheduling is an external side effect and cannot share an atomic transaction with SQLDelight. Production `NonCancellable` blocks would only reduce one cancellation window; they would not survive process death, scheduler failure, or device restart.

## Goals / Non-Goals

**Goals:**

- Atomically record the desired notification side effect with each local reminder mutation.
- Make notification application replayable and idempotent.
- Recover pending operations after cancellation, process recreation, scheduler failure, and device restart.
- Prevent stale alarm intents from displaying notifications for completed, deleted, or superseded reminders.
- Preserve deterministic identities and avoid duplicate reminders during retry.
- Keep local database success authoritative and responsive.

**Non-Goals:**

- Making local notification delivery part of remote Supabase synchronization.
- Guaranteeing exact wall-clock execution while Android denies notification or exact-alarm permissions.
- Introducing a generic distributed workflow engine.
- Keeping a coroutine alive with production `NonCancellable` solely to finish side effects.
- Changing reminder business rules, lead times, or user-facing copy.

## Decisions

### Store desired notification state as a coalescing outbox

SQLDelight will store one pending desired notification state per reminder identity rather than an append-only event history. The row will contain the reminder ID, desired action (`Schedule` or `Cancel`), the schedule payload needed by Android, and a monotonically changing revision/fingerprint.

Each reminder mutation and its desired notification row will be committed in the same SQLDelight transaction. A later mutation for the same reminder replaces the pending desired state, avoiding redundant schedule/cancel chains while preserving the latest intent.

An append-only outbox was considered but rejected because intermediate notification actions have no audit value and must be coalesced before replay anyway.

### Acknowledge only the revision that was applied

The processor will read a pending row, apply its deterministic scheduler operation, and delete/acknowledge that row only if its revision still matches. If a concurrent mutation replaces the desired state while processing, acknowledgement of the older revision cannot erase the newer work.

Alarm scheduling and cancellation already use stable reminder-based identities, so replaying the same revision is idempotent. Cancellation after applying the alarm but before acknowledgement safely causes the same operation to run again.

### Treat database commit as mutation success

Once reminder state and its outbox row commit, the domain mutation is successful. Immediate outbox draining is best effort and does not roll back or duplicate the business mutation. Scheduler errors leave the row pending for retry instead of surfacing as persistence failure.

This separates user data durability from Android delivery availability and removes the need for a UI retry to recreate a reminder after partial completion.

### Trigger recovery from multiple durable boundaries

After a relevant mutation, the app will request an immediate serialized drain. It will also request unique Android background work and drain at authenticated startup/foreground entry. The SQLDelight outbox remains the source of truth; WorkManager and lifecycle hooks are only execution triggers.

The processor will serialize drains so multiple triggers do not apply the same pending set concurrently. Cancellation stops the current drain without deleting unapplied or unacknowledged rows.

### Validate alarm intents against current local state

Scheduled alarm intents will carry the reminder identity and revision/fingerprint. Before displaying a notification, the receiver will asynchronously verify that the local reminder is still active, incomplete, not deleted, and matches the scheduled revision. A stale alarm may still fire at the Android level, but it will not produce a user-visible notification.

This check closes the interval between a local cancellation/deletion commit and eventual outbox processing.

### Keep outbox contracts KMP-compatible

Outbox models, local data-source contracts, and orchestration tests will remain in common code. Android supplies the scheduler processor, WorkManager trigger, and receiver validation. JVM Desktop tests use fakes; no Desktop application or notification implementation is introduced.

## Risks / Trade-offs

- [Outbox schema and migrations increase persistence complexity] -> Use one keyed desired-state table, explicit migration tests, and atomic repository transaction tests.
- [WorkManager enqueue can itself be interrupted after commit] -> Keep startup, foreground, immediate, and worker triggers; the persisted outbox is never dependent on one trigger.
- [A pending schedule may be delayed until recovery executes] -> Request immediate and unique background drains after commit and expose deterministic retry tests.
- [Receiver database initialization may exceed broadcast limits] -> Use `goAsync`, an application-owned scope with a bounded timeout, and always finish the pending result.
- [Concurrent mutation and drain can race] -> Use revision-conditional acknowledgement and processor serialization.
- [Permissions can prevent notification delivery indefinitely] -> Retain desired state or classify permission-denied outcomes explicitly without treating them as data loss.
- [Old scheduled intents lack a revision after upgrade] -> Treat missing revision as stale unless current data can be proven equivalent, then reconcile all active reminders during migration startup.

## Migration Plan

1. Add the SQLDelight desired-notification outbox table and migration with migration tests.
2. Add atomic local transaction APIs and convert reminder/vehicle/maintenance mutation paths.
3. Implement and test the common outbox processor contract with an Android scheduler adapter.
4. Add immediate, WorkManager, startup, foreground, boot, and app-update recovery triggers as appropriate.
5. Add receiver validation for active reminder identity and revision.
6. Reconcile existing active reminders once after migration to populate desired schedule state.

Rollback must preserve existing reminder rows. The outbox table can be ignored by the previous application version, but alarms created by the new version remain compatible with stable reminder identities.

## Open Questions

- Exact-alarm and notification permission denial need explicit processor result categories during implementation; both must retain enough state for later reconciliation without retry loops that consume excessive resources.
