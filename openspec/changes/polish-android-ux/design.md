## Context

Carbura's Android MVP uses typed Navigation 3 routes, Compose Material 3 screens, shared MVI ViewModels, SQLDelight local-first repositories, and a shared `SyncManager`. The core flows work, but UI state initialization and navigation are currently coordinated directly in composables. This causes a notification launch to use Reminders as the back-stack root, feature screens to flash empty content before loading, and background pulls to leave visible snapshot state stale.

The feature screens also evolved independently. Maintenance has the strongest scrollable form pattern, while Garage and Reminders contain non-scrollable long forms and crowded horizontal card layouts. Error handling is mostly validation-only, sync failure is visible only in User, and accessibility semantics rely heavily on Material defaults.

This change spans the app shell, four feature modules, the Android design system, sync status observation, and Android UI test infrastructure. It must preserve local-first mutations, the existing visual identity, current domain behavior, and the future KMP direction without introducing a new data model.

## Goals / Non-Goals

**Goals:**

- Make authenticated navigation deterministic from normal, notification, and restored-session entry points.
- Give every list feature explicit initial loading, recoverable failure, retry, content, and mutation-progress behavior.
- Keep local content usable when sync fails and make that failure visible without blocking the user.
- Refresh the active feature after a successful pull without clearing in-progress form input.
- Make vehicle context, reminder prerequisites, validation, permissions, and destructive actions understandable.
- Support compact height, landscape, large fonts, IME insets, and expanded widths with focused layout changes.
- Add accessibility semantics and automated regression coverage for the highest-risk UX paths.

**Non-Goals:**

- Maintenance-generated reminder integration or new reminder prediction rules.
- WorkManager, services, realtime, push notifications, or sync conflict resolution.
- Per-entity synchronization badges.
- Family invitations, exports, attachments, or accumulated-cost features.
- Navigation rail, two-pane layouts, or a broad design-system rewrite.
- Desktop or iOS UI implementation.

## Decisions

### Keep Garage as the canonical authenticated root

The authenticated back stack always starts with `Garage`. A reminder notification appends `Reminders` only after authentication and the initial sync attempt. Top-level navigation uses one helper that clears detail routes and restores the canonical root before selecting Garage, Reminders, or User. Sign-out clears protected destinations.

This is preferred over allowing any top-level destination to become root because existing bottom-navigation behavior assumes Garage is the home destination. A separate navigation framework is unnecessary for this correction.

The unused `CreateMaintenance` route is removed rather than preserved with misleading Garage behavior. Maintenance creation remains an action inside the selected vehicle destination.

### Use feature-specific load and mutation state with a consistent contract

Garage, Maintenance, and Reminders gain explicit initial loading and recoverable load-error state plus Retry events. Validation remains separate from persistence/load errors. Mutations expose enough pending state to disable duplicate submissions and show progress for the active action.

A generic cross-feature state machine was considered but rejected: each feature has different form and action needs, and a shared sealed hierarchy would increase coupling. Shared Compose rendering primitives are appropriate; shared presentation state is not required.

### Refresh snapshots through explicit refresh events

The app shell observes successful sync timestamps and asks the active feature to refresh through a refresh event that does not reset forms or replace existing content with an initial loading screen. This is the minimum change compatible with snapshot repository APIs.

Converting repositories to `Flow` was considered cleaner long-term, but it would turn a UX polish pass into a persistence contract migration across all platforms.

### Surface sync failure in the authenticated shell

The app shell renders a non-blocking snackbar or banner when the latest sync fails, states that local changes remain saved, and offers Retry or navigation to User. It does not label every failure as offline because auth, profile, serialization, and server failures share the same result channel. Raw technical details remain in logs or the existing diagnostic area, not primary feature copy.

### Evolve maintenance history into honest vehicle context without a new domain feature

The existing `VehicleDetail` route remains the selected-vehicle destination, but it displays the vehicle name and a compact summary of persisted vehicle fields above the maintenance history. Vehicle data is loaded by ID rather than serialized into the route so edits cannot leave stale route arguments.

This avoids creating a separate detail feature while making the current route name and user journey coherent.

### Reuse a small set of Android design-system primitives

`core:designsystem` gains only Android primitives needed repeatedly: a width-constrained screen container, loading state, empty state, recoverable error with Retry, and optionally a non-interactive status badge. Feature-specific copy and actions remain in feature modules.

The dark Material color scheme is completed for roles already used, and the XML/window/system-bar appearance is aligned with the always-dark Compose theme. A light theme remains out of scope.

### Prefer scroll, wrapping, and bounded selection over adaptive redesign

Long forms use vertical scrolling, IME padding, and safe insets. Content is centered with a maximum readable width on expanded screens. Crowded rows wrap or stack based on available width. Reminder vehicle selection becomes a bounded single-choice control rather than one button per vehicle.

This addresses phones, landscape, tablets, and large text without introducing navigation rails or two-pane layouts.

### Add Compose UI tests only for high-risk contracts

Android test dependencies and focused semantics/navigation tests are added for notification launch navigation, loading/error/retry, selected-vehicle context, no-vehicle reminder prerequisite, large text/compact constraints, and accessible error/selection semantics. Existing common ViewModel tests remain the primary state-machine coverage.

## Risks / Trade-offs

- [Refreshing after sync can disrupt user input] -> Use a dedicated refresh event that updates list data without resetting form fields or showing full-screen loading.
- [A shell sync warning may repeat during periodic failures] -> Deduplicate by failure transition/message and allow dismissal or successful sync to clear it.
- [Moving full vehicle edit from a dialog changes dismissal behavior] -> Preserve explicit discard behavior and test retained state until dismissal.
- [Vehicle lookup adds data loading to the detail screen] -> Reuse the existing vehicle repository and show history with a recoverable context error rather than serializing stale data.
- [Completing global color roles changes every screen] -> Keep existing brand colors, add only roles currently consumed, and verify contrast on the connected Android device.
- [Compose test infrastructure can increase build time] -> Keep the suite focused and avoid a full E2E in this change; the final E2E remains a separate roadmap item.
- [Current-date initialization is timezone-sensitive] -> Inject a date provider using the device local date and use deterministic values in tests.

## Migration Plan

1. Add tests and deterministic navigation/state helpers before changing visible layouts.
2. Introduce reusable Android state/layout primitives and complete theme roles.
3. Migrate one feature at a time: Garage, vehicle detail/Maintenance, Reminders, then Onboarding.
4. Add shell sync feedback and post-sync refresh after feature refresh events exist.
5. Run focused common and Android UI tests, then the full quality gate.
6. Install on the connected Android device and verify normal launch, notification launch, compact/landscape, large text, offline failure, and retry paths.

No database or remote migration is required. Rollback is code-only and leaves persisted user data unchanged.

## Open Questions

None. Maintenance reminder integration, observable repository flows, and broader adaptive navigation remain separate future changes.
