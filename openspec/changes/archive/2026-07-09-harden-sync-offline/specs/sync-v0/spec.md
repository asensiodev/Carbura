## ADDED Requirements

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
