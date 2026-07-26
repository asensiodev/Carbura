## Context

Vehicles currently store identity and odometer data but no upcoming obligations. Reminders can be created manually or automatically from ITV/insurance maintenance records, and date reminders already schedule local Android notifications. Vehicle creation and editing now share local-first save and sync behavior, making the vehicle form the natural place to capture optional next due targets.

This change crosses model, persistence, sync, domain orchestration, presentation, Android date selection, and notification scheduling. Existing user data and manual reminders must remain valid.

## Goals / Non-Goals

**Goals:**

- Capture optional next ITV date, insurance renewal date, and next service odometer on a vehicle.
- Preview and require confirmation for reminders derived from those fields.
- Create, update, schedule, or remove generated reminders without duplicates.
- Preserve manual and maintenance-generated reminders independently.
- Synchronize vehicle due targets between devices.
- Expose enough upcoming reminder data for later garage-card UX work.

**Non-Goals:**

- Recurring reminder rules or automatic calculation of future dates after completion.
- Background odometer collection or notifications triggered automatically by device mileage.
- A complete vehicle-card or garage visual redesign.
- Remote push notifications, realtime synchronization, or advanced conflict resolution.
- Automatically creating suggestions without explicit user confirmation.

## Decisions

### Store explicit next targets on the vehicle

`Vehicle` gains nullable `nextItvDate`, `insuranceRenewalDate`, and `nextServiceOdometerKm` fields. Explicit next targets are simpler and more predictable than storing recurrence intervals in this iteration. Recurrence can be added later after completion semantics are defined.

The fields are persisted in SQLDelight and Supabase and included in vehicle sync DTOs. Keeping them on the vehicle allows forms and future summary cards to display current planning data without inferring it from reminder titles.

### Use deterministic IDs for vehicle-generated reminders

Each generated reminder uses a stable ID derived from vehicle ID and target kind, such as `vehicle-reminder:<vehicle-id>:itv`. Saving the same suggestion therefore updates the existing reminder through current upsert behavior instead of creating duplicates.

A new reminder source column was considered but rejected for this slice because stable reserved IDs provide lifecycle ownership without expanding every reminder mapper and remote table. Manual and maintenance-generated reminders use unrelated IDs and remain untouched.

### Orchestrate vehicle and generated reminders in one shared use case

A shared save/orchestration use case receives the validated vehicle, the confirmed suggestion kinds, reminder repository, notification scheduler, and vehicle repository. It saves the vehicle and reconciles only its deterministic vehicle-generated reminder IDs.

Date reminders are scheduled through the existing platform contract. Replaced date reminders are rescheduled using the same stable ID. Odometer-only reminders remain persisted but do not schedule a date notification. Clearing a target deletes and cancels only the generated reminder for that target.

### Preview suggestions before persistence

The shared domain derives suggestion descriptors from entered vehicle fields. Android renders these in the create/edit flow and requires explicit confirmation before orchestration. This separates deterministic suggestion rules from Compose and prevents silent reminder creation.

### Apply additive migrations

Local and remote vehicle tables receive nullable columns. Existing rows remain valid with null values, and no backfill is required. Sync mappers treat absent values as null.

## Risks / Trade-offs

- [Stable reminder IDs expose an ownership convention] -> Centralize ID generation in one shared function and cover it with tests.
- [Saving vehicle then reminder reconciliation can partially fail] -> Use a local SQL transaction where repository boundaries permit and keep operations retryable; do not remove existing reminders until validated inputs are ready.
- [A manually deleted generated reminder may be suggested again on later vehicle edits] -> Show the preview every time and recreate only after explicit confirmation.
- [Date fields can conflict across devices] -> Keep vehicle fields under existing last-write-wins semantics and reconcile reminders after accepted local edits.
- [Odometer reminders do not fire automatically] -> Present them as visible pending targets; background mileage detection remains out of scope.
- [Old app versions ignore new fields] -> Nullable additive columns preserve compatibility, though edits from an old client can retain only fields represented by its payload; document version expectations for demo devices.

## Migration Plan

1. Add nullable vehicle columns to SQLDelight with a versioned local migration.
2. Add a versioned Supabase migration for equivalent snake_case columns.
3. Update model, persistence, sync DTOs, and mappers before exposing UI inputs.
4. Deploy the Supabase migration before using a client build containing the new fields.
5. Rollback can stop using the fields while leaving nullable columns in place; destructive down migrations are not required.

## Open Questions

None for this iteration. Recurrence intervals and the final vehicle-card presentation are deferred to later OpenSpec changes.
