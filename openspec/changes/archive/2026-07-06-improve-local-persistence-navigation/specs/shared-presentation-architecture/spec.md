## ADDED Requirements

### Requirement: Android Navigation 3 Stack
The system SHALL render Android MVP screens through a typed Navigation 3 back stack instead of manual route state switching.

#### Scenario: Garage opens vehicle history
- **WHEN** the Android user selects a vehicle from the garage screen
- **THEN** the app pushes a typed vehicle history route onto the Navigation 3 back stack

#### Scenario: Back from vehicle history
- **WHEN** the Android user navigates back from vehicle history
- **THEN** the app pops back to the garage route using the Navigation 3 back stack

#### Scenario: Feature screens remain navigation-agnostic
- **WHEN** a feature screen needs to trigger navigation
- **THEN** it emits callbacks or effects without depending on Navigation 3 UI APIs
