## Context

The shared presentation architecture now supports KMP ViewModels and Koin. The next refinement is to make the MVI pattern explicit and complete so every feature follows the same shape.

## Goals / Non-Goals

**Goals:**
- Use immutable `UiState` for renderable screen data.
- Use `Event` for all user and lifecycle input into a ViewModel.
- Use `Effect` for one-off outputs such as snackbars or navigation requests.
- Keep contracts in separate files for readability and reuse.

**Non-Goals:**
- Introduce an external MVI framework.
- Add new UI screens or persistent data.

## Decisions

- Use `StateFlow` for state and a buffered `Channel` exposed as `Flow` for one-off effects.
- Keep events/effects feature-specific rather than forcing a generic base class too early.
- Let the ViewModel expose `onEvent(event)` as the only input method.

## Risks / Trade-offs

- More files per feature -> accepted for clarity and consistency as the project grows.
- Effects require explicit collection in Compose -> this is intentional to avoid mixing one-off actions into persistent state.
