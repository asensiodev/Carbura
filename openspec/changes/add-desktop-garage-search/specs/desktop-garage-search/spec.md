## ADDED Requirements

### Requirement: Shared Garage Search State
The system SHALL represent Garage search input and derived visible vehicles in immutable shared presentation state.

#### Scenario: Blank search
- **WHEN** the Garage query is blank or whitespace-only
- **THEN** visible vehicles equal the complete ordered Garage list

#### Scenario: Clear search
- **WHEN** the user clears an active Garage query
- **THEN** the complete Garage list becomes visible

### Requirement: Vehicle Field Matching
The system SHALL match a trimmed query case-insensitively against vehicle name, license plate, and type.

#### Scenario: Match supported field
- **WHEN** a query matches any supported field
- **THEN** the matching vehicle appears without changing source order

### Requirement: Distinct Garage Search Empty State
The system SHALL distinguish an empty Garage from an active query with no matching vehicles.

#### Scenario: No matching vehicles
- **WHEN** vehicles exist and an active query matches none
- **THEN** Desktop shows a no-matches state with a clear-search action

#### Scenario: Empty Garage
- **WHEN** no vehicles exist
- **THEN** the existing first-vehicle empty state remains visible

### Requirement: Garage Search Retention
The system SHALL retain an active Garage query across refreshes and local vehicle mutations during the current application session.

#### Scenario: Refresh searched Garage
- **WHEN** Garage data refreshes while search is active
- **THEN** the query remains active and applies to refreshed vehicles
