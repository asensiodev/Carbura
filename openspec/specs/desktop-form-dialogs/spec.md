# desktop-form-dialogs Specification

## Purpose
Define integrated, reachable, and mutation-safe form dialogs for Desktop workflows.

## Requirements

### Requirement: Integrated transactional forms
Desktop SHALL present vehicle, reminder, and maintenance creation or editing forms as modal overlays integrated into the main application window.

#### Scenario: Open a Desktop form
- **WHEN** the user starts a vehicle, reminder, or maintenance create or edit action
- **THEN** the form opens over the active workspace without creating an independently resizable native window

### Requirement: Reachable form content
Desktop form dialogs SHALL keep every field reachable within the current application window without requiring window resizing.

#### Scenario: Form exceeds available height
- **WHEN** the form content is taller than the available modal body
- **THEN** the body scrolls vertically within the constrained modal surface

### Requirement: Persistent form actions
Desktop form dialogs SHALL keep the form title and action footer visible while body content scrolls.

#### Scenario: User scrolls a long form
- **WHEN** the user scrolls through fields in a long form
- **THEN** Cancel and primary save actions remain visible and usable

### Requirement: Guarded dismissal
Desktop form dialogs MUST preserve active mutation guards for all dismissal paths.

#### Scenario: Mutation is active
- **WHEN** a save mutation is active and the user presses Escape or clicks outside the modal
- **THEN** the form remains open until the mutation finishes
