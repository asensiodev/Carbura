## Why

Carbura accepts a future date when a user records maintenance, but currently treats that date only as history and gives no opportunity to turn the planned work into an actionable reminder. Asking at save time preserves the user's intent without silently creating reminders or blocking future-dated records.

## What Changes

- Detect a valid `performedOn` date later than the device's current local date when maintenance is submitted.
- Ask whether to save only or save and create a reminder before performing either mutation.
- Create an idempotent reminder for the planned maintenance date after the maintenance record is saved and only with explicit consent.
- Support the offer for every maintenance type while preserving the separate ITV/insurance reminder derived from `nextDueDate`.
- Remove the planned reminder and its scheduled notification when the source maintenance record is deleted.
- Preserve cancellation propagation, retry safety, accessibility, and large-text behavior.

## Capabilities

### New Capabilities

- `future-maintenance-reminder-offer`: Consent, creation, idempotency, scheduling, and cleanup for reminders derived from future maintenance dates.

### Modified Capabilities

- `maintenance-history`: Future-dated maintenance submission pauses for an explicit reminder choice before saving.
- `reminders-mvp`: A consented planned-maintenance reminder appears as one normal pending reminder and follows existing notification behavior.

## Impact

- Shared maintenance presentation state, events, effects, and ViewModel orchestration.
- Android maintenance confirmation UI and localized copy.
- Domain reminder identity, creation, and source cleanup behavior.
- Koin wiring for the maintenance feature.
- Common unit tests, Android instrumented tests, architecture checks, and OpenSpec contracts.
- No new runtime dependency, database column, remote schema, or platform target.
