## Context

Carbura already supports two incomplete halves of this flow. `MaintenanceRecord.nextDueDate` is modeled, stored in SQLDelight, synchronized through Supabase, and represented in remote DTOs, while `CreateAutomaticReminderUseCase` can create a single ITV or insurance reminder. The Android maintenance form currently captures a free-text type and no next due date, classifies every record as `Custom`, and never invokes automatic reminder creation.

The reminder model stores one logical due target and one primary notice lead time. Creating one reminder row per notification would require users to manage duplicate-looking cards and would allow the alerts for one expiration to diverge through completion, deletion, or synchronization. The product decision is therefore one logical reminder with several fixed notification moments.

## Goals / Non-Goals

**Goals:**

- Create a canonical ITV or insurance maintenance record with an optional next due date.
- Persist one deterministic generated reminder per source maintenance record.
- Schedule ITV notifications 60, 30, and 7 days before expiration.
- Schedule insurance notifications 45, 37, and 7 days before expiration.
- Explain the insurance decision window without presenting policy-specific timing as universal legal advice.
- Make creation and source deletion idempotent and preserve unrelated reminders.
- Reuse existing local and remote schema fields and remain compatible with future Desktop clients.

**Non-Goals:**

- User-configurable multi-alert schedules or recurrence.
- Automatic calculation of the next inspection, renewal, or service date.
- Maintenance record editing.
- Automatic reminders for arbitrary custom maintenance types.
- A reminder-source database column or schema migration.
- Desktop notifications or Desktop UI implementation.

## Decisions

### Persist one logical reminder and schedule several alert instances

The generated reminder ID is `maintenance-reminder:<maintenance-record-id>`. One reminder remains visible in Reminders and synchronizes normally. A shared notification plan contains several typed alert offsets, and the Android scheduler creates a unique alarm identity from the reminder ID plus alert kind.

Creating several reminder rows was rejected because it clutters the pending list, duplicates sync state, and gives completion or deletion ambiguous semantics. Adding an alert-offset collection to the database was rejected because these release policies are fixed and deterministic, so a schema migration would add storage complexity without user configurability.

The existing `noticeDaysBefore` field stores the primary/earliest lead time, 60 for ITV and 45 for insurance. Explicit alert plans, rather than that summary field, drive multi-alert scheduling.

### Use typed notification alerts with platform-localized copy

Shared domain code defines alert kinds and offsets, not Spanish notification strings. The Android scheduler passes the alert kind and expiration date to the receiver, which resolves localized title and body copy at delivery time.

Insurance copy states that the policy should be reviewed and that the usual notice period is at least one month. It does not state that every policy or jurisdiction has the same legal deadline.

### Use canonical maintenance type selection

Maintenance creation stores a `MaintenanceTypeCode` selected from canonical options. ITV and insurance reveal an optional next-due-date picker; custom maintenance retains a user-entered label. Domain input carries both the canonical code and optional custom label, preventing localized text inference.

### Orchestrate record and reminder creation in shared domain code

A maintenance creation orchestrator validates and persists the record, then derives and saves the optional generated reminder and schedules its plan. Presentation receives a result that identifies whether a reminder was created so it can show accurate feedback.

The ViewModel allocates one pending maintenance record ID and retains it across retries until the full operation succeeds. Combined with the deterministic reminder ID, retry after a partial failure upserts the same logical entities rather than duplicating them.

A cross-repository database transaction is not introduced because repositories and notification scheduling cross persistence/platform boundaries. Idempotent deterministic retry is the bounded local-first strategy for this release.

### Delete generated reminder with its source record

Maintenance deletion derives `maintenance-reminder:<record-id>`, deletes that reminder when present, and cancels all alert instances for its ID. It never searches by vehicle or maintenance type and therefore cannot delete manual reminders or vehicle-planning reminders.

### Keep fixed policies reusable across platforms

Shared domain policy defines ITV `60/30/7` and insurance `45/37/7`. Android implements local scheduling now. A future Desktop client can display and synchronize the same one logical reminder while independently deciding whether Desktop notifications are supported.

## Risks / Trade-offs

- [A reminder save or alarm schedule can fail after maintenance persistence] -> Retain the maintenance ID across retry and use deterministic reminder/alarm identities.
- [Fixed alert policies cannot be customized] -> Keep configurability explicitly out of scope and model policy functions so a future schema can replace them deliberately.
- [Insurance notice periods vary] -> Use qualified copy and direct users to review their policy.
- [An alert date may already be in the past] -> Schedule only future alert instants and keep the logical reminder visible until completion or deletion.
- [Multiple alarms must all be canceled] -> Derive every alarm identity from the same reminder ID and known fixed alert kinds.
- [Canonical type selection changes existing free-text behavior] -> Retain a Custom option and custom label field.

## Migration Plan

1. Add failing domain tests for policies, deterministic IDs, orchestration, retry, and source deletion cleanup.
2. Extend the scheduler contract and Android implementation to accept typed multi-alert plans while preserving manual single-alert scheduling.
3. Add canonical maintenance type and next due date to maintenance presentation.
4. Wire shared orchestration through maintenance dependency injection.
5. Add Compose and an app-module deterministic journey test without introducing production authentication switches; retain the final authenticated UI E2E as its separate roadmap change.
6. Run strict OpenSpec validation, the full quality gate, Android instrumentation, and manual notification scheduling verification.

No database migration is required. Rollback removes the UI/orchestration path; existing generated reminders remain valid ordinary reminder rows and can still be deleted manually.

## Open Questions

None. ITV uses 60/30/7 days and insurance uses 45/37/7 days as fixed release policies.
