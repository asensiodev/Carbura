## 1. Profile And Family Data

- [x] 1.1 Extend `RemoteUserProfile` to expose the current family display name.
- [x] 1.2 Update `SupabaseUserProfileGateway` to resolve family name from the `families` table after profile lookup/bootstrap.
- [x] 1.3 Add or update data tests for profile mapping and family-name fallback behavior.

## 2. Authenticated State Wiring

- [x] 2.1 Extend `OnboardingUiState` with profile email and family name.
- [x] 2.2 Populate the new fields when loading an existing profile and when ensuring a new profile.
- [x] 2.3 Clear the new fields on sign-out/unauthenticated state.
- [x] 2.4 Update onboarding ViewModel tests for email/family-name state.

## 3. Android User Tab UI

- [x] 3.1 Pass email and family name from `CarburaApp` to `UserRoute`.
- [x] 3.2 Replace placeholder user/family copy with profile, email, family workspace, deferred invitations copy, and sign-out action.
- [x] 3.3 Add required Android string resources using existing style and Spanish copy.
- [x] 3.4 Preserve edge-to-edge safe top spacing and bottom navigation behavior.

## 4. Verification

- [x] 4.1 Run `./gradlew test assembleDebug`.
- [x] 4.2 Run `openspec validate add-user-family-mvp --strict`.
- [x] 4.3 Install/smoke test the Android `Usuario` tab when a device is available.
