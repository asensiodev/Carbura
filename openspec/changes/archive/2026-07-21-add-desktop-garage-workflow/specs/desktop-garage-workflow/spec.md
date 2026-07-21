## ADDED Requirements

### Requirement: Desktop Garage lists persistent local vehicles
The Desktop application SHALL load and display vehicles associated with the local Desktop family from the persistent SQLDelight database.

#### Scenario: Garage contains saved vehicles
- **WHEN** the user opens Garage after vehicles have been saved
- **THEN** the workspace displays each vehicle's identity, type, and current odometer

#### Scenario: Garage has no vehicles
- **WHEN** the local Desktop family has no active vehicles
- **THEN** the workspace displays an empty state with an action to add a vehicle

#### Scenario: Vehicle loading fails
- **WHEN** the vehicle repository cannot load Garage data
- **THEN** the workspace displays an error state with a retry action

### Requirement: Desktop user can create a vehicle
The Desktop Garage SHALL provide a vehicle creation form backed by the shared vehicle validation and persistence rules.

#### Scenario: Valid vehicle is submitted
- **WHEN** the user submits a valid name, type, and non-negative odometer
- **THEN** the vehicle is persisted, the form closes, and the refreshed Garage displays it

#### Scenario: Invalid vehicle is submitted
- **WHEN** the user submits a blank name or invalid odometer
- **THEN** the form remains open and displays validation feedback without persisting a vehicle

### Requirement: Desktop user can edit a vehicle
The Desktop Garage SHALL allow the user to edit an existing vehicle's supported identity and odometer fields through the shared update workflow.

#### Scenario: Valid changes are submitted
- **WHEN** the user submits valid changes to an existing vehicle
- **THEN** the changes are persisted, the form closes, and the refreshed Garage displays the updated values

#### Scenario: Odometer is decreased
- **WHEN** the user submits an odometer below the stored value
- **THEN** the workspace requires explicit confirmation before persisting the decrease

### Requirement: Desktop user can delete a vehicle
The Desktop Garage SHALL require confirmation before deleting a vehicle and its associated local data.

#### Scenario: Deletion is confirmed
- **WHEN** the user confirms deletion of a vehicle
- **THEN** the vehicle and associated local records are soft-deleted and the vehicle is removed from the displayed Garage

#### Scenario: Deletion fails
- **WHEN** the repository cannot complete a confirmed deletion
- **THEN** the vehicle remains visible and the workspace displays failure feedback

### Requirement: Desktop Garage remains local-only
The Desktop Garage SHALL perform vehicle workflows without requiring authentication or a remote synchronization session.

#### Scenario: Desktop starts without remote credentials
- **WHEN** Carbura Desktop starts without Supabase or authenticated-user configuration
- **THEN** Garage ViewModels resolve and local vehicle CRUD remains available

#### Scenario: Desktop application restarts
- **WHEN** the user restarts the Desktop application after a successful vehicle mutation
- **THEN** the Garage reloads the previously persisted local vehicle state
