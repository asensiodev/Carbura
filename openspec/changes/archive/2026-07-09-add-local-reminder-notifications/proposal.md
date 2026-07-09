## Why

Recordatorios already let the user create due items, but the MVP does not yet notify the user outside the app. Local Android notifications complete the core reminder value for the demo without requiring remote notifications or background sync.

## What Changes

- Add local Android notification scheduling for reminders that have a due date.
- Request/use Android notification permission where required by the platform.
- Create an Android notification channel for reminder alerts.
- Schedule notifications when reminders are created and cancel them when reminders are completed, deleted, or their vehicle is deleted.
- Keep platform APIs behind a shared contract so reminder logic remains KMP-ready.

## Capabilities

### New Capabilities

### Modified Capabilities
- `reminders-mvp`: Add local notification behavior for date-based reminders.
- `kmp-project-structure`: Clarify that notification platform integration stays behind shared contracts.

## Impact

- Affected modules: `core:domain`, `core:data` or Android app wiring, `feature:reminders`, and `app:android`.
- Android platform impact: notification permission, notification channel, alarm/notification delivery component.
- No Supabase schema changes and no remote notifications.
