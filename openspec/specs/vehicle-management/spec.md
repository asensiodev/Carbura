## ADDED Requirements

### Requirement: Garage Vehicle List
The system SHALL show the vehicles currently available in the active garage on Android.

#### Scenario: Empty garage state
- **WHEN** the active garage has no vehicles
- **THEN** the Android garage screen shows an empty state that invites the user to add a vehicle

#### Scenario: Vehicle appears after creation
- **WHEN** the user creates a valid vehicle
- **THEN** the Android garage screen shows the new vehicle in the garage list

### Requirement: Vehicle Creation Form
The system SHALL allow the Android user to create a vehicle with the minimum MVP fields required by the domain.

#### Scenario: Valid vehicle creation
- **WHEN** the user enters a non-blank vehicle name and a non-negative odometer value
- **THEN** the vehicle is saved through the domain vehicle creation use case

#### Scenario: Blank vehicle name error
- **WHEN** the user submits a blank vehicle name
- **THEN** the screen shows a validation error and the vehicle is not added to the list

#### Scenario: Negative odometer error
- **WHEN** the user submits a negative odometer value
- **THEN** the screen shows a validation error and the vehicle is not added to the list

### Requirement: Temporary In-Memory Garage Storage
The system SHALL keep garage vehicles in memory for the initial Android MVP slice until persistent storage is introduced.

#### Scenario: Vehicles remain available during the current process
- **WHEN** a vehicle is created successfully during the current app process
- **THEN** subsequent reads from the garage repository return that vehicle
