## Why

Supabase connectivity is in place, but the app still opens directly into the garage without an authenticated user boundary. Login should be introduced before more family-scoped features so future remote reads, sync and shared data are anchored to the active Supabase session.

## What Changes

- Add an Android-first login/onboarding entry point that is shown when no authenticated session exists.
- Gate the main navigation graph by session state: unauthenticated users see login, authenticated users enter the garage.
- Expose login state, events and one-off effects through shared MVI presentation files in the onboarding feature.
- Trigger Google/Supabase sign-in through `AuthGateway` without leaking Supabase SDK APIs into UI code.
- Add a temporary sign-out entry point after login so the session lifecycle can be validated during MVP development.
- Validate post-login connectivity by reading the authenticated user's remote profile through the existing gateway.

## Capabilities

### New Capabilities
- `login-onboarding`: Android MVP login gate, login UI state, sign-in/sign-out actions and authenticated entry into the app.

### Modified Capabilities
- `auth-session`: Session state is now consumed by app startup/navigation and login UI behavior.
- `supabase-connectivity`: The minimal remote profile read is now exercised after authentication from the login/onboarding flow.

## Impact

- Affects `feature:onboarding`, `app:shared`, `app:android`, `core:auth`, `core:data` gateway consumption and tests.
- Adds onboarding presentation contracts, ViewModel, DI and Android Compose screen.
- Requires runtime validation with configured Supabase credentials, Google OAuth redirect settings and a matching `user_profiles` row for the signed-in user.
