## Requirements

### Requirement: Modular KMP project structure
The system SHALL provide a Kotlin Multiplatform project structure organized into `app`, `core`, `feature`, and `build-logic` areas.

#### Scenario: Project exposes modular areas
- **WHEN** a developer inspects the repository after bootstrap
- **THEN** the repository contains Gradle modules or module directories for application targets, core shared libraries, feature modules, and build convention logic

#### Scenario: Android remains the primary target
- **WHEN** a developer builds the bootstrapped project
- **THEN** the Android application target is available as the primary runnable target for Entrega 2

### Requirement: Convention plugins are available
The system SHALL define Gradle convention plugins in `build-logic` to centralize repeated Kotlin Multiplatform, Android, Compose, serialization, and testing configuration.

#### Scenario: Module build files use conventions
- **WHEN** a developer opens module Gradle files
- **THEN** common build configuration is applied through project convention plugins instead of repeated inline configuration in every module

### Requirement: Core modules are prepared
The system SHALL prepare core modules for model, domain, data, auth, design system, and testing responsibilities.

#### Scenario: Core responsibilities are separated
- **WHEN** a developer inspects the core module structure
- **THEN** model, domain, data, auth, design system, and testing responsibilities are represented by separate modules or clearly separated module directories

### Requirement: Data Implementations Stay In Core Data
The system SHALL keep production repository implementations and local storage wiring in `core:data` rather than feature modules.

#### Scenario: Feature module boundaries stay presentation-focused
- **WHEN** a developer inspects feature modules
- **THEN** they do not contain production local database implementations or production repository bindings

#### Scenario: Core data owns repository bindings
- **WHEN** shared dependency injection is initialized for production
- **THEN** `core:data` provides concrete repository bindings for persisted MVP data

### Requirement: Feature modules are prepared
The system SHALL prepare feature areas for onboarding, garage, maintenance, and reminders without implementing full product behavior in this change.

#### Scenario: Feature boundaries exist
- **WHEN** a developer inspects the feature module structure
- **THEN** onboarding, garage, maintenance, and reminders boundaries are present for future implementation

### Requirement: Platform integrations use common contracts
The system SHALL define platform-dependent integrations through common KMP contracts and platform-specific adapters.

#### Scenario: Native integrations do not leak into domain
- **WHEN** a developer reviews domain and use case modules
- **THEN** they do not depend directly on Android, Desktop, iOS, Credential Manager, notification, permission, or platform storage APIs

#### Scenario: Auth can vary by platform
- **WHEN** authentication is implemented in later changes
- **THEN** Android can use Credential Manager with Google ID while Desktop and future iOS can use their own adapters behind the same common contract

### Requirement: Design system base exists
The system SHALL include a minimal shared design system base for theme, visual tokens, and reusable Compose components.

#### Scenario: Android UI can consume shared design system
- **WHEN** Android UI modules are implemented after bootstrap
- **THEN** they can depend on the shared design system instead of defining local theme primitives from scratch

### Requirement: Local configuration is safe
The system SHALL include a `local.properties.example` file with placeholder configuration and SHALL NOT require committing real secrets.

#### Scenario: Developer sees required local keys
- **WHEN** a developer opens `local.properties.example`
- **THEN** the file lists placeholder keys for Supabase and Google configuration without real credentials

### Requirement: Bootstrap is verifiable
The system SHALL provide Gradle commands that verify the scaffold is valid before product features are implemented.

#### Scenario: Gradle tasks are available
- **WHEN** a developer runs `./gradlew tasks`
- **THEN** Gradle lists available tasks without configuration errors

#### Scenario: Android build is available
- **WHEN** a developer runs the Android debug build command
- **THEN** the Android target builds or reports only actionable setup errors documented in the change tasks
