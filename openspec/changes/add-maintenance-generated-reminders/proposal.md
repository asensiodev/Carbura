## Why

Maintenance records already persist and synchronize an optional next due date, but the Android flow cannot capture that date or turn an ITV or insurance record into actionable alerts. Completing this path now makes the planned Android E2E truthful and protects users at the moments when inspection booking or insurance cancellation decisions still remain possible.

## What Changes

- Add canonical maintenance-type selection for ITV, insurance, and existing custom maintenance categories.
- Capture an optional next due date for ITV and insurance maintenance records.
- Create one deterministic logical reminder after an ITV or insurance record is saved with a next due date.
- Schedule multiple notifications for that reminder: ITV at 60, 30, and 7 days before expiration; insurance at 45, 37, and 7 days before expiration.
- Use insurance notification copy that explains the usual one-month notice window without presenting it as universal legal advice.
- Cancel the generated notification schedule and delete only the associated generated reminder when its source maintenance record is deleted.
- Preserve manual reminders and vehicle-planning reminders independently.
- Add domain, data, ViewModel, Compose, scheduling, and app-module journey integration coverage.
- Exclude recurrence, automatic due-date calculation, maintenance editing, arbitrary per-user alert schedules, and new reminder-source database columns.

## Capabilities

### New Capabilities

- `maintenance-generated-reminders`: Deterministic reminder creation, multi-alert notification policy, source deletion cleanup, and user-facing feedback for ITV and insurance maintenance.

### Modified Capabilities

- `maintenance-history`: Add canonical maintenance types and optional next due dates to Android maintenance creation.
- `reminders-mvp`: Present one logical maintenance-generated reminder while scheduling multiple notification moments.

## Impact

- Shared maintenance and reminder domain use cases, deterministic identity helpers, and notification scheduler contracts.
- Maintenance MVI state, events, ViewModel, dependency injection, Android form, strings, and tests.
- Android AlarmManager scheduling identities and notification copy.
- Existing SQLDelight and Supabase fields are reused; no database migration is expected.
- Application-level Android E2E infrastructure for the vehicle-to-maintenance-to-reminder journey.
