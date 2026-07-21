## ADDED Requirements

### Requirement: Shared Maintenance Search State
The system SHALL represent maintenance search input and derived visible records in immutable shared presentation state.

#### Scenario: Blank query shows complete history
- **WHEN** the maintenance search query is blank or whitespace-only
- **THEN** visible records equal the complete ordered vehicle history

#### Scenario: Search query is cleared
- **WHEN** the user clears an active maintenance search
- **THEN** the shared state restores the complete vehicle history

### Requirement: Maintenance Text Matching
The system SHALL perform trimmed case-insensitive substring matching over maintenance type, custom label, workshop, notes, performed date, and next-due date.

#### Scenario: Match any supported field
- **WHEN** a query matches at least one supported field of a maintenance record
- **THEN** that record appears in visible results

#### Scenario: Preserve source ordering
- **WHEN** multiple maintenance records match a query
- **THEN** visible results retain the source history order

### Requirement: Distinct Filtered Empty State
The system SHALL distinguish a vehicle with no maintenance records from an active search with no matches.

#### Scenario: No matching records
- **WHEN** source records exist and an active query matches none
- **THEN** the Desktop workspace shows a no-matches state with an action to clear search

#### Scenario: No source records
- **WHEN** the selected vehicle has no maintenance records
- **THEN** the existing first-record empty state remains visible

### Requirement: Search Context Lifecycle
The system SHALL retain an active query across refreshes and record mutations for the selected vehicle and start with a blank query for a newly selected vehicle context.

#### Scenario: Refresh active search
- **WHEN** history reloads while a query is active
- **THEN** the query remains active and is applied to the refreshed records

#### Scenario: Switch selected vehicle
- **WHEN** the Desktop user selects another vehicle
- **THEN** the new vehicle history starts with a blank search query
