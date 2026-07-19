## 1. Outbox Model And Migration

- [ ] 1.1 Define KMP desired-notification action, payload, revision, and processor result models.
- [ ] 1.2 Add failing SQLDelight tests for inserting, replacing, listing, and revision-conditionally acknowledging desired notification rows.
- [ ] 1.3 Add the desired-notification outbox table, generated queries, and schema migration.
- [ ] 1.4 Add migration tests proving existing reminder data is preserved and the outbox starts in a recoverable state.

## 2. Atomic Local Mutation Contracts

- [ ] 2.1 Add failing local repository tests for atomically saving a reminder and its schedule action.
- [ ] 2.2 Add failing tests for atomically completing or deleting a reminder and recording its cancel action.
- [ ] 2.3 Add failing tests for maintenance deletion and vehicle deletion recording cancel actions for every affected reminder.
- [ ] 2.4 Implement atomic SQLDelight transaction APIs for reminder schedule and cancel desired state.
- [ ] 2.5 Convert manual reminder, generated reminder, proactive vehicle reminder, maintenance, and vehicle workflows to the atomic contracts.
- [ ] 2.6 Verify existing pending remote-sync flags and tombstones remain correct in the expanded transactions.

## 3. Idempotent Outbox Processor

- [ ] 3.1 Add processor tests for deterministic schedule replay, cancel replay, and ordinary scheduler failure retention.
- [ ] 3.2 Add cancellation tests before scheduler application, after scheduler application, and before acknowledgement.
- [ ] 3.3 Add a race test proving acknowledgement of an older revision cannot delete a newer desired state.
- [ ] 3.4 Implement a serialized common outbox processor that applies pending rows through the reminder scheduler.
- [ ] 3.5 Implement revision-conditional acknowledgement and leave retryable or cancelled work pending.
- [ ] 3.6 Define permission-denied and non-retryable processor outcomes without deleting recoverable desired state incorrectly.

## 4. Android Recovery Triggers

- [ ] 4.1 Add the required AndroidX WorkManager dependency and dependency-injection bindings.
- [ ] 4.2 Implement unique background work that drains the SQLDelight outbox and maps pending retryable work to WorkManager retry.
- [ ] 4.3 Trigger an immediate application-owned drain and unique background work after relevant local commits.
- [ ] 4.4 Trigger reconciliation at authenticated startup and throttled foreground entry.
- [ ] 4.5 Restore pending recovery after boot or app replacement where Android alarm behavior requires it.
- [ ] 4.6 Add worker and lifecycle-trigger tests proving concurrent triggers remain serialized and use outbox state as the source of truth.

## 5. Stale Alarm Suppression

- [ ] 5.1 Add reminder revision/fingerprint data to scheduled alarm intents without changing stable alarm identities.
- [ ] 5.2 Add receiver tests for active matching, completed, deleted, missing, and superseded reminders.
- [ ] 5.3 Implement bounded asynchronous receiver validation using current local reminder state and always finish `goAsync` work.
- [ ] 5.4 Suppress stale notifications while preserving existing localized notification content for valid current alarms.

## 6. Existing Data Reconciliation

- [ ] 6.1 Add tests that classify existing active reminders for scheduling and inactive reminders for cancellation.
- [ ] 6.2 Implement one-time post-migration reconciliation with deterministic revisions.
- [ ] 6.3 Verify repeated migration reconciliation is idempotent and does not duplicate reminders or alarms.

## 7. End-To-End Cancellation Matrix

- [ ] 7.1 Add cancellation-boundary tests for manual reminder creation without generating a second reminder identity on recovery.
- [ ] 7.2 Add cancellation-boundary tests for reminder completion and deletion.
- [ ] 7.3 Add cancellation-boundary tests for maintenance-generated reminder creation and maintenance deletion.
- [ ] 7.4 Add cancellation-boundary tests for proactive vehicle reminders, vehicle reconciliation, and vehicle deletion.
- [ ] 7.5 Add process-recreation-equivalent tests proving a fresh processor drains rows left by a cancelled predecessor.

## 8. Verification

- [ ] 8.1 Run SQLDelight migration, common domain, data, worker, receiver, and affected feature tests.
- [ ] 8.2 Run Android instrumented tests for valid and stale notification delivery where platform behavior is required.
- [ ] 8.3 Run `qualityCheck`, `:app:android:assembleDebug`, and `git diff --check`.
- [ ] 8.4 Manually verify create, reschedule, complete, delete, vehicle delete, process restart, and temporary scheduler-failure recovery on device.
