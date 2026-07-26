## ADDED Requirements

### Requirement: Desktop Account Deletion Entry Point
The authenticated Desktop Account workspace SHALL expose permanent account deletion in a visually distinct destructive section and SHALL distinguish it from sign-out.

#### Scenario: Desktop user reviews account actions
- **WHEN** an authenticated user opens the Desktop Account workspace
- **THEN** the workspace shows separate sign-out and permanent delete-account actions

### Requirement: Desktop Explicit Irreversible Confirmation
The Desktop app MUST require explicit confirmation before dispatching permanent account deletion and MUST prevent duplicate deletion operations.

#### Scenario: Desktop user starts deletion
- **WHEN** the user selects delete account
- **THEN** Desktop explains permanence and family-data treatment without dispatching deletion

#### Scenario: Desktop user cancels deletion
- **WHEN** the confirmation is dismissed
- **THEN** account, session, local data, and navigation state remain unchanged

#### Scenario: Desktop user confirms deletion
- **WHEN** the destructive confirmation is accepted
- **THEN** Desktop dispatches exactly one deletion operation and disables duplicate account actions until terminal cleanup completes

### Requirement: Desktop Convergent Deletion Cleanup
After Desktop dispatches confirmed deletion, it SHALL clear the local session, synchronized account cache, and authenticated scope before returning to local unauthenticated mode, even when the remote commit cannot be confirmed.

#### Scenario: Desktop deletion succeeds
- **WHEN** the server confirms account deletion
- **THEN** Desktop removes local credentials and account data and returns to local mode

#### Scenario: Desktop deletion outcome is unconfirmed
- **WHEN** transport or server failure occurs after deletion dispatch
- **THEN** Desktop performs the same terminal local cleanup without claiming that remote deletion failed or succeeded
