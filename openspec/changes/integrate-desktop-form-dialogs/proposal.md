## Why

Desktop creation and editing forms currently open as native windows whose default height can hide fields and actions until the user manually resizes them. Transactional forms must remain visually connected to the workspace and keep all content reachable at the current application size.

## What Changes

- Replace native Desktop windows for vehicle, reminder, and maintenance forms with in-app modal overlays.
- Constrain modal width and height relative to the main application window.
- Make long form content vertically scrollable while keeping the header and action footer visible.
- Preserve dismissal guards, validation, loading states, and existing form behavior.
- Keep short confirmations as focused alert dialogs.

## Capabilities

### New Capabilities
- `desktop-form-dialogs`: Defines integrated, constrained, and scrollable transactional forms for Desktop workflows.

### Modified Capabilities

## Impact

- Affects shared Desktop UI infrastructure under `app/desktop`.
- Affects vehicle, reminder, and maintenance form presentation only.
- Does not change shared MVI contracts, persistence, domain behavior, or Android UI.
