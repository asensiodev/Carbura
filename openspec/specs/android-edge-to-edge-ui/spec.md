## Purpose
Define Android edge-to-edge rendering and safe system-bar behavior for Carbura screens.

## Requirements

### Requirement: Android Edge-To-Edge Rendering
The system SHALL render the Android app edge-to-edge while keeping interactive content clear of system bars.

#### Scenario: App starts with edge-to-edge enabled
- **WHEN** the Android app launches
- **THEN** the app content can draw behind transparent system bars without disabling normal screen interaction

#### Scenario: Top-level content respects safe insets
- **WHEN** a user views onboarding, garage, maintenance history, or reminders on a device with gesture navigation or display cutouts
- **THEN** primary content and actions remain visible and tappable within safe system-bar insets

### Requirement: Existing Screen Polish Preservation
The system SHALL preserve the existing Android visual language while adding edge-to-edge support.

#### Scenario: No raw spacing regressions
- **WHEN** edge-to-edge padding is added to feature screens
- **THEN** spacing uses existing design system tokens instead of raw screen-level `dp` values

#### Scenario: Existing flows remain usable
- **WHEN** the user signs in, creates a vehicle, opens maintenance history, or creates a maintenance record
- **THEN** the flows remain visually usable after edge-to-edge changes
