## ADDED Requirements

### Requirement: Pending Reminder Vehicle Filtering
The system SHALL allow the Android user to filter pending reminders by one or more vehicles without changing reminder persistence or ordering.

#### Scenario: All reminders are selected by default
- **WHEN** the reminders screen loads with no vehicle filters selected
- **THEN** the screen shows all pending reminders in the existing due-target order

#### Scenario: Filter reminders by one vehicle
- **WHEN** the user selects one vehicle filter
- **THEN** the screen shows only pending reminders associated with that vehicle

#### Scenario: Filter reminders by multiple vehicles
- **WHEN** the user selects multiple vehicle filters
- **THEN** the screen shows pending reminders associated with any selected vehicle in the existing due-target order

#### Scenario: Clear vehicle filters
- **WHEN** the user selects the exclusive `All` filter or deselects the final selected vehicle
- **THEN** the screen clears vehicle filtering and shows all pending reminders

#### Scenario: Selected vehicle becomes unavailable
- **WHEN** vehicle data reloads without a previously selected filter vehicle
- **THEN** the screen removes that vehicle from the filter selection and falls back to `All` when no valid selections remain

#### Scenario: No reminders match selected vehicles
- **WHEN** pending reminders exist but none are associated with the selected vehicle filters
- **THEN** the screen shows a filter-specific empty state without presenting the family-wide empty state

### Requirement: Accessible Vehicle Filter Controls
The system SHALL present vehicle filters as horizontally scrollable rounded controls whose selected state does not depend on a checkmark icon.

#### Scenario: Vehicle filters overflow horizontally
- **WHEN** the available vehicle filter controls exceed the screen width
- **THEN** the user can scroll horizontally to reach every filter

#### Scenario: Filter selection is communicated
- **WHEN** a filter is selected
- **THEN** its visual styling and accessibility semantics communicate the selected state without displaying a selected checkmark icon
