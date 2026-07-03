## Context

The MVP currently has shared domain and models plus a first Android garage screen. The first garage implementation used a lightweight controller to keep delivery fast, but the product direction is KMP with Android, Desktop and future iOS support. The architecture should therefore use shared state holders and DI from `commonMain` where practical.

## Goals / Non-Goals

**Goals:**
- Use `androidx.lifecycle.ViewModel` from common KMP code for feature state holders.
- Use MVI-lite: immutable `UiState`, explicit intents and `StateFlow`.
- Use Koin modules from common code for repositories, use cases and ViewModels.
- Add Navigation 3 dependencies and route types to prepare shared navigation.

**Non-Goals:**
- Full multi-screen navigation graph implementation before a second real screen exists.
- Desktop app target or iOS SwiftUI bridge implementation.
- Replacing in-memory data with SQLDelight or Supabase.

## Decisions

- Use AndroidX Lifecycle ViewModel KMP instead of a custom controller. This aligns with official KMP guidance and makes shared ViewModels usable from Android, Desktop and future iOS wrappers.
- Use Koin 4.1.x instead of 3.x/4.2.x. Koin 4.2.x is compiled with Kotlin 2.3, while the project currently uses Kotlin 2.1; 4.1.1 keeps the DI foundation compatible without forcing an AGP/Kotlin upgrade.
- Use Navigation 3 artifacts from `androidx.navigation3` with typed route keys. Navigation graph wiring will happen when the maintenance/detail screens are added.
- Keep feature navigation callbacks at feature boundaries so features do not own app-level navigation decisions.

## Risks / Trade-offs

- Navigation 3 is newer than classic Android Navigation -> keep usage minimal until the second screen exists.
- Koin adds runtime DI -> isolate setup in a small app module and keep constructors simple/testable.
- Shared ViewModel lifecycle differs across platforms -> Android is wired first; Desktop/iOS integration remains documented and prepared but not implemented.
