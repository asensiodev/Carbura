## Requirements

### Requirement: Shared MVP Entities
The system SHALL define shared KMP models for the MVP garage domain: family, user profile, vehicle, maintenance type, maintenance record and reminder.

#### Scenario: Domain entities are available from common code
- **WHEN** common KMP code needs to represent the garage domain
- **THEN** it can use typed models from `core:model` without depending on Android, SQLDelight or Supabase

#### Scenario: Vehicle carries family ownership
- **WHEN** a vehicle is represented in the domain model
- **THEN** it includes a typed `FamilyId` and `VehicleId`

### Requirement: Typed Identifiers
The system SHALL use typed identifiers for family, user profile, vehicle, maintenance type, maintenance record and reminder IDs.

#### Scenario: ID types are not interchangeable
- **WHEN** domain code expects a vehicle identifier
- **THEN** a family, maintenance or reminder identifier cannot be passed accidentally without explicit conversion

### Requirement: Vehicle Creation Validation
The system SHALL validate vehicle creation before saving it through a repository.

#### Scenario: Valid vehicle is saved
- **WHEN** a vehicle is created with family, id, non-blank name, valid type and non-negative odometer
- **THEN** the vehicle repository receives the vehicle and the use case returns success

#### Scenario: Blank vehicle name is rejected
- **WHEN** a vehicle is created with a blank name
- **THEN** the use case returns a validation error and does not save it

#### Scenario: Negative odometer is rejected
- **WHEN** a vehicle is created with a negative odometer
- **THEN** the use case returns a validation error and does not save it

### Requirement: Maintenance Record Validation
The system SHALL validate maintenance record creation before saving it through a repository.

#### Scenario: Valid maintenance record is saved
- **WHEN** a maintenance record has family, vehicle, type, date and valid optional odometer/cost
- **THEN** the maintenance repository receives the record and the use case returns success

#### Scenario: Invalid maintenance odometer is rejected
- **WHEN** a maintenance record has a negative odometer
- **THEN** the use case returns a validation error and does not save it

#### Scenario: Invalid maintenance cost is rejected
- **WHEN** a maintenance record has a negative cost
- **THEN** the use case returns a validation error and does not save it

### Requirement: Vehicle History Query
The system SHALL expose a use case for retrieving maintenance history for a vehicle in newest-first order.

#### Scenario: History is newest first
- **WHEN** a vehicle history is requested
- **THEN** the returned maintenance records are ordered by performed date descending

### Requirement: Automatic Reminder Creation
The system SHALL create a basic reminder for ITV or insurance maintenance records when a due date is provided.

#### Scenario: ITV due date creates reminder
- **WHEN** an ITV maintenance record includes a next due date
- **THEN** the reminder use case creates a reminder with 30 days notice by default

#### Scenario: Insurance due date creates reminder
- **WHEN** an insurance maintenance record includes a next due date
- **THEN** the reminder use case creates a reminder with 30 days notice by default

#### Scenario: Maintenance without due date skips reminder
- **WHEN** a maintenance record has no next due date
- **THEN** no automatic reminder is created

#### Scenario: Non-reminder maintenance skips reminder
- **WHEN** a maintenance record type is not ITV or insurance
- **THEN** no automatic reminder is created automatically
