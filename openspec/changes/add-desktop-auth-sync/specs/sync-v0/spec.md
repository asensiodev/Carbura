## ADDED Requirements

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
