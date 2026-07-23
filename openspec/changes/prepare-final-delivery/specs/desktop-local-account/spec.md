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
