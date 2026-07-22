## ADDED Requirements

### Requirement: Local Data Detection Before First Sync
Desktop SHALL detect existing active or pending `local-family` vehicles, maintenance records, and reminders after authentication and before the first authenticated sync.

#### Scenario: Legacy local data exists
- **WHEN** a user authenticates and unresolved `local-family` records exist
- **THEN** Carbura blocks authenticated sync and presents the local-data decision with record scope information bound to the exact record IDs, versions, ownership, and deletion state

#### Scenario: No legacy local data exists
- **WHEN** a user authenticates and no unresolved `local-family` records exist
- **THEN** Carbura proceeds to authenticated-family initial sync without showing an unnecessary import decision

### Requirement: Explicit Local Data Consent
Desktop MUST NOT upload, reassign, or delete `local-family` records until the authenticated user explicitly chooses an action.

#### Scenario: User chooses import and merge
- **WHEN** the user confirms Import and merge for the authenticated family
- **THEN** Carbura transactionally adopts only the approved snapshot into that family, preserves pending synchronization semantics, records the decision, and then starts sync

#### Scenario: User chooses account data only
- **WHEN** the user chooses Use account data
- **THEN** Carbura leaves `local-family` records unchanged and excluded from authenticated sync, records the decision, and pulls authenticated-family data

#### Scenario: User cancels the decision
- **WHEN** the user cancels before choosing an import policy
- **THEN** Carbura does not sync or mutate local records and returns to a non-authenticated-data state

### Requirement: Idempotent Adoption
Local data adoption and its persisted decision MUST be idempotent across retries, crashes, and application restarts.

#### Scenario: Application stops during adoption
- **WHEN** Desktop restarts after adoption was interrupted
- **THEN** Carbura resumes or safely retries without duplicating records, changing identifiers, or requesting consent for an already committed decision

#### Scenario: Sync retries after adoption
- **WHEN** the first remote sync fails after local adoption succeeds
- **THEN** adopted records remain pending for retry and are not adopted a second time

### Requirement: Decision Is Scoped to Authenticated Family
Desktop SHALL associate local-data decisions with the authenticated user, family, and immutable local-data snapshot generation.

#### Scenario: Different account signs in
- **WHEN** unresolved `local-family` data remains and a different authenticated family is used
- **THEN** Carbura does not reuse another family's import consent and requests an applicable decision

#### Scenario: New local records exist after prior consent
- **WHEN** unresolved local record IDs or versions differ from the snapshot previously approved for import
- **THEN** Carbura does not reuse the earlier import approval and requests a new decision before uploading them

### Requirement: Adoption Preserves Relationships
Importing local data SHALL preserve vehicle, maintenance, reminder, generated-reminder, and tombstone relationships while replacing legacy family ownership and remapping identifiers only when collision safety requires it.

#### Scenario: Related local records are imported
- **WHEN** a vehicle with maintenance and reminders is adopted
- **THEN** all collision-free records retain their identifiers, any colliding legacy records receive new identifiers, and every relationship remains valid under the authenticated family

### Requirement: Collision-Safe Legacy Data
Desktop MUST prevent legacy local IDs from overwriting authenticated-family or incoming remote records during adoption or account-data pull.

#### Scenario: Imported ID already exists remotely
- **WHEN** preflight finds a legacy vehicle, maintenance, reminder, or tombstone ID that exists in the authenticated family
- **THEN** Carbura transactionally remaps the legacy ID and every dependent reference before push and preserves both logical records

#### Scenario: Excluded local ID collides with remote pull
- **WHEN** the user chose account data and an incoming authenticated-family row has the same ID as an excluded `local-family` row
- **THEN** Carbura preserves both rows through namespace-safe storage or transactional legacy remapping rather than `INSERT OR REPLACE` data loss

### Requirement: No Silent Destructive Choice
Desktop MUST NOT silently discard local data when the user chooses authenticated account data.

#### Scenario: User excludes local data from sync
- **WHEN** the user chooses Use account data
- **THEN** legacy records remain locally recoverable and the Account workspace explains that unsynchronized local data still exists
