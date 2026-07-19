## Why

Carbura creates a persistent Supabase identity and family-scoped personal data but provides no way for users to permanently delete that account. Adding an in-app deletion flow is necessary for user control, privacy compliance, and store account-deletion requirements.

## What Changes

- Add a destructive account-management section to the Android `Usuario` screen.
- Require explicit confirmation before starting irreversible deletion and expose progress and retryable failure feedback.
- Add a shared authentication gateway operation that invokes an authenticated, server-side Supabase deletion function.
- Delete only the requesting member when a family has other active members; delete the family aggregate when the requester is its final active member.
- Delete the Supabase Auth user only after related public data has been handled successfully.
- Return the app to the unauthenticated flow after a confirmed request, including when a lost response leaves the remote commit uncertain.
- Document the external account-deletion request URL as a release configuration requirement; hosting that public page is outside this repository.

## Capabilities

### New Capabilities
- `account-deletion`: In-app confirmation, secure server-side account erasure, shared-family retention rules, and post-deletion session behavior.

### Modified Capabilities

## Impact

- Android app shell and `Usuario` UI, strings, state, and tests.
- Shared `AuthGateway` and onboarding MVI contracts and implementations.
- Supabase PostgreSQL migrations, Auth schema interaction, family ownership, and RLS-safe RPC access.
- Release operations must configure and publish an external deletion-request URL for store listing compliance.
