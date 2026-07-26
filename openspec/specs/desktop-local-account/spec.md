# desktop-local-account Specification

## Purpose
Define the Desktop account workspace, its local-mode disclosure, storage visibility, responsive actions, and safe native integrations.
## Requirements
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

### Requirement: Storage Location Visibility
The Desktop Account workspace SHALL keep the exact application data directory and database file path available through explicit storage details without giving technical paths equal prominence to account identity and actions.

#### Scenario: Review local storage summary
- **WHEN** the Desktop Account workspace is visible
- **THEN** the user sees a compact explanation that Carbura stores data on this computer and can open the data folder

#### Scenario: Review exact local storage paths
- **WHEN** the user requests storage details
- **THEN** the displayed directory and database path match the SQLDelight driver's configured location

### Requirement: Account actions have responsive priority
The Desktop Account workspace SHALL prioritize synchronized account identity and actions over secondary storage diagnostics.

#### Scenario: Authenticated Account is shown at constrained width
- **WHEN** the Account workspace is narrower than the combined width of account actions
- **THEN** Sync and Sign out remain fully visible and usable without label clipping

#### Scenario: Account and storage are shown together
- **WHEN** an authenticated account and local storage information are both available
- **THEN** the account occupies the primary full-width region and storage appears as a compact secondary section

### Requirement: Safe Native Platform Actions
The Desktop Account workspace SHALL allow the user to request opening the local data directory and the Carbura project website through supported operating-system actions.

#### Scenario: Open supported data directory
- **WHEN** the user requests the data folder and the operating system supports folder opening
- **THEN** the app delegates the exact application data directory to the native platform action

#### Scenario: Open supported project website
- **WHEN** the user requests the project website and the operating system supports URI browsing
- **THEN** the app delegates the Carbura HTTPS project URI to the native platform action

#### Scenario: Native action unavailable
- **WHEN** a requested native action is unsupported or fails
- **THEN** the Account workspace remains available and shows user-visible failure feedback

### Requirement: Non-Destructive Local Account Scope
The Desktop Account workspace MUST require explicit confirmation for sign-out or data-origin decisions and MUST NOT expose unguarded local data deletion, reset, or simulated authentication controls.

#### Scenario: Review Account controls
- **WHEN** the user reviews local or authenticated Account controls
- **THEN** every control accurately represents real authentication and synchronization behavior and preserves local application data unless a separately specified destructive flow is confirmed

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

### Requirement: Unconfigured Desktop Local Mode
Desktop SHALL keep Garage, Maintenance, Reminders, and Account usable in local mode when Supabase public configuration is absent, without constructing or validating an authenticated remote client.

#### Scenario: Desktop starts without Supabase configuration
- **WHEN** both public Supabase settings are blank
- **THEN** Desktop opens local content and local mutations persist without resolving `SupabaseClient`, `AuthGateway`, `RemoteSyncDataSource`, or authenticated `SyncManager`

### Requirement: Complete Authenticated Desktop Account Actions
The authenticated Desktop Account workspace SHALL expose synchronization, retry, sign-out, and permanent account deletion while preserving storage-path visibility.

#### Scenario: Authenticated user opens Account
- **WHEN** the Desktop session and family are active
- **THEN** Account shows identity, family, synchronization state, storage location, sign-out, and delete-account actions

#### Scenario: Legacy profile contains JSON quote artifacts
- **WHEN** Desktop loads an identity persisted by an older metadata parser
- **THEN** Account presents readable identity without serialization quotation marks or technical family identifiers

#### Scenario: Authenticated Desktop restarts without network
- **WHEN** Keychain restores a session whose exact user and family scope were previously validated but the remote profile is temporarily unavailable
- **THEN** Desktop opens that family's cached local data, reports synchronization as retryable, and does not expose another account or `local-family`

#### Scenario: User retries synchronization from Account
- **WHEN** an authenticated user retries a failed synchronization from the Account workspace
- **THEN** the shell remains on Account while progress and the next result are presented
