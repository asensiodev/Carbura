## Context

The Desktop application now has a persistent Garage workspace and an application-local family namespace. The shared Reminders feature already owns loading, sorting, filtering, form validation, creation, completion, deletion, mutation exclusion, and cancellation behavior. Its current Compose screen is Android-only because it also owns Android resources, lifecycle collection, notification permissions, date pickers, and swipe interactions.

Desktop persistence already binds `LocalReminderRepository` and a no-op notification scheduler. Reminder mutations are durable and notification intents remain atomic, but macOS and Windows system notifications are not implemented. Desktop must therefore present accurate local-only capability without bypassing the shared state machine or implying remote synchronization.

## Goals / Non-Goals

**Goals:**

- Provide a responsive Desktop pending-reminders list with shared vehicle filtering.
- Support manual reminder creation, completion, and confirmed deletion through shared events and use cases.
- Navigate the no-vehicle state to Desktop Garage through an explicit shell callback.
- Keep Garage and Reminders in one persistent local family and dependency graph.
- Preserve Android behavior and architecture boundaries.

**Non-Goals:**

- Editing reminders, because no shared update or reminder-provenance contract exists yet.
- Native Desktop notification scheduling, background execution, or notification permissions.
- Desktop authentication, remote family synchronization, or multi-family switching.
- Completed reminder history, undo, or generated-reminder ownership changes.
- Copying the Android Reminders screen or moving platform UI into shared domain code.

## Decisions

### Reuse the shared Reminders ViewModel unchanged

`RemindersWorkspace` will collect `RemindersViewModel` state and dispatch its existing events. Desktop Compose will own only visual layout, dialog visibility, deletion confirmation, effect-to-snackbar mapping, and shell navigation.

Direct repository access from Desktop UI was rejected because it would duplicate sorting, validation, filtering, mutation locking, and cancellation handling. Copying the Android screen was rejected because its notification permission and Android resource dependencies are platform-specific.

### Generalize Desktop local mode before adding a second feature

The Garage-specific Desktop DI module will become an application-level local-mode module. It will provide the stable local `FamilyId` and local-only `SyncManager` override through Koin. Garage and Reminders ViewModels will both resolve the same injected family rather than importing feature-specific global state.

Introducing a full session abstraction was considered but deferred until Desktop authentication exists. The Koin-provided family keeps the current boundary explicit and replaceable without inventing unused session behavior.

### Keep reminder editing out of this increment

The current repository save operation is an upsert, but there is no `UpdateReminderUseCase`, edit state, ownership check, or persisted provenance. Source-generated reminders can be reconciled by Garage or Maintenance later, so Desktop-only editing would create unsafe behavior and divergent business rules.

Edit support will require a separate shared-domain change that preserves immutable identity and source fields and defines which reminders are editable.

### Model Desktop notification availability honestly

The workspace will state that reminders are stored and visible in Carbura while operating-system notifications are unavailable. It will not display Android permission controls or claim that the local-only sync implementation uploads data.

Native notification capability should later be represented by an explicit platform capability contract rather than inferred inside shared presentation.

### Navigate through shell callbacks

`DestinationContent` will receive an `onNavigate` callback from `DesktopShell`. `NavigateToGarage` effects will invoke that callback. The workspace will not mutate global navigation state or depend on Android navigation libraries.

## Risks / Trade-offs

- [Desktop reminders do not produce system alerts] -> Show concise capability messaging and retain durable notification intents for a future scheduler.
- [Snapshot repositories do not update live across destinations] -> Reload the ViewModel when the destination is entered and use existing post-mutation refresh behavior.
- [The local family is not an authenticated tenant] -> Inject it from one Desktop local-mode module and avoid remote-sync language.
- [Manual date entry can be error-prone] -> Use the shared `CalendarDate` validation and show precise format guidance without adding platform date APIs.
- [Generated reminders can be completed or deleted] -> Preserve existing product behavior while deferring unsafe editing until provenance is modeled.
