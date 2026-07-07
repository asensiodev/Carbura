## Context

The Android app already authenticates with Supabase/Google and ensures every authenticated user has a remote `user_profiles` row attached to a personal `families` workspace. `OnboardingViewModel` keeps the active authenticated state and currently exposes only `displayName` to the app shell, while the `Usuario` tab is implemented in `MainActivity` with placeholder family copy.

## Goals / Non-Goals

**Goals:**
- Surface the authenticated user's display name, email, and current family name in the Android `Usuario` tab.
- Keep the feature read-only for the MVP.
- Reuse the existing remote profile bootstrap flow and Koin/app-shell wiring.
- Preserve the existing bottom tab navigation and sign-out behavior.

**Non-Goals:**
- Editing profile fields.
- Creating, renaming, joining, or deleting families.
- Inviting members or displaying a member list.
- Adding offline persistence for remote profile/family metadata.

## Decisions

- Extend `RemoteUserProfile` with `familyName` and fetch it through `SupabaseUserProfileGateway` after resolving the profile's `family_id`.
  - Rationale: avoids changing the already deployed `ensure_user_profile` RPC signature and uses the existing `families` table/RLS.
  - Alternative considered: change the RPC to return `family_name`; this would require another migration and more runtime coordination for little MVP value.
- Extend `OnboardingUiState` with `email` and `familyName` rather than adding a dedicated user feature ViewModel.
  - Rationale: the onboarding/auth state already owns profile bootstrap and is available to the app shell; adding another ViewModel would duplicate loading state for a read-only summary.
  - Alternative considered: create `feature:user`; this is better once editing/invitations are implemented.
- Keep user/family UI in `app/android` for this MVP slice.
  - Rationale: the tab is app-shell level and still small. It can be extracted later if it grows.

## Risks / Trade-offs

- Remote family lookup fails after profile lookup -> fall back to a generic family label and keep the user authenticated.
- UI remains read-only -> make deferred family management explicit in copy so the limitation is understandable in the demo.
- App-shell `MainActivity` continues to hold simple user UI -> acceptable for MVP, but should be extracted before adding forms or invitations.
