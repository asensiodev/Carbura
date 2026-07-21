## Purpose

Define Android garage vehicle creation, selection, editing, odometer updates, and local-first behavior.
## Requirements
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

### Requirement: Garage Vehicle Selection
The system SHALL allow the Android user to select a vehicle from the garage list to open a destination that identifies the selected vehicle and exposes its maintenance history.

#### Scenario: Select vehicle from garage
- **WHEN** the user selects an existing vehicle in the garage list
- **THEN** the app opens a destination that shows the vehicle identity and its maintenance history

### Requirement: Vehicle Editing
The system SHALL allow the Android user to edit an existing vehicle's name, type, optional license plate, and current odometer while preserving its identity and active family.

#### Scenario: Save valid vehicle edits
- **WHEN** the user submits valid changes for an existing vehicle with a non-blank name and non-negative odometer
- **THEN** the system persists the edited values for the same vehicle and shows the updated vehicle in the garage

#### Scenario: Reject invalid edited name
- **WHEN** the user submits vehicle edits with a blank name
- **THEN** the system shows a validation error and does not persist the edits

#### Scenario: Reject negative edited odometer
- **WHEN** the user submits vehicle edits with a negative odometer
- **THEN** the system shows a validation error and does not persist the edits

### Requirement: Quick Odometer Update
The system SHALL provide an Android action to update an existing vehicle's current odometer without requiring the user to edit unrelated fields.

#### Scenario: Increase odometer
- **WHEN** the user submits an odometer value greater than or equal to the vehicle's current value
- **THEN** the system persists the new value and shows it for that vehicle

#### Scenario: Invalid quick odometer value
- **WHEN** the user submits a blank, non-numeric, or negative odometer value
- **THEN** the system shows a validation error and keeps the current odometer unchanged

### Requirement: Odometer Decrease Confirmation
The system MUST require explicit user confirmation before persisting an odometer value lower than the vehicle's current value.

#### Scenario: Request confirmation for lower odometer
- **WHEN** the user submits an odometer value lower than the vehicle's current value without confirming the decrease
- **THEN** the system keeps the current value unchanged and asks the user to confirm the old and proposed values

#### Scenario: Confirm lower odometer
- **WHEN** the user explicitly confirms the requested odometer decrease
- **THEN** the system persists the lower value for the vehicle

#### Scenario: Cancel lower odometer
- **WHEN** the user cancels the odometer decrease confirmation
- **THEN** the system keeps the current odometer unchanged

### Requirement: Local-First Vehicle Updates
The system SHALL make successful vehicle edits available from local storage immediately and mark them for synchronization.

#### Scenario: Edit vehicle while offline
- **WHEN** a valid vehicle edit is saved without remote connectivity
- **THEN** the updated vehicle remains available locally and pending for a later synchronization attempt

### Requirement: Optional Vehicle Due Targets
The system SHALL allow the Android user to provide optional next ITV date, insurance renewal date, and next service odometer values during vehicle creation and editing.

#### Scenario: Save valid optional due targets
- **WHEN** the user saves a valid vehicle with any supported optional due target
- **THEN** the due target is persisted with that vehicle and remains available when editing it again

#### Scenario: Reject negative next service odometer
- **WHEN** the user enters a negative next service odometer
- **THEN** the system shows a validation error and does not save the vehicle changes

#### Scenario: Clear optional due target
- **WHEN** the user removes an optional due target and saves the vehicle
- **THEN** the vehicle stores that target as empty

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
Vehicle creation and full editing SHALL remain scrollable and operable with compact height, software keyboard, and optional planning fields. Full editing SHALL use a dedicated full-screen presentation on compact Android windows instead of a constrained alert dialog.

#### Scenario: Edit all optional fields in landscape
- **WHEN** the user edits a vehicle in landscape with the keyboard visible
- **THEN** every field and save action remains reachable without clipping

#### Scenario: Edit a vehicle on a compact phone
- **WHEN** the user selects the edit action from a vehicle card
- **THEN** a full-screen editor identifies the vehicle, provides clear navigation, and keeps the save action reachable above system and IME insets

### Requirement: Adaptive Vehicle Creation Action
Garage SHALL expose vehicle creation through a thumb-reachable labeled action on compact non-empty screens while retaining an inline action in the empty state.

#### Scenario: Garage contains vehicles on a compact phone
- **WHEN** the vehicle list is displayed
- **THEN** a labeled creation action is available near the bottom end and does not obscure the final vehicle card
