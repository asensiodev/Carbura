## Context

The project already has shared KMP models and domain use cases for vehicle creation. The app still needs a visible Android-first garage flow that proves the domain can be exercised from UI before adding auth, Supabase client integration or SQLDelight persistence.

## Goals / Non-Goals

**Goals:**
- Provide a garage screen that lists vehicles and supports creating a valid vehicle.
- Reuse `CreateVehicleUseCase` and `VehicleRepository` instead of duplicating validation in the UI.
- Keep the first vertical slice simple, deterministic and testable.

**Non-Goals:**
- Real authentication, family selection or invitations.
- Supabase client, SQLDelight persistence or offline sync queues.
- Vehicle editing, deletion, images or advanced fields.
- Desktop UI.

## Decisions

- Use an in-memory repository for this slice. This keeps the UI flow independent from persistence work while still exercising the real domain contract.
- Keep the feature as `feature:garage` without splitting `api` and `impl`. The MVP does not yet need cross-feature API boundaries, and avoiding extra Gradle modules keeps delivery faster.
- Put screen state and intents in the garage feature so they can later move to shared `commonMain` if desktop becomes active.
- Use a deterministic placeholder `FamilyId` until auth/onboarding provides the real active garage.

## Risks / Trade-offs

- In-memory data is lost when the process restarts -> acceptable for this slice and replaced by persistence in a later change.
- Placeholder family ownership is not production-ready -> isolate it in the feature layer so auth can replace it without changing the domain model.
- UI tests may be heavier than needed now -> prioritize ViewModel/state tests and rely on `assembleDebug` for Compose integration.
