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
