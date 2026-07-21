## Context

The reminders screen currently loads the active family's pending reminders and vehicles into shared presentation state, then renders one chronological list. Every reminder already has a required `VehicleId`, while `selectedVehicleId` is reserved for the reminder creation form.

## Goals / Non-Goals

**Goals:**

- Let users filter pending reminders by any combination of available vehicles.
- Preserve the unfiltered chronological overview as the default.
- Keep filtering in shared presentation state so behavior is unit testable.
- Provide a compact, accessible Android control that scales horizontally.

**Non-Goals:**

- Grouping reminders into vehicle sections.
- Changing repository queries, persistence, sync, or navigation.
- Persisting filter selection across process restarts.
- Changing the vehicle selected in the creation form.

## Decisions

- Store selected list filters as `Set<VehicleId>` in `RemindersUiState`. An empty set represents `All`, avoiding conflicting boolean state.
- Expose `visibleReminders` as a derived state projection. The source reminder list remains unchanged and retains domain-provided ordering.
- Use a dedicated filter event rather than reusing the creation form's `VehicleSelected` event.
- Toggling the final selected vehicle off returns to `All`. Selecting `All` clears the set.
- Reconcile selected IDs whenever vehicles reload so removed vehicles cannot leave stale filters.
- Render Material 3 `FilterChip`s in a horizontal `LazyRow`. Selection is communicated through container, border, and label styling; no selected checkmark icon is shown.
- Keep unavailable-vehicle reminders visible under `All` but do not synthesize an unavailable-vehicle filter.

Alternatives considered:

- Vehicle sections were rejected because they disrupt the global urgency ordering.
- Tabs were rejected because they scale poorly and do not naturally support multiple selection.
- Query-level filtering was rejected because all pending reminders are already loaded and expected list sizes are small.

## Risks / Trade-offs

- [Many vehicles create a long control row] -> Keep the row horizontally scrollable with edge padding and single-line labels.
- [Filtered empty state may be mistaken for no reminders] -> Show distinct copy and keep filters visible so users can recover.
- [A newly created reminder may be hidden by active filters] -> Preserve the user's filter and rely on the visible filter state rather than silently changing their selection.
- [Vehicle deletion leaves invalid filters] -> Intersect selected IDs with the reloaded vehicle IDs and fall back to `All` when none remain.
