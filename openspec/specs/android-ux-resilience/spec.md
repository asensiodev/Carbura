# android-ux-resilience Specification

## Purpose
Define resilient, responsive, accessible, and mutation-safe Android presentation behavior across core feature workflows.

## Requirements
### Requirement: Recoverable Feature Loading
Android list features SHALL distinguish initial loading, loaded content, empty content, and recoverable load failure without briefly presenting empty content before the first load starts.

#### Scenario: Initial load starts
- **WHEN** Garage, Maintenance, or Reminders first enters composition
- **THEN** it presents a loading state until its first local repository read completes

#### Scenario: Load fails and retry succeeds
- **WHEN** a local repository read fails and the user selects Retry after the failure is resolved
- **THEN** the feature keeps a recoverable error visible until retry and then renders the loaded local content

### Requirement: Duplicate Action Prevention
Android forms and destructive actions SHALL prevent duplicate submissions while their current local mutation is in progress.

#### Scenario: Submit action is already running
- **WHEN** the user activates the same save or delete action again before its current execution completes
- **THEN** the system ignores the duplicate activation and exposes progress for the active action

### Requirement: Responsive Android Content
Android feature content SHALL remain usable on compact height, landscape, large font scale, expanded width, and while the software keyboard is visible without hiding primary actions.

#### Scenario: Long form with keyboard
- **WHEN** a long vehicle, maintenance, or reminder form is shown with the software keyboard visible
- **THEN** the user can scroll to every field and activate the primary action without first dismissing the keyboard

#### Scenario: Expanded screen width
- **WHEN** a feature is displayed on an expanded-width Android window
- **THEN** its main content is width-constrained and remains readable instead of stretching without bound

#### Scenario: Large text reflows actions
- **WHEN** Android font scale is increased to 200 percent
- **THEN** headings, metadata, and actions wrap or stack without clipping essential labels or controls

### Requirement: Android calendar state follows field state
Android date pickers SHALL initialize from and remain consistent with the currently displayed field value.

#### Scenario: Optional date is cleared
- **WHEN** the user clears an optional date, reopens its picker, and confirms without selecting a new date
- **THEN** the previously cleared date is not restored

#### Scenario: Date changes outside an open picker
- **WHEN** form state changes the date value before the picker is opened again
- **THEN** the picker reflects the updated value

### Requirement: Accessible Interaction Semantics
Android screens SHALL expose headings, errors, selected controls, statuses, and item actions with semantics that identify their purpose and context.

#### Scenario: Form validation fails
- **WHEN** a submitted form contains invalid input
- **THEN** the invalid field exposes error semantics and supporting text and the failure is announced without relying on color alone

#### Scenario: Item action is announced
- **WHEN** assistive technology focuses an edit or delete action for a vehicle, maintenance record, or reminder
- **THEN** the action description identifies both the action and the affected item

#### Scenario: Single-choice control is announced
- **WHEN** assistive technology focuses vehicle type or reminder vehicle selection
- **THEN** it exposes single-choice and selected-state semantics

### Requirement: Shared Android State Presentation
Repeated loading, empty, recoverable error, and constrained-content presentation SHALL use the Carbura Android design system while feature-specific text and actions remain owned by each feature.

#### Scenario: Feature renders a recoverable error
- **WHEN** a list feature cannot load local data
- **THEN** it uses the shared error presentation with feature-specific guidance and Retry action

### Requirement: Irreversible Action Confirmation
Android UI SHALL require item-specific confirmation before dispatching an irreversible delete action for a vehicle, maintenance record, or reminder.

#### Scenario: User activates delete
- **WHEN** the user selects delete for an existing item
- **THEN** the UI names the affected item and requires explicit confirmation before dispatching the delete event
