## ADDED Requirements

### Requirement: IME-Safe And Width-Constrained Content
Android forms and primary screen content SHALL respect system and IME insets and SHALL use a readable maximum width on expanded windows.

#### Scenario: Software keyboard opens
- **WHEN** the keyboard opens over a long form
- **THEN** the focused input and primary action remain reachable by scrolling within safe insets

#### Scenario: Screen expands beyond phone width
- **WHEN** the app is displayed on a tablet or expanded window
- **THEN** primary content is centered within a bounded readable width

### Requirement: Consistent Dark System Presentation
The Android window theme, Compose color roles, and system-bar icon appearance SHALL remain consistent with Carbura's dark visual language from startup through normal use.

#### Scenario: App starts while system uses light mode
- **WHEN** Carbura launches on a device whose system theme is light
- **THEN** startup background and system-bar icons retain sufficient contrast with Carbura's dark surfaces
