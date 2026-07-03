## ADDED Requirements

### Requirement: Shared Feature ViewModels
The system SHALL define feature state holders as shared KMP ViewModels when their logic is platform-independent.

#### Scenario: Garage state is exposed from common code
- **WHEN** the garage screen needs vehicles, form state or validation feedback
- **THEN** it obtains them from a shared `GarageViewModel` that exposes immutable state

### Requirement: MVI Screen State
The system SHALL model feature UI changes through immutable state and explicit user intents.

#### Scenario: User edits vehicle name
- **WHEN** the user changes the vehicle name field
- **THEN** the ViewModel receives an intent and emits updated garage UI state

#### Scenario: User submits vehicle form
- **WHEN** the user submits the vehicle creation form
- **THEN** the ViewModel validates through the domain use case and emits either the updated list or a validation error

### Requirement: Multiplatform Dependency Injection
The system SHALL provide a shared Koin module for feature dependencies that can be initialized from platform entry points.

#### Scenario: Android app starts
- **WHEN** the Android application is created
- **THEN** Koin starts with Carbura shared modules before Compose resolves feature ViewModels

### Requirement: Navigation 3 Readiness
The system SHALL define typed navigation route keys compatible with Navigation 3 for the MVP screens.

#### Scenario: Routes are referenced from common code
- **WHEN** the app needs to identify a top-level destination
- **THEN** it can use typed route definitions without depending on Android-only navigation APIs
