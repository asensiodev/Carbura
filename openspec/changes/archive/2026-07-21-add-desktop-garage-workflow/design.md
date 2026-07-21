## Context

The Compose Desktop shell and persistent SQLDelight driver are operational, but Garage still renders a migration placeholder. Garage overview and vehicle form ViewModels live in `commonMain` and already coordinate validation, persistence, deletion, and cancellation semantics. The existing Garage Compose screen and design-system primitives are Android-only, so Desktop needs a platform-native presentation that consumes the same shared state and events.

Desktop does not yet provide authentication or Supabase configuration. The production data module declares a remote `SyncManager`, while local vehicle repositories can operate independently. This increment therefore needs explicit local-only synchronization behavior without weakening Android synchronization.

## Goals / Non-Goals

**Goals:**

- Provide persistent vehicle list, create, edit, and delete workflows on macOS and Windows from one Desktop source set.
- Reuse shared Garage ViewModels and domain use cases rather than duplicating validation or mutation rules.
- Preserve the existing Desktop visual language and adapt controls to compact window widths.
- Exercise dependency injection and SQLDelight persistence with Desktop tests.

**Non-Goals:**

- Desktop authentication, Supabase synchronization, account selection, or multi-family switching.
- Vehicle maintenance history, reminder planning fields, or native notification UI.
- Extracting the Android Garage screen or Android design system into `commonMain`.
- Changing Android Garage behavior.

## Decisions

### Use shared ViewModels behind a Desktop-native Compose workspace

`GarageWorkspace` will collect `GarageOverviewViewModel` and `VehicleFormViewModel` state and dispatch their existing events. Desktop-specific composables will render cards, dialogs, fields, and confirmations. This keeps business rules shared while avoiding Android APIs and a premature cross-platform design-system rewrite.

The alternative of direct repository calls from Desktop Compose was rejected because it would duplicate validation, cancellation, and mutation coordination already covered by common tests.

### Resolve Garage dependencies through Koin with local-only synchronization

Desktop startup will load `garageModule` alongside `dataModule` and override `SyncManager` with a Desktop no-op implementation. Both Garage ViewModels will use a stable `FamilyId("local-family")` until Desktop authentication supplies an account family.

The alternative of manually constructing ViewModels in composables was rejected because it bypasses the application's dependency graph and makes lifecycle and integration testing harder. Resolving the remote Desktop `SyncManager` was rejected because authentication and Supabase configuration are outside this increment.

### Use one create/edit dialog and explicit destructive confirmation

The workspace will show the overview continuously and open a modal form for creation or editing. The first Desktop form includes vehicle name, type, license plate where supported by the shared form state, and odometer. Deletion and odometer decreases require explicit confirmation.

Planning dates and service targets remain absent so the Desktop flow cannot imply reminder functionality that has not migrated.

### Refresh snapshots after successful mutations

The vehicle repository exposes snapshot reads rather than a reactive stream. The workspace will collect form success effects, close the form, display feedback, and dispatch `GarageOverviewEvent.Refresh`. Successful deletion already updates the overview optimistically.

### Keep UI logic testable without requiring native window automation

Desktop tests will cover Koin resolution and persistent CRUD through the same shared ViewModels and repositories. Small pure state helpers extracted for rendering decisions may receive direct tests. Full native UI automation is deferred unless Compose Desktop testing can be added without destabilizing the build.

## Risks / Trade-offs

- [Local-only records use a synthetic family ID] -> Centralize the ID in Desktop wiring so authentication can replace it in one place.
- [The declared remote sync graph can fail without auth dependencies] -> Override `SyncManager` only in the Desktop application module and test ViewModel resolution.
- [Snapshot reads do not update automatically] -> Refresh after create and edit effects and retain optimistic deletion behavior.
- [Desktop and Android presentations can visually diverge] -> Reuse shared state/events and preserve the established Desktop shell rather than copying Android layout details.
- [A modal form exposes fewer planning fields than Android] -> Clearly scope this increment to core vehicle identity and odometer CRUD; migrate reminder-aware fields with the Reminders slice.
