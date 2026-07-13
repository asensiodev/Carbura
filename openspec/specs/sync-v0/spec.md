## Purpose
Provide functional local-first synchronization between SQLDelight and Supabase for the Android MVP, while keeping the implementation reusable from shared KMP code.
## Requirements
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
- **THEN** the app shows a loading screen and blocks authenticated content until the first sync attempt finishes

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
- **WHEN** a sync after the initial attempt fails
- **THEN** the app shows a non-blocking status or error without losing local data

### Requirement: Offline Retry Safety
The system SHALL preserve retryable local changes when remote sync fails.

#### Scenario: Failed sync keeps pending vehicle change
- **WHEN** a locally changed vehicle is pending synchronization and the remote sync operation fails
- **THEN** the local vehicle remains available and remains pending synchronization for a later retry

#### Scenario: Retry clears pending vehicle change
- **WHEN** a later sync retry succeeds after a previous remote failure
- **THEN** the previously pending vehicle change is sent remotely and cleared locally as synced

### Requirement: Tombstone Synchronization Safety
The system SHALL preserve deleted syncable entities as tombstones until they are synchronized.

#### Scenario: Deleted vehicle remains a pending tombstone after remote failure
- **WHEN** a vehicle is deleted locally and the remote sync operation fails
- **THEN** the local store keeps the deleted vehicle as a pending tombstone and excludes it from active vehicle lists

#### Scenario: Deleted vehicle tombstone syncs remotely
- **WHEN** a pending deleted vehicle tombstone is synchronized successfully
- **THEN** the remote store receives the deletion marker and the local pending status is cleared

### Requirement: Remote Restore Safety
The system SHALL restore remote family data into an empty local store during sync.

#### Scenario: Empty local store pulls remote family data
- **WHEN** sync runs for an authenticated family with no local vehicles, maintenance records, or reminders but remote data exists
- **THEN** the system stores the remote family data locally as synced data

### Requirement: Vehicle Planning Field Synchronization
The system SHALL include optional vehicle planning fields in sync v0 vehicle push, pull, and last-write-wins merge behavior.

#### Scenario: Push vehicle planning fields
- **WHEN** a locally changed vehicle with planning fields is synchronized successfully
- **THEN** the remote vehicle stores its next ITV date, insurance renewal date, and next service odometer values

#### Scenario: Pull vehicle planning fields
- **WHEN** a remote vehicle with planning fields is newer than its local version
- **THEN** the local vehicle stores the remote planning fields

#### Scenario: Clear planning field across devices
- **WHEN** a newer vehicle update clears a planning field and synchronizes
- **THEN** the cleared value replaces the older populated value on the other device
