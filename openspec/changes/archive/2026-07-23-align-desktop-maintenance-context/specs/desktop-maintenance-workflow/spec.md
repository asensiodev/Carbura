## MODIFIED Requirements

### Requirement: Desktop Maintenance is vehicle-scoped
The Desktop Maintenance workspace SHALL load vehicles from the active Desktop local family and SHALL display or modify history only after one vehicle is explicitly selected or supplied by Garage navigation.

#### Scenario: User arrives from Garage
- **WHEN** the user opens Maintenance from a Garage vehicle action
- **THEN** the workspace selects that vehicle and loads its maintenance history

#### Scenario: User opens Maintenance from the sidebar
- **WHEN** the user opens Maintenance directly from Desktop navigation
- **THEN** the workspace requests an explicit vehicle selection without silently selecting the first vehicle

#### Scenario: User selects another vehicle
- **WHEN** the user selects another local-family vehicle
- **THEN** the workspace replaces the displayed history with that vehicle's records

#### Scenario: Selected vehicle is unavailable
- **WHEN** a routed or previous vehicle no longer exists in the local family
- **THEN** the workspace returns to the explicit vehicle selection state

#### Scenario: No vehicles exist
- **WHEN** the local Desktop family has no vehicles
- **THEN** the workspace explains that a vehicle is required and offers navigation to Garage
