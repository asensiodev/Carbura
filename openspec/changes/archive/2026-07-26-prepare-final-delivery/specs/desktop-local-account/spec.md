## ADDED Requirements

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
