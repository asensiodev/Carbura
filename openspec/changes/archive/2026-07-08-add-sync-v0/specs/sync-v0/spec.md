## ADDED Requirements

### Requirement: Local Sync Metadata
The system SHALL track sync metadata for locally persisted vehicles, maintenance records and reminders.

#### Scenario: Local mutation is marked pending
- **WHEN** a syncable entity is created, updated, completed or deleted locally
- **THEN** the local store records that the change is pending synchronization

#### Scenario: Local mutation has update timestamp
- **WHEN** a syncable entity changes locally
- **THEN** the local store records an `updated_at` timestamp usable for conflict comparison

### Requirement: Sync Now
The system SHALL provide a shared KMP sync operation for the active authenticated family.

#### Scenario: Push pending changes
- **WHEN** sync runs with pending local changes and remote access succeeds
- **THEN** the system sends those changes to Supabase and clears their pending status

#### Scenario: Pull remote changes
- **WHEN** sync runs with remote family data available
- **THEN** the system stores the remote data locally for vehicles, maintenance records and reminders

#### Scenario: Remote failure preserves local data
- **WHEN** sync fails while pushing or pulling remote data
- **THEN** local data remains available and pending changes remain pending for retry

### Requirement: Last Write Wins Merge
The system SHALL resolve v0 sync conflicts using `updated_at` last-write-wins semantics.

#### Scenario: Remote record is newer
- **WHEN** local and remote records have the same id and the remote `updated_at` is newer
- **THEN** the remote record replaces the local synced state

#### Scenario: Local record is newer
- **WHEN** local and remote records have the same id and the local `updated_at` is newer
- **THEN** the local record remains authoritative and is pushed remotely

### Requirement: Android Sync Triggers
The Android app SHALL trigger sync v0 while the app is actively used.

#### Scenario: Sync after session ready
- **WHEN** an authenticated session becomes available after login or restore
- **THEN** the app triggers a non-blocking sync

#### Scenario: Sync on foreground
- **WHEN** the app enters foreground and the sync throttle interval has elapsed
- **THEN** the app triggers a non-blocking sync

#### Scenario: Periodic in-app sync
- **WHEN** the app remains open with an authenticated session
- **THEN** the app periodically triggers non-blocking sync

#### Scenario: Manual sync
- **WHEN** the user selects the manual sync action
- **THEN** the app triggers sync and updates the visible sync status

### Requirement: Sync Status UI
The Android app SHALL expose minimal sync status to the user.

#### Scenario: Last sync is available
- **WHEN** a sync completes successfully
- **THEN** the `Usuario` tab can show the last successful sync time

#### Scenario: Sync fails
- **WHEN** sync fails
- **THEN** the app shows a non-blocking status or error without losing local data
