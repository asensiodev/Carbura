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
Desktop form dialogs SHALL keep every field reachable and legible within the current application window without requiring window resizing.

#### Scenario: Form exceeds available height
- **WHEN** the form content is taller than the available modal body
- **THEN** the body scrolls vertically within the constrained modal surface

#### Scenario: Form width is constrained
- **WHEN** paired fields cannot retain usable labels and values at the available width or text scale
- **THEN** those fields stack vertically instead of clipping or overlapping

### Requirement: Persistent form actions
Desktop form dialogs SHALL keep the form title and action footer visible and usable while body content scrolls or action labels require additional width.

#### Scenario: User scrolls a long form
- **WHEN** the user scrolls through fields in a long form
- **THEN** Cancel and primary save actions remain visible and usable

#### Scenario: Footer width is constrained
- **WHEN** footer actions do not fit on one row
- **THEN** actions reflow without truncating their labels

### Requirement: Guarded dismissal
Desktop form dialogs MUST preserve active mutation guards and unsaved edit protection for all dismissal paths.

#### Scenario: Mutation is active
- **WHEN** a save mutation is active and the user presses Escape or clicks outside the modal
- **THEN** the form remains open until the mutation finishes

#### Scenario: Edit is dirty
- **WHEN** an edit form contains unsaved changes and the user requests dismissal
- **THEN** the dialog requires confirmation before discarding the draft

### Requirement: Desktop uses calendar date selection
Desktop vehicle, maintenance, and reminder forms SHALL provide an integrated calendar selector for date input and SHALL expose an explicit clear action for optional dates.

#### Scenario: User opens a Desktop date field
- **WHEN** the user activates a date field
- **THEN** an integrated calendar opens with the current valid date selected and no free-text date parsing is required

#### Scenario: User clears an optional Desktop date
- **WHEN** the user activates Clear in an optional date selector
- **THEN** the canonical field value becomes blank and remains blank when reopened
