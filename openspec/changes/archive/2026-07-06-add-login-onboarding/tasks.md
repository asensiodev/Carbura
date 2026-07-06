## 1. Presentation Contract

- [x] 1.1 Replace the onboarding placeholder with shared `UiState`, `Event` and `Effect` contract files.
- [x] 1.2 Implement an `OnboardingViewModel` that loads current session, handles Google login, validates remote profile and handles sign out.
- [x] 1.3 Add onboarding Koin bindings and include them in shared app DI.

## 2. Tests

- [x] 2.1 Add ViewModel tests for unauthenticated startup, authenticated startup and loading transitions.
- [x] 2.2 Add ViewModel tests for login success, login failure and missing remote profile.
- [x] 2.3 Add ViewModel tests for sign out returning to unauthenticated state and emitting effects with Turbine.

## 3. Android UI and Navigation

- [x] 3.1 Add an Android Compose login screen that uses design tokens and string resources without raw `dp` in feature screens.
- [x] 3.2 Gate `MainActivity` Navigation 3 content by onboarding session state and route authenticated users to garage.
- [x] 3.3 Add a temporary sign-out action from authenticated UI that clears protected navigation state.

## 4. Runtime Guidance and Verification

- [x] 4.1 Document the local runtime validation steps for Supabase URL, anon key, Google OAuth redirect and `user_profiles` row.
- [x] 4.2 Run `./gradlew test`.
- [x] 4.3 Run `./gradlew assembleDebug`.
- [x] 4.4 Run `git diff --check` and inspect working tree status.
