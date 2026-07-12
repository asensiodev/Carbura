## ADDED Requirements

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
