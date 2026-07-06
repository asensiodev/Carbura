## Why

The MVP currently keeps garage and maintenance data in feature-local in-memory repositories, so data is lost on process restart and persistence responsibilities leak into feature modules. Navigation also uses a manual Compose state switch even though typed Navigation 3 route keys already exist, which will not scale cleanly as more screens are added.

## What Changes

- Move MVP repository implementations for vehicles and maintenance records into `core:data` behind existing domain repository contracts.
- Introduce local persistent storage for Android-first MVP data so created vehicles and maintenance records survive app restarts.
- Keep domain and feature presentation independent from platform storage APIs.
- Replace the manual route `when` state in `MainActivity` with a typed Navigation 3 back stack and scene host.
- Keep typed route definitions in shared code and route rendering in the Android app boundary.

## Capabilities

### New Capabilities
- `local-persistence`: Local repository persistence for MVP vehicle and maintenance data.

### Modified Capabilities
- `shared-presentation-architecture`: Upgrade Navigation 3 readiness into an actual typed Navigation 3 stack for Android MVP screens.
- `kmp-project-structure`: Clarify that data implementations live in `core:data`, not feature modules.

## Impact

- Affects `core:data`, `feature:garage`, `feature:maintenance`, `app:shared`, `app:android`, Gradle version catalog and possibly build convention configuration.
- Adds a local persistence dependency, preferably SQLDelight for KMP-friendly storage.
- Requires repository tests for persisted vehicle and maintenance data.
- Requires Android build verification after Navigation 3 host integration.
