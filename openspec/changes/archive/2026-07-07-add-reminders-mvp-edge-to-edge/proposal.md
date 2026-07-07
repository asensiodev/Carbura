## Why

The MVP needs one more high-value product loop before the Friday delivery: after tracking vehicles and maintenance, users should be able to plan upcoming actions. The Android UI should also behave correctly on modern devices with edge-to-edge system bars so the demo feels polished and production-ready.

## What Changes

- Add a minimal reminders feature for Android MVP:
  - list pending reminders
  - create a reminder for a vehicle
  - support due date and/or due odometer
  - mark reminders as completed
  - persist reminders locally with SQLDelight
- Add navigation entry points so reminders are reachable from the Android app.
- Enable Android edge-to-edge rendering and apply safe insets to top-level screens.
- Keep reminder notifications, remote sync, recurring schedules, and family sharing workflows out of this change.

## Capabilities

### New Capabilities
- `reminders-mvp`: Android MVP reminder creation, listing, completion, and local persistence.
- `android-edge-to-edge-ui`: Android edge-to-edge rendering with safe system-bar inset handling.

### Modified Capabilities
- None.

## Impact

- Affected modules: `feature:reminders`, `core:model`, `core:domain`, `core:data`, `app:shared`, `app:android`, and `core:designsystem` if inset/polish tokens are needed.
- Adds local reminder repository behavior backed by existing SQLDelight schema or minimal query additions.
- Adds shared MVI presentation files for reminders following the current feature architecture.
- Android UI behavior changes around system bars and screen padding, without changing domain contracts for existing features.
