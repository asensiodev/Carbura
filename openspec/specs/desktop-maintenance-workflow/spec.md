# desktop-maintenance-workflow Specification

## Purpose
Define the vehicle-scoped Desktop workflow for viewing, creating, planning, and deleting persistent maintenance records.

## Requirements
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

### Requirement: Desktop Maintenance displays persistent history
The Desktop application SHALL display persisted maintenance records for the selected vehicle in descending performed-date order.

#### Scenario: History contains records
- **WHEN** the selected vehicle has maintenance records
- **THEN** each record displays its type, performed date, and available odometer, cost, workshop, notes, and next-due context

#### Scenario: History is empty
- **WHEN** the selected vehicle has no maintenance records
- **THEN** the workspace displays an empty state with an action to add a record

#### Scenario: History loading fails
- **WHEN** maintenance history cannot be loaded
- **THEN** the workspace displays a recoverable error state with a retry action

### Requirement: Desktop user can create maintenance records
The Desktop Maintenance workspace SHALL provide a record form backed by shared maintenance validation and persistence orchestration.

#### Scenario: Valid record is submitted
- **WHEN** the user submits valid maintenance details
- **THEN** the record is persisted, the form resets, and refreshed history displays it

#### Scenario: Invalid record is submitted
- **WHEN** shared validation rejects the maintenance details
- **THEN** the form remains available and displays validation feedback without persisting a record

#### Scenario: Supported next-due date is provided
- **WHEN** an ITV or insurance record includes a valid next-due date
- **THEN** the record and deterministic next-due reminder are persisted atomically

### Requirement: Desktop handles future maintenance reminder offers
The Desktop Maintenance workspace SHALL expose the shared future-maintenance decision before persisting a future-dated record.

#### Scenario: User saves with reminder
- **WHEN** the user accepts the future-maintenance reminder offer
- **THEN** the record and planned reminder are persisted and the interface states that the reminder is stored in Carbura

#### Scenario: User saves without reminder
- **WHEN** the user declines the future-maintenance reminder offer but chooses to save
- **THEN** the record is persisted without a planned reminder

#### Scenario: User dismisses the offer
- **WHEN** the user dismisses the future-maintenance offer
- **THEN** neither the pending record nor planned reminder is persisted

### Requirement: Desktop user can delete maintenance records
The Desktop Maintenance workspace SHALL require confirmation before deleting a maintenance record.

#### Scenario: Deletion is confirmed
- **WHEN** the user confirms deletion
- **THEN** the record and its generated and planned reminders are soft-deleted atomically and removed from history

#### Scenario: Deletion fails
- **WHEN** persistence cannot complete deletion
- **THEN** the record remains visible and failure feedback is displayed

### Requirement: Desktop Maintenance notification capability is explicit
The Desktop Maintenance workspace SHALL distinguish stored reminder records from unavailable native operating-system alerts.

#### Scenario: Maintenance creates a reminder
- **WHEN** maintenance creation stores an automatic or planned reminder
- **THEN** success feedback does not claim that a native macOS or Windows notification was scheduled
