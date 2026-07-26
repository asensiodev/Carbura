## ADDED Requirements

### Requirement: Desktop Authenticated Entry Point
The system SHALL provide a Desktop Google login entry point through shared authentication boundaries without exposing Supabase SDK types to Desktop feature ViewModels.

#### Scenario: Desktop user starts login
- **WHEN** the Desktop user selects Continue with Google
- **THEN** the shell delegates the secure OAuth operation to the auth gateway and presents semantic progress or failure state

### Requirement: Desktop Session-Aware Startup
The Desktop shell SHALL restore and validate authentication state before selecting local or authenticated synchronized content.

#### Scenario: Valid Desktop session is restored
- **WHEN** secure stored credentials refresh successfully
- **THEN** Desktop resolves the user profile and family before entering authenticated startup

#### Scenario: No Desktop session exists
- **WHEN** no secure session can be restored
- **THEN** Desktop offers Google sign-in and an explicit local-mode path without initializing authenticated sync

#### Scenario: Desktop session restoration fails recoverably
- **WHEN** credential storage or network access fails without proving credentials invalid
- **THEN** Desktop does not delete local data, does not claim sign-in succeeded, and offers retry or local mode

### Requirement: Same Identity Resolves Same Family
Desktop SHALL use the authenticated Supabase user profile to resolve the same active family used by Android for that account.

#### Scenario: Same Google account is used on both platforms
- **WHEN** Desktop authentication returns the same Supabase user as Android
- **THEN** profile resolution returns the same `family_id` and no new family is created solely because the platform is Desktop

#### Scenario: Profile is absent after first authentication
- **WHEN** an authenticated user has no profile
- **THEN** Desktop uses the existing secured profile provisioning RPC before sync and does not accept a client-selected family ID

### Requirement: Desktop Session Sign Out State
The Desktop shell SHALL clear protected navigation and synchronization state after secure sign-out.

#### Scenario: Desktop sign-out completes
- **WHEN** the auth gateway and secure session store complete sign-out
- **THEN** protected workspaces are replaced by unauthenticated or local-mode content without deleting persisted records
