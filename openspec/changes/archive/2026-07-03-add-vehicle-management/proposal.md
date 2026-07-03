## Why

Carbura needs its first visible Android flow so a user can start building a garage by adding a vehicle. This turns the existing shared domain model into an end-to-end MVP slice while keeping auth, Supabase sync and local persistence out of scope for this step.

## What Changes

- Add an Android garage screen with an empty state, a vehicle creation form and a vehicle list.
- Connect the form to the existing shared `CreateVehicleUseCase` through a simple in-memory repository.
- Represent loading, success and validation errors in UI state.
- Keep the implementation Android-first and testable without requiring Supabase or real authentication.

## Capabilities

### New Capabilities
- `vehicle-management`: Covers the Android-first garage flow for listing and creating vehicles in the MVP.

### Modified Capabilities

## Impact

- Affects `feature:garage`, `app:android`, and potentially `core:data` or `core:testing` for a temporary in-memory repository.
- Reuses existing `core:model` and `core:domain` contracts and use cases.
- No secrets, remote backend calls, database migrations or breaking API changes are introduced.
