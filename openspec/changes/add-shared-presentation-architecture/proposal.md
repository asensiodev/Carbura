## Why

Carbura needs a presentation architecture that remains Android-first for the MVP while being prepared for Desktop and future iOS. The current garage slice uses a temporary controller; replacing it with shared ViewModel, MVI state and DI establishes the intended KMP direction before more features depend on it.

## What Changes

- Add shared Jetpack ViewModel support in KMP common code.
- Add Koin as the multiplatform dependency injection foundation.
- Refactor the garage flow from controller-style orchestration to a shared `GarageViewModel` with `UiState` and intents.
- Add Navigation 3 dependencies and common typed route definitions so the app is ready for shared Compose Multiplatform navigation.
- Keep the visible Android flow working with the existing in-memory repository.

## Capabilities

### New Capabilities
- `shared-presentation-architecture`: Covers shared KMP ViewModels, MVI screen state, DI setup and Navigation 3 readiness.

### Modified Capabilities

## Impact

- Affects Gradle version catalog, `feature:garage`, `app:android` and new shared app DI/navigation scaffolding.
- Introduces dependencies on AndroidX Lifecycle ViewModel KMP, Koin and Navigation 3.
- Does not introduce real persistence, auth, Supabase client integration or new screens.
