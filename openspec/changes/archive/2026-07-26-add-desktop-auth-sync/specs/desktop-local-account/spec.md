## MODIFIED Requirements

### Requirement: Dedicated Desktop Account Workspace
The Desktop app SHALL render a dedicated, session-aware Account workspace instead of generic migration placeholder content.

#### Scenario: Open Account destination in local mode
- **WHEN** the unauthenticated Desktop user selects Account
- **THEN** the app displays local storage context and a secure Google sign-in action

#### Scenario: Open Account destination while authenticated
- **WHEN** the authenticated Desktop user selects Account
- **THEN** the app displays user, family, synchronization, storage-origin, and sign-out context

### Requirement: Local Mode Disclosure
The Desktop Account workspace SHALL accurately distinguish local-only data, authenticated synchronized family data, and excluded legacy local data.

#### Scenario: Review unauthenticated local mode
- **WHEN** no authenticated session exists
- **THEN** the workspace states that current `local-family` data is not synchronized and offers secure sign-in

#### Scenario: Review authenticated mode
- **WHEN** an authenticated family session exists
- **THEN** the workspace identifies the active account and family and does not describe synchronized data as local-only

#### Scenario: Legacy data was excluded
- **WHEN** the user chose account data without importing existing `local-family` records
- **THEN** the workspace states that separate local-only records remain on the device and does not imply they were uploaded or deleted

### Requirement: Non-Destructive Local Account Scope
The Desktop Account workspace MUST require explicit confirmation for sign-out or data-origin decisions and MUST NOT expose unguarded local data deletion, reset, or simulated authentication controls.

#### Scenario: Review Account controls
- **WHEN** the user reviews local or authenticated Account controls
- **THEN** every control accurately represents real authentication and synchronization behavior and preserves local application data unless a separately specified destructive flow is confirmed

## ADDED Requirements

### Requirement: Desktop Account Sync Controls
The authenticated Desktop Account workspace SHALL expose current sync progress, last successful sync, recoverable failure, and a manual Sync now action.

#### Scenario: User manually synchronizes
- **WHEN** the user selects Sync now while no sync is active
- **THEN** the workspace starts shared synchronization and updates status without blocking unrelated local navigation

### Requirement: Desktop Account Secure Sign Out
The authenticated Desktop Account workspace SHALL provide sign-out with clear local-data consequences.

#### Scenario: User confirms sign-out
- **WHEN** the user confirms secure sign-out
- **THEN** credentials and protected shell state are cleared while SQLDelight records remain on the device

### Requirement: Desktop Account Storage Visibility Persists
Authentication SHALL NOT remove visibility of the exact application data directory and database path.

#### Scenario: Authenticated user reviews storage
- **WHEN** the Account workspace is visible after login
- **THEN** it continues showing the paths used by Desktop persistence and identifies which data scope is currently active
