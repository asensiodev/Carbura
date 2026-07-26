## ADDED Requirements

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
