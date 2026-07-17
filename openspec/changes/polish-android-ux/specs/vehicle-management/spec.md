## MODIFIED Requirements

### Requirement: Garage Vehicle Selection
The system SHALL allow the Android user to select a vehicle from the garage list to open a destination that identifies the selected vehicle and exposes its maintenance history.

#### Scenario: Select vehicle from garage
- **WHEN** the user selects an existing vehicle in the garage list
- **THEN** the app opens a destination that shows the vehicle identity and its maintenance history

## ADDED Requirements

### Requirement: Canonical Authenticated Navigation Root
The Android app SHALL keep Garage as the canonical authenticated navigation root for normal launches, reminder-notification launches, top-level tab changes, and session changes.

#### Scenario: Open reminders from notification
- **WHEN** an authenticated user launches the app from a reminder notification
- **THEN** Reminders opens above the Garage root and selecting Garage returns to Garage

#### Scenario: Sign out from a protected destination
- **WHEN** the user signs out while a top-level or vehicle destination is open
- **THEN** protected navigation entries are cleared before the next authenticated session starts

### Requirement: Vehicle Card Action Hierarchy
Garage vehicle cards SHALL prioritize opening vehicle context and updating the odometer while keeping destructive actions identifiable and less visually dominant.

#### Scenario: Vehicle card renders on compact width
- **WHEN** a vehicle has a long name or the device uses large text
- **THEN** its name, odometer, primary detail action, and odometer action remain visible without overlapping edit or delete controls

### Requirement: Responsive Vehicle Forms
Vehicle creation and full editing SHALL remain scrollable and operable with compact height, software keyboard, and optional planning fields.

#### Scenario: Edit all optional fields in landscape
- **WHEN** the user edits a vehicle in landscape with the keyboard visible
- **THEN** every field and save action remains reachable without clipping
