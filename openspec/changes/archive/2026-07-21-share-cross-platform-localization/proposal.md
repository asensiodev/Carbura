## Why

Desktop presents hardcoded English copy while Android presents the product in Spanish, creating an inconsistent experience for the same workflows. The text should be centralized now so new cross-platform screens do not continue duplicating and diverging user-facing copy.

## What Changes

- Establish Spanish as the consistent current product language across Android and Desktop.
- Introduce shared Compose Multiplatform resources for user-facing copy used by cross-platform workflows.
- Replace hardcoded Desktop labels, messages, dialogs, snackbars, validation errors, and accessibility descriptions with resource lookups.
- Align equivalent Android and Desktop terminology while retaining platform-specific copy where capabilities differ.
- Add checks that exercise representative resource resolution on Desktop.

## Capabilities

### New Capabilities
- `cross-platform-localization`: Defines consistent resource-backed product copy and language behavior across Android and Desktop.

### Modified Capabilities

## Impact

- Affects Desktop workspaces and shell navigation under `app/desktop`.
- Affects Android and shared resource ownership in the relevant application and feature modules.
- May require moving Android-only string resources into Compose Multiplatform source sets and updating generated resource imports.
- Does not change domain behavior, persistence, synchronization, or platform-specific feature availability.
