## ADDED Requirements

### Requirement: In-App Account Deletion Entry Point
The Android app SHALL expose permanent account deletion from a visually distinct destructive section of the authenticated `Usuario` screen.

#### Scenario: User opens account management
- **WHEN** an authenticated user views the `Usuario` screen
- **THEN** the screen shows a delete-account action with copy that distinguishes deletion from sign-out

### Requirement: Explicit Irreversible Confirmation
The Android app MUST require explicit confirmation before requesting account deletion.

#### Scenario: User starts deletion
- **WHEN** the user selects the delete-account action
- **THEN** the app shows a confirmation dialog explaining permanence and the treatment of personal or shared family data without starting deletion

#### Scenario: User cancels confirmation
- **WHEN** the user dismisses or cancels the confirmation dialog
- **THEN** the account, session, local data, and navigation state remain unchanged

#### Scenario: User confirms deletion
- **WHEN** the user confirms the destructive dialog
- **THEN** the app starts exactly one account-deletion operation and disables duplicate destructive actions until it settles

### Requirement: Authenticated Server-Side Erasure
The system SHALL delete an account through a server-side Supabase operation that derives identity exclusively from the authenticated session and exposes no privileged credential to the client.

#### Scenario: Unauthenticated caller requests deletion
- **WHEN** a request has no authenticated `auth.uid()`
- **THEN** the server rejects deletion without changing public or Auth data

#### Scenario: Authenticated caller requests deletion
- **WHEN** an authenticated user invokes account deletion
- **THEN** the server handles related public data and deletes exactly that `auth.users` identity in one transaction

### Requirement: Shared Family Retention
The server SHALL preserve family-owned data when another active family member remains and SHALL delete the family aggregate when the requester is its final active member.

#### Scenario: Requester is the final family member
- **WHEN** the deleting user is the only active profile in the family
- **THEN** the server deletes the family and its cascading vehicles, custom maintenance types, maintenance records, reminders, and user profile before deleting the Auth user

#### Scenario: Another family member remains
- **WHEN** at least one other active profile belongs to the family
- **THEN** the server deletes only the requesting profile and Auth user while preserving family-owned data

#### Scenario: Family creator leaves shared family
- **WHEN** the deleting user is `families.created_by` and another active member remains
- **THEN** the server transfers `created_by` to a remaining active member before deleting the requester

### Requirement: Successful Client Cleanup
After remote deletion succeeds, the client SHALL clear the Supabase session, local synchronized account data, and known scheduled reminder notifications before returning to the unauthenticated flow.

#### Scenario: Deletion completes
- **WHEN** server-side account deletion and terminal client cleanup complete
- **THEN** the app shows the unauthenticated onboarding flow and protected navigation retains no user-specific destination

### Requirement: Convergent Deletion Outcome
After a confirmed deletion request is dispatched, the app SHALL clear local account state and return to the unauthenticated flow even when the remote commit cannot be confirmed.

#### Scenario: Deletion response is unconfirmed
- **WHEN** account deletion returns a transport or server error after dispatch
- **THEN** the app clears the local session and account cache and returns to onboarding without presenting the deletion outcome as a sign-in failure

#### Scenario: Deletion coroutine is cancelled before server completion
- **WHEN** structured concurrency cancels deletion before the remote operation completes
- **THEN** cancellation is rethrown without being converted into deletion-failure feedback

### Requirement: External Deletion Request Release Requirement
Production distribution SHALL provide a public external account-deletion request URL in the store listing.

#### Scenario: Production release is prepared
- **WHEN** Carbura is configured for production store distribution
- **THEN** release documentation identifies a functional public URL where users can request account deletion outside the installed app
