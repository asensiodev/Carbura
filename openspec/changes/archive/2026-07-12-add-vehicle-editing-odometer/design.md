## Context

The shared `Vehicle` model already contains editable identity fields and `currentOdometerKm`, and `LocalVehicleRepository.saveVehicle` already performs an upsert that refreshes `updated_at` and sets `pending_sync`. Android currently exposes creation and deletion but no update interaction. The change spans shared domain behavior, local-first persistence, presentation state, and Android UI without requiring a database or Supabase schema migration.

## Goals / Non-Goals

**Goals:**

- Add one shared, testable vehicle update path for both the full edit form and quick odometer action.
- Preserve vehicle identity and family ownership while updating editable fields.
- Require explicit confirmation before saving an odometer lower than the persisted value.
- Reuse existing local-first upsert and sync behavior.
- Keep validation and confirmation decisions outside Android-specific UI code.

**Non-Goals:**

- Adding ITV, insurance, or maintenance interval fields; those belong to the proactive-reminders change.
- Automatically modifying maintenance records or kilometre reminders after an odometer update.
- Adding vehicle images, attachments, or audit history.
- Changing sync conflict resolution or the remote schema.
- Building Desktop UI for vehicle editing in this change.

## Decisions

### Use a shared update use case with an explicit decrease override

An `UpdateVehicleUseCase` will receive the current vehicle, edited values, and an explicit `allowOdometerDecrease` flag. It will validate the same field constraints as creation, preserve `id` and `familyId`, and return a distinct confirmation-required result when the proposed odometer is lower and the override is absent.

This keeps the safety rule testable in common code and lets either the full edit form or quick update show the same confirmation interaction. Putting the comparison only in Compose was rejected because another platform or caller could bypass it.

### Reuse `VehicleRepository.saveVehicle` as the persistence operation

The existing save contract and SQLDelight upsert support both insertion and update while correctly setting sync metadata. A separate repository update method would duplicate behavior without adding a meaningful domain distinction. Repository tests will prove that saving an existing ID replaces editable values and remains pending for sync.

### Extend the garage presentation flow instead of adding a new feature module

Vehicle selection and lifecycle actions already live in the garage feature. Its MVI contract will gain edit and quick-odometer state, intents, validation output, confirmation state, and success/error effects. Android dialogs or sheets will render that state without owning business rules.

### Use one reusable vehicle form for create and edit where practical

Creation and editing share most fields and validation. The Android implementation should reuse the current form structure rather than introduce visually divergent forms, while keeping mode-specific labels and submission intents clear. Quick odometer update remains a smaller focused interaction that delegates to the same update use case.

## Risks / Trade-offs

- [The broad `saveVehicle` name does not distinguish create from update] -> Keep intent explicit at the use-case layer and verify upsert behavior in repository tests.
- [Concurrent remote edits can still overwrite local edits] -> Continue using the documented sync v0 last-write-wins behavior; conflict UI is outside this change.
- [A user can intentionally confirm an incorrect lower value] -> Make the old and proposed values visible in the confirmation and require an explicit confirmation action.
- [Adding edit state can make the garage ViewModel too large] -> Follow the existing MVI structure and extract only reusable form state when it reduces duplication.
- [UI reuse could force creation and editing into an over-general abstraction] -> Prefer the smallest shared form composable and keep mode-specific orchestration in the screen.

## Migration Plan

No data migration is required. Existing vehicles are edited through the current SQLDelight upsert and synchronized through the existing vehicle sync DTO. The change can be rolled back without transforming stored data.

## Open Questions

None. Proactive reminder fields and rules are intentionally deferred to the next OpenSpec change.
