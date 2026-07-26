# desktop-application Specification

## Purpose
Define the executable Compose Desktop application, navigation shell, persistent local storage, and native package configuration.
## Requirements
### Requirement: Desktop application is executable from shared JVM code
The system SHALL provide one Compose Desktop application entry point that runs on supported macOS and Windows JVM hosts.

#### Scenario: Developer launches on macOS
- **WHEN** a developer runs the Desktop Gradle run task on macOS
- **THEN** Carbura opens as a native desktop window without requiring the Android application

#### Scenario: Desktop code targets Windows
- **WHEN** the Desktop application is compiled or packaged on Windows
- **THEN** it uses the same application source set and business models as macOS

### Requirement: Desktop shell establishes Carbura navigation
The Desktop application SHALL expose garage, reminders, maintenance, and account destinations through a desktop-appropriate navigation shell.

#### Scenario: User changes destination
- **WHEN** the user selects a destination from the desktop navigation
- **THEN** the main content region updates while the application window remains open

#### Scenario: Window is compact
- **WHEN** the window width cannot accommodate the expanded navigation rail
- **THEN** the shell remains usable with compact labels and without clipped primary content

### Requirement: Desktop feature availability is explicit
The Desktop application SHALL clearly identify workflows that have not yet migrated from Android-only Compose UI.

#### Scenario: User opens an unavailable workflow
- **WHEN** the selected destination is not interactive on Desktop yet
- **THEN** the application shows a clear availability state and does not present controls that silently fail

### Requirement: Desktop local data is persistent
The production Desktop application SHALL store its SQLDelight database in an operating-system-appropriate persistent application-data directory.

#### Scenario: Application restarts
- **WHEN** the Desktop application closes and starts again
- **THEN** previously committed local data remains available from the same database file

#### Scenario: Data directory does not exist
- **WHEN** Desktop persistence initializes for the first time
- **THEN** the application creates the required Carbura data directory before opening the database

### Requirement: Native desktop distributions are configured
The Desktop module SHALL declare native package formats for macOS and Windows from the same application definition.

#### Scenario: macOS package is requested
- **WHEN** the macOS packaging task runs on macOS
- **THEN** the build produces a Carbura DMG distribution without requiring Windows-specific source code

#### Scenario: Windows package is requested
- **WHEN** the Windows packaging task runs on Windows
- **THEN** the build produces a Carbura MSI distribution without requiring macOS-specific source code

### Requirement: Desktop production presentation
The Desktop application SHALL present implemented workflows as production functionality and SHALL NOT display preview or migration-placeholder labeling in the final delivery.

#### Scenario: User opens the Desktop shell
- **WHEN** the final Desktop application starts
- **THEN** navigation and feature content contain no Desktop preview banner or supported-system promotional placeholder

#### Scenario: Installed app opens on a constrained display
- **WHEN** the packaged application starts on a display smaller than its preferred window size
- **THEN** its initial and minimum window bounds fit within the usable display area

### Requirement: Native package evidence
The final delivery SHALL generate each native Desktop package on a host that supplies the required `jpackage` tool and SHALL not equate a JVM jar with a native distribution.

#### Scenario: macOS package task runs with a full JDK
- **WHEN** the DMG packaging task executes on macOS
- **THEN** it produces an installable Carbura DMG for artifact inspection and manual acceptance

#### Scenario: Packaging tool is unavailable
- **WHEN** the selected JDK lacks `jpackage`
- **THEN** packaging remains incomplete and the delivery does not claim that a native artifact was verified
