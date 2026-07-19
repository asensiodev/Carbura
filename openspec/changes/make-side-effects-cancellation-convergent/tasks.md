## 1. Outbox Model And Migration

- [x] 1.1 Define KMP desired-notification action, payload, revision, and processor result models.
- [x] 1.2 Add failing SQLDelight tests for inserting, replacing, listing, and revision-conditionally acknowledging desired notification rows.
- [x] 1.3 Add the desired-notification outbox table, generated queries, and schema migration.
- [x] 1.4 Add migration tests proving existing reminder data is preserved and the outbox starts in a recoverable state.

## 2. Atomic Local Mutation Contracts

- [x] 2.1 Add failing local repository tests for atomically saving a reminder and its schedule action.
- [x] 2.2 Add failing tests for atomically completing or deleting a reminder and recording its cancel action.
- [x] 2.3 Add failing tests for maintenance deletion and vehicle deletion recording cancel actions for every affected reminder.
- [x] 2.4 Implement atomic SQLDelight transaction APIs for reminder schedule and cancel desired state.
- [x] 2.5 Convert manual reminder, generated reminder, proactive vehicle reminder, maintenance, and vehicle workflows to the atomic contracts.
- [x] 2.6 Verify existing pending remote-sync flags and tombstones remain correct in the expanded transactions.

## 3. Idempotent Outbox Processor

- [x] 3.1 Add processor tests for deterministic schedule replay, cancel replay, and ordinary scheduler failure retention.
- [x] 3.2 Add cancellation tests before scheduler application, after scheduler application, and before acknowledgement.
- [x] 3.3 Add a race test proving acknowledgement of an older revision cannot delete a newer desired state.
- [x] 3.4 Implement a serialized common outbox processor that applies pending rows through the reminder scheduler.
- [x] 3.5 Implement revision-conditional acknowledgement and leave retryable or cancelled work pending.
- [x] 3.6 Define permission-denied and non-retryable processor outcomes without deleting recoverable desired state incorrectly.

## 4. Android Recovery Triggers

- [x] 4.1 Add the required AndroidX WorkManager dependency and dependency-injection bindings.
- [x] 4.2 Implement unique background work that drains the SQLDelight outbox and maps pending retryable work to WorkManager retry.
- [x] 4.3 Trigger an immediate application-owned drain and unique background work after relevant local commits.
- [x] 4.4 Trigger reconciliation at authenticated startup and throttled foreground entry.
- [x] 4.5 Restore pending recovery after boot or app replacement where Android alarm behavior requires it.
- [x] 4.6 Add worker and lifecycle-trigger tests proving concurrent triggers remain serialized and use outbox state as the source of truth.

## 5. Stale Alarm Suppression

- [x] 5.1 Add reminder revision/fingerprint data to scheduled alarm intents without changing stable alarm identities.
- [x] 5.2 Add receiver tests for active matching, completed, deleted, missing, and superseded reminders.
- [x] 5.3 Implement bounded asynchronous receiver validation using current local reminder state and always finish `goAsync` work.
- [x] 5.4 Suppress stale notifications while preserving existing localized notification content for valid current alarms.

## 6. Existing Data Reconciliation

- [x] 6.1 Add tests that classify existing active reminders for scheduling and inactive reminders for cancellation.
- [x] 6.2 Implement one-time post-migration reconciliation with deterministic revisions.
- [x] 6.3 Verify repeated migration reconciliation is idempotent and does not duplicate reminders or alarms.

## 7. End-To-End Cancellation Matrix

- [x] 7.1 Add cancellation-boundary tests for manual reminder creation without generating a second reminder identity on recovery.
- [x] 7.2 Add cancellation-boundary tests for reminder completion and deletion.
- [x] 7.3 Add cancellation-boundary tests for maintenance-generated reminder creation and maintenance deletion.
- [x] 7.4 Add cancellation-boundary tests for proactive vehicle reminders, vehicle reconciliation, and vehicle deletion.
- [x] 7.5 Add process-recreation-equivalent tests proving a fresh processor drains rows left by a cancelled predecessor.

## 8. Verification

- [x] 8.1 Run SQLDelight migration, common domain, data, worker, receiver, and affected feature tests.
- [x] 8.2 Run Android instrumented tests for valid and stale notification delivery where platform behavior is required.
- [x] 8.3 Run `qualityCheck`, `:app:android:assembleDebug`, and `git diff --check`.
- [x] 8.4 Manually verify create, reschedule, complete, delete, vehicle delete, process restart, and temporary scheduler-failure recovery on device.
