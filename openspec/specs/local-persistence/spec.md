## Purpose
Define local persistence for MVP vehicles and maintenance records while preserving domain and feature boundaries.
## Requirements
### Requirement: Local Vehicle Persistence
The system SHALL persist Android MVP garage vehicles in local storage through the domain vehicle repository contract.

#### Scenario: Vehicle survives app restart
- **WHEN** a vehicle is created and the app process is restarted
- **THEN** the garage repository returns the previously created vehicle from local storage

#### Scenario: Feature modules do not own production vehicle storage
- **WHEN** production dependency injection is initialized
- **THEN** the vehicle repository implementation is provided by `core:data` instead of `feature:garage`

### Requirement: Local Maintenance Persistence
The system SHALL persist Android MVP maintenance records in local storage through the domain maintenance repository contract.

#### Scenario: Maintenance record survives app restart
- **WHEN** a maintenance record is created and the app process is restarted
- **THEN** the maintenance repository returns the previously created record for the same vehicle

#### Scenario: History remains ordered by service date
- **WHEN** persisted maintenance records are read for a vehicle
- **THEN** the returned history is ordered by service date descending

#### Scenario: Feature modules do not own production maintenance storage
- **WHEN** production dependency injection is initialized
- **THEN** the maintenance repository implementation is provided by `core:data` instead of `feature:maintenance`

### Requirement: Persistence Boundary Isolation
The system SHALL keep local database APIs isolated from domain models and feature presentation.

#### Scenario: Domain remains storage agnostic
- **WHEN** a developer inspects `core:domain` and feature ViewModels
- **THEN** they do not depend on SQLDelight, Android database APIs, or platform storage classes

### Requirement: Active-Family Local Isolation
Every local entity read, mutation, tombstone, sync acknowledgement, notification operation, and background task SHALL validate the expected active family in addition to entity identity.

#### Scenario: Stale account A action runs under account B
- **WHEN** cached UI or background work references an account A entity after account B or local mode becomes active
- **THEN** local persistence rejects the operation and account A data remains unchanged

#### Scenario: Authenticated family list is loaded
- **WHEN** Desktop displays product data for the active family
- **THEN** queries return only that family and cannot expose cached records from another account or `local-family`

#### Scenario: Sync acknowledgement is applied
- **WHEN** a remote operation marks an entity synchronized
- **THEN** the local update includes expected family ownership and cannot clear another family's pending flag by ID alone

### Requirement: Namespace-Safe Cached Identity
Local persistence MUST preserve records from different family scopes even if their external entity IDs collide.

#### Scenario: Remote ID matches excluded local ID
- **WHEN** authenticated pull receives an ID already used by an excluded `local-family` record
- **THEN** persistence stores both logical records without replacement or relationship corruption

#### Scenario: Legacy IDs are remapped
- **WHEN** migration resolves a collision by assigning new legacy IDs
- **THEN** vehicle-child, reminder-source, tombstone, and notification references update atomically

### Requirement: Account Switch Isolation
Desktop SHALL keep cached account data unavailable to other accounts while preserving it for later restoration by its owning family.

#### Scenario: User signs out and another account signs in
- **WHEN** the active family changes
- **THEN** the new account cannot view, update, delete, complete, notify, or synchronize records cached for the previous family

#### Scenario: Original account returns
- **WHEN** the original authenticated family becomes active again
- **THEN** its cached local-first data becomes available without being reclassified as `local-family`
