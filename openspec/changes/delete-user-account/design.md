## Context

Authentication is provided by Supabase Auth through `AuthGateway`, while a public `user_profiles` row links each user to one family. Family-owned vehicles, maintenance records, reminders, and custom maintenance types cascade from `families`, but `families.created_by` currently restricts deletion of its Auth user. The Android `Usuario` screen exposes sign-out only, and synchronized family data remains cached in SQLDelight with scheduled local reminder notifications.

Account deletion crosses Android presentation, shared MVI, authentication, local persistence, notifications, and privileged PostgreSQL data. The client uses only the Supabase anon key and MUST NOT receive a service-role credential.

## Goals / Non-Goals

**Goals:**
- Offer an explicit, irreversible account-deletion action from the authenticated Android user screen.
- Converge to a cleared local session after dispatch so a lost response cannot leave a remotely deleted identity presented as authenticated.
- Perform public-data and Auth-user deletion transactionally in PostgreSQL under the requesting authenticated identity.
- Preserve a shared family when other active members remain and transfer `created_by` ownership when necessary.
- Delete the family aggregate when the requester is its final active member.
- Clear the deleted account's local synchronized data, scheduled notifications, and cached session after remote deletion succeeds.
- Keep shared contracts compatible with JVM Desktop even though there is no Desktop application.

**Non-Goals:**
- Implement family invitations or member-management UI.
- Host the public web deletion-request page required for store listings.
- Add data export, deletion grace periods, recovery, or administrator deletion.
- Delete data owned by a family that still has another active member.

## Decisions

### Use an authenticated `security definer` PostgreSQL function

The client will call `delete_current_user_account()` through PostgREST. The function derives the user from `auth.uid()`, accepts no user ID, uses fully qualified relations with a restricted search path, and is executable only by `authenticated`. This avoids exposing service-role credentials or trusting client-supplied identity.

An Edge Function was considered, but it would add deployment and secret-management machinery for an operation PostgreSQL can execute atomically with the existing relational constraints.

### Handle family ownership before deleting the Auth user

The function locks the active profile and family membership set. If another active profile exists, it deletes only the requesting profile and transfers `families.created_by` to the oldest remaining member when the requester was the creator. If no other active profile exists, it deletes the family, allowing existing foreign-key cascades to remove family-owned data. It then deletes the requesting row from `auth.users` in the same transaction.

Soft deletion was considered, but it would not satisfy permanent account erasure and would retain directly identifying profile/Auth data.

### Keep deletion in the existing onboarding MVI owner

`OnboardingViewModel` already owns authenticated session transitions and remains the single app-level owner for deletion. A dedicated event and operation state distinguish deletion from sign-out and login. Success publishes the existing unauthenticated navigation effect; failure keeps the authenticated profile visible with actionable feedback.

A separate account ViewModel was considered, but it would duplicate session ownership and introduce coordination between two state machines for a single terminal auth transition.

### Clear client state only after the server transaction succeeds

`AuthGateway.deleteAccount()` invokes the RPC and clears the local Supabase session in `finally`. A domain-level local account data cleaner removes SQLDelight vehicle, maintenance, and reminder rows and cancels known reminder notifications under the same lock used by synchronization. Once dispatch starts, the ViewModel performs terminal local cleanup and publishes a clean unauthenticated state even when a transport failure makes the remote commit uncertain. Deletion diagnostics are not reused as login errors.

Clearing local data before dispatch was rejected. Clearing it after an unconfirmed response is intentional: the user has already confirmed irreversible deletion, and privacy plus local convergence take precedence over retaining a cache that may belong to a deleted identity. If the remote account remains, the user can sign in and retry; synchronization can restore retained server data.

### Use a two-step destructive UI

The `Usuario` screen presents a visually separate danger section and an outlined error-colored action. Tapping it opens a confirmation dialog that explains permanence and family-data behavior. Only the dialog confirmation dispatches deletion. While deletion is active, destructive and session actions are disabled and progress is visible.

Typed-name confirmation was considered but adds friction without materially strengthening identity verification for this single-user Google-authenticated MVP.

## Risks / Trade-offs

- [A client disconnects after PostgreSQL commits] -> Clear the session and local family cache in a non-cancellable terminal path, then show the normal login screen without misclassifying deletion as a sign-in failure.
- [Local cleanup fails after remote deletion] -> Run terminal cleanup without parent cancellation and clear the Supabase session as part of the gateway operation; keep local cleanup idempotent so startup or a repeated local operation can safely finish it.
- [Concurrent family-member deletion changes ownership] -> Lock the family row and relevant active profiles before counting and selecting a replacement owner.
- [Existing families have inconsistent creator membership] -> Select a replacement from active profiles whenever the deleted user is `created_by`; abort transactionally if no valid replacement exists while members remain.
- [Store listing lacks an external deletion URL] -> Track the URL as a release requirement; no placeholder or non-functional URL is added to the app.

## Migration Plan

1. Apply the new Supabase migration before releasing the client action.
2. Release shared gateway, local cleanup, and Android UI changes after the RPC is available.
3. Publish and configure an external account-deletion request page in Google Play Console before production distribution.
4. Rollback removes the client action first, then revokes and drops the RPC. Completed deletions are irreversible.

## Open Questions

- What production URL will host the external deletion-request flow required by the store listing?
