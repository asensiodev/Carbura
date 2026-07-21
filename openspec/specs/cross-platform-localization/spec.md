# cross-platform-localization Specification

## Purpose
TBD - created by archiving change share-cross-platform-localization. Update Purpose after archive.
## Requirements
### Requirement: Consistent product language
The application SHALL present equivalent user workflows in Spanish on Android and Desktop.

#### Scenario: Equivalent workflow terminology
- **WHEN** a user opens the same vehicle, reminder, or maintenance workflow on Android and Desktop
- **THEN** labels and actions use consistent Spanish product terminology

#### Scenario: Platform-specific capability explanation
- **WHEN** Desktop describes a capability that differs from Android
- **THEN** the application presents an accurate platform-specific explanation in Spanish

### Requirement: Resource-backed presentation copy
User-facing Compose presentation copy on Desktop MUST be resolved from generated resources rather than embedded English string literals.

#### Scenario: Desktop screen rendering
- **WHEN** a Desktop workspace renders labels, dialogs, messages, snackbars, or empty states
- **THEN** its copy is obtained through the application resource system

#### Scenario: Accessibility description rendering
- **WHEN** an interactive Desktop icon requires an accessibility description
- **THEN** the description is resource-backed and presented in Spanish

### Requirement: Semantic presentation state
Shared presentation and domain layers SHALL remain independent of localized display strings.

#### Scenario: Validation result localization
- **WHEN** shared logic emits a semantic validation result
- **THEN** the platform Compose boundary resolves the corresponding localized resource

### Requirement: Format argument support
The shared resource system SHALL support product copy that includes runtime values without concatenating language-specific sentence fragments in presentation code.

#### Scenario: Message with vehicle name
- **WHEN** a confirmation or result message includes a vehicle name
- **THEN** the complete sentence is resolved from a formatted resource with the vehicle name as an argument
