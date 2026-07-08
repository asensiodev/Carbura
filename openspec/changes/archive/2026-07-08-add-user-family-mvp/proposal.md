## Why

The MVP already authenticates users and bootstraps a personal family workspace, but the Android `Usuario` tab still feels like a placeholder. Showing real profile and family context increases demo credibility and prepares the product path for family invitations without taking on full account management yet.

## What Changes

- Show the authenticated user's display name and email in the Android `Usuario` tab when available.
- Show the current family/workspace name from the remote profile bootstrap data.
- Add clear MVP copy for deferred family member/invitation management.
- Keep sign-out available from the same tab.
- Do not implement editing, invitations, or member management in this change.

## Capabilities

### New Capabilities
- `user-family-mvp`: Covers the Android MVP user profile and family workspace summary shown after sign-in.

### Modified Capabilities

None.

## Impact

- Android app shell `User` tab UI and state wiring.
- Onboarding/auth state data surfaced to authenticated routes.
- Remote profile gateway/model usage if extra family display fields are needed.
- Android string resources for user/family copy.
