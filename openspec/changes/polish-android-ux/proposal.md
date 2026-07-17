## Why

Carbura's Android flows are functionally complete for the current MVP, but navigation edge cases, transient empty-state flashes, missing recoverable errors, constrained layouts, and weak accessibility semantics make the experience less reliable than the underlying local-first behavior. This polish pass is needed before the final E2E and release evidence so those tests protect a stable user journey rather than codifying known UX defects.

## What Changes

- Keep Garage as the canonical authenticated navigation root, including launches from reminder notifications and session changes.
- Make the vehicle destination show clear selected-vehicle context and align garage cards around the primary detail and odometer actions.
- Add explicit initial loading, recoverable load errors, retry actions, and duplicate-submission protection to Garage, Maintenance, and Reminders.
- Surface synchronization failures non-blockingly while making clear that local changes remain saved, and refresh visible content after successful sync.
- Add a no-vehicles prerequisite state before reminder creation and replace unbounded or ambiguous single-choice controls.
- Make long forms and content responsive to compact height, landscape, large fonts, IME insets, and expanded widths.
- Improve accessibility semantics for headings, errors, selected controls, statuses, and item-specific actions.
- Present onboarding as an optically centered product entry point with benefit-led copy that does not expose backend vendors.
- Replace the compact full-edit dialog with a full-screen vehicle form and move compact add actions into a thumb-reachable position.
- Require confirmation for irreversible delete actions while keeping frequent reversible actions lightweight.
- Align system bars and the Android dark palette with the existing Carbura visual language.
- Add focused navigation, ViewModel, Compose UI, responsive-layout, and accessibility tests for the polished flows.
- Exclude maintenance-generated reminders, background sync, realtime, conflict UI, invitations, and broad adaptive-navigation redesign from this change.

## Capabilities

### New Capabilities

- `android-ux-resilience`: Shared Android requirements for recoverable feature states, responsive forms and content, accessible semantics, and non-blocking sync feedback.

### Modified Capabilities

- `android-edge-to-edge-ui`: Extend safe-inset behavior to constrained, IME-safe, large-text, and consistent dark system-bar presentation.
- `vehicle-management`: Improve canonical navigation, selected-vehicle context, garage card hierarchy, adaptive add actions, and full-screen compact vehicle editing.
- `maintenance-history`: Add vehicle identity, current-date initialization, recoverable loading, responsive records, and field-associated validation.
- `reminders-mvp`: Add the no-vehicles prerequisite, bounded vehicle selection, recoverable loading, responsive cards, and permission-denial guidance.
- `login-onboarding`: Replace technical implementation exposure with accessible, benefit-led recovery and optically centered responsive login content.
- `sync-v0`: Surface non-blocking sync failure outside the User tab and refresh visible local content after successful pulls.

## Impact

- Android app shell and typed Navigation 3 back-stack behavior in `MainActivity` and shared routes.
- Garage, maintenance, reminders, and onboarding MVI state, events, ViewModels, strings, and Compose screens.
- Android design-system color roles and a small set of reusable screen-state/layout primitives.
- Sync status observation and feature refresh orchestration; no sync protocol or schema changes.
- New Android Compose/navigation test configuration plus focused common ViewModel tests.
- No Supabase, SQLDelight, domain model, or migration changes are expected.
