## ADDED Requirements

### Requirement: Data Implementations Stay In Core Data
The system SHALL keep production repository implementations and local storage wiring in `core:data` rather than feature modules.

#### Scenario: Feature module boundaries stay presentation-focused
- **WHEN** a developer inspects feature modules
- **THEN** they do not contain production local database implementations or production repository bindings

#### Scenario: Core data owns repository bindings
- **WHEN** shared dependency injection is initialized for production
- **THEN** `core:data` provides concrete repository bindings for persisted MVP data
