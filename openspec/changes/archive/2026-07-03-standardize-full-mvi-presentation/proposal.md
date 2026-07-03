## Why

Carbura needs a consistent, scalable presentation pattern before more features are built. The garage flow currently uses MVI-lite; upgrading it to full MVI establishes the project-wide standard for shared KMP ViewModels.

## What Changes

- Standardize feature presentation around `UiState`, `Event` and `Effect` contracts.
- Split MVI contracts into dedicated files instead of grouping them inside the ViewModel file.
- Refactor `GarageViewModel` so it owns event processing, state reduction and one-off effects.
- Update Android Compose and tests to use the full MVI contract.

## Capabilities

### New Capabilities
- `full-mvi-presentation`: Covers the project-wide presentation contract for shared KMP ViewModels.

### Modified Capabilities

## Impact

- Affects `feature:garage` and establishes conventions for future features.
- No domain, data, Supabase or navigation behavior changes.
