## Context

Android opens maintenance from a route that requires `vehicleId`. Desktop supports both Garage-originated navigation and a persistent Maintenance sidebar destination. The Desktop workspace currently falls back to the first loaded vehicle whenever no valid selection exists.

## Goals / Non-Goals

**Goals:**
- Make vehicle ownership explicit before Desktop history is shown or changed.
- Preserve direct navigation from a Garage vehicle.
- Retain efficient vehicle switching inside the Desktop workspace.

**Non-Goals:**
- Removing the Maintenance sidebar destination.
- Making Desktop navigation identical to Android navigation.
- Changing shared maintenance state or persistence.

## Decisions

### Clear context for sidebar navigation

Selecting Maintenance in the sidebar clears any Garage-provided route context. This distinguishes intentional direct navigation from opening a specific vehicle's history.

### Do not default to the first vehicle

Selection resolution will retain a valid current or routed vehicle but return null otherwise. The selector remains visible and an instructional empty state replaces history until the user chooses.

### Keep view models vehicle-keyed

`MaintenanceHistoryViewModel` continues to be created only after selection and remains keyed by `vehicleId` and `familyId`. This preserves the same ownership boundary used by Android.

## Risks / Trade-offs

- [Trade-off] Sidebar users perform one additional click. → The explicit choice prevents accidental cross-vehicle records.
- [Risk] Stale routed context survives later navigation. → Clear it specifically when Maintenance is selected through the sidebar.
- [Risk] Invalid or deleted routed vehicles leave a blank workspace. → Resolve invalid IDs to the explicit selection state.

## Migration Plan

1. Extract and test selection-resolution behavior.
2. Clear routed context for sidebar-originated Maintenance navigation.
3. Add the explicit unselected state and resource copy.
4. Run Desktop and full-project verification.

## Open Questions

None.
