## ADDED Requirements

### Requirement: Non-Blocking Authenticated Sync Feedback
The Android authenticated shell SHALL surface the latest synchronization failure outside the User tab without hiding usable local content or claiming every failure is caused by lost connectivity.

#### Scenario: Background synchronization fails
- **WHEN** a foreground, periodic, post-mutation, or manual sync attempt fails after local content is available
- **THEN** the current feature remains usable and the shell states that local changes remain saved with a Retry or User action

#### Scenario: Repeated periodic failure is unchanged
- **WHEN** periodic sync repeats the same unresolved failure
- **THEN** the app avoids repeatedly interrupting the user with duplicate feedback

### Requirement: Visible Content Refresh After Sync
The Android app SHALL refresh the active feature's visible repository snapshot after a successful synchronization pull without clearing in-progress form input.

#### Scenario: Remote data arrives during successful sync
- **WHEN** sync stores newer remote entities while a feature is visible
- **THEN** that feature refreshes its displayed list and preserves any form values currently being edited
