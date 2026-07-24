## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Android calendar state follows field state
Android date pickers SHALL initialize from and remain consistent with the currently displayed field value.

#### Scenario: Optional date is cleared
- **WHEN** the user clears an optional date, reopens its picker, and confirms without selecting a new date
- **THEN** the previously cleared date is not restored

#### Scenario: Date changes outside an open picker
- **WHEN** form state changes the date value before the picker is opened again
- **THEN** the picker reflects the updated value
