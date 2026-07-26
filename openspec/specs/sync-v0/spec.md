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

### Requirement: Desktop Authenticated Sync
Desktop SHALL use the shared local-first sync operation for the active family only after authentication, profile resolution, and any required local-data decision complete.

#### Scenario: Initial Desktop sync succeeds
- **WHEN** Desktop has a valid session, resolved family, and completed data decision
- **THEN** pending family changes are pushed, remote family data is pulled, and the synchronized workspaces display the merged state

#### Scenario: Empty Desktop database uses existing mobile data
- **WHEN** the same account already has remote Android data and Desktop has no family records
- **THEN** initial sync restores those vehicles, maintenance records, reminders, and planning fields into Desktop local storage

#### Scenario: Sync starts without authenticated family
- **WHEN** no valid session or resolved profile family exists
- **THEN** Desktop does not invoke remote family sync and does not substitute `local-family` as an authenticated family

### Requirement: Desktop Sync Triggers
Desktop SHALL trigger synchronization after session readiness, on foreground with throttling, periodically while open, after successful local mutations, and through a manual action.

#### Scenario: Session becomes ready
- **WHEN** authentication and local-data onboarding complete
- **THEN** Desktop runs one initial sync attempt before presenting synchronized content

#### Scenario: Desktop returns to foreground
- **WHEN** the application becomes active and the throttle interval elapsed
- **THEN** Desktop starts a non-blocking sync

#### Scenario: Desktop remains open
- **WHEN** an authenticated Desktop session remains active
- **THEN** Desktop runs throttled periodic sync without overlapping an active sync operation

#### Scenario: Desktop mutation succeeds locally
- **WHEN** a vehicle, maintenance, or reminder mutation commits locally
- **THEN** Desktop schedules non-blocking synchronization while preserving immediate local UI feedback

#### Scenario: User requests manual sync
- **WHEN** an authenticated user selects Sync now
- **THEN** Desktop invokes shared sync and exposes current progress and result

### Requirement: Desktop Sync Status and Failure Feedback
Desktop SHALL expose synchronization progress, last success, pending local safety, and recoverable failure without blocking usable local data after startup.

#### Scenario: Initial sync fails with existing family data
- **WHEN** the first sync attempt fails but authenticated-family records already exist locally
- **THEN** Desktop keeps those records usable and explains that synchronization can be retried

#### Scenario: Background sync fails
- **WHEN** a later Desktop sync attempt fails
- **THEN** local records and pending flags remain intact and the shell or Account workspace provides non-duplicated retry feedback

#### Scenario: Sync succeeds
- **WHEN** Desktop sync completes successfully
- **THEN** the last-success status updates and visible repositories refresh without clearing active form input

### Requirement: Desktop Sync Single Flight
Desktop MUST serialize synchronization attempts across initial, foreground, periodic, mutation, and manual triggers.

#### Scenario: Trigger occurs during active sync
- **WHEN** another Desktop sync trigger fires while sync is running
- **THEN** Carbura coalesces or serializes the request without concurrent push/pull operations

### Requirement: Desktop Sync Authorization Isolation
Desktop remote synchronization MUST rely on Supabase authenticated RLS and profile-derived family ownership rather than trusting a client-provided family identifier.

#### Scenario: User requests another family's data
- **WHEN** a modified Desktop client sends another `family_id`
- **THEN** Supabase RLS rejects access unless authenticated membership authorizes that family

#### Scenario: Release security verification runs
- **WHEN** Desktop authenticated sync is prepared for release
- **THEN** tests prove one authenticated user cannot read, write, update, or delete another family's records

### Requirement: Desktop Sync Secret Redaction
Desktop synchronization diagnostics MUST redact session and backend secrets.

#### Scenario: Remote sync fails
- **WHEN** a Supabase request returns an error
- **THEN** user feedback and logs omit tokens, cookies, authorization headers, privileged keys, and full sensitive payloads

### Requirement: Version-Aware Pending Acknowledgement
The system SHALL clear pending synchronization state only when the local entity still matches the exact version uploaded to the remote store.

#### Scenario: Entity is unchanged during upload
- **WHEN** an uploaded vehicle, maintenance record, reminder, or tombstone retains the uploaded `updated_at` value until acknowledgement
- **THEN** its pending synchronization state is cleared

#### Scenario: Entity changes during upload
- **WHEN** an entity receives a newer local mutation after its older version is captured for upload but before acknowledgement
- **THEN** acknowledgement of the older upload leaves the newer local version pending for a later sync

#### Scenario: New tombstone replaces an in-flight active upload
- **WHEN** an entity is deleted locally while an older active version is being uploaded
- **THEN** acknowledgement of the active version does not clear the pending tombstone
