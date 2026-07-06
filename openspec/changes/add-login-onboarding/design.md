## Context

The app now creates an injectable Supabase client and exposes `AuthGateway`, but Android still starts directly at `Garage`. The existing `feature:onboarding` module is only a route placeholder. Login must preserve the current shared presentation architecture: shared ViewModels, explicit MVI contracts, Koin DI, Navigation 3 route keys, Android-only Compose screens and no direct Supabase SDK usage from feature UI.

## Goals / Non-Goals

**Goals:**
- Add a minimal Android login gate before the garage when no session exists.
- Keep auth interactions behind `AuthGateway` and remote profile validation behind `RemoteUserProfileGateway`.
- Implement onboarding presentation with separated `UiState`, `Event` and `Effect` contracts.
- Provide a temporary sign-out path to validate session lifecycle during development.
- Compile and test without real Supabase credentials while keeping runtime errors actionable.

**Non-Goals:**
- Full account creation, family creation or invitation flows.
- Offline-first sync, remote vehicle writes or conflict handling.
- Polished production auth UX, credential manager customization or biometric auth.
- Desktop/iOS login flows.

## Decisions

### App-level session gate

Android `MainActivity` will resolve an onboarding/session ViewModel and choose the initial route from session state. This keeps feature screens navigation-agnostic while letting the app shell own Navigation 3 stack changes.

Alternative considered: let `GarageScreen` redirect if unauthenticated. Rejected because garage should not know auth/session concerns.

### Onboarding owns login MVI

`feature:onboarding` will contain shared presentation contracts and ViewModel: `OnboardingUiState`, `OnboardingEvent`, `OnboardingEffect`, `OnboardingViewModel` and a Koin module. Android Compose UI will render the login screen and forward events.

Alternative considered: put login state in `app:shared`. Rejected because login/onboarding is a feature slice and should remain testable like garage/maintenance.

### Remote profile validation after login

After sign-in, the ViewModel will call `RemoteUserProfileGateway.getProfileForUser()` for the authenticated user. The first iteration should surface whether the profile is missing or remote access fails, but it should not create remote rows automatically.

Alternative considered: skip remote read until sync. Rejected because RLS/profile validity should be validated before building sync on top.

### Temporary sign-out affordance

A simple sign-out action can live in the Android app shell or garage top area for now. It is intentionally minimal and can move to a future account/settings screen.

## Risks / Trade-offs

- Google OAuth may require Supabase redirect/deep-link configuration beyond compile-time code -> keep runtime failure visible and document manual validation steps.
- A valid auth session can exist without a `user_profiles` row -> show a recoverable missing-profile state instead of silently entering family-scoped flows.
- Session gating may briefly show loading on cold start -> prefer correctness over flashing the garage for unauthenticated users.
- Temporary sign-out UI is not final IA -> keep it isolated so it can be moved later.
