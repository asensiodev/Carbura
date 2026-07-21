## Why

Carbura's shared Kotlin Multiplatform modules are JVM Desktop-compatible, but there is no executable Desktop application that users can launch on macOS or Windows. Adding a Compose Desktop entry point now validates the shared architecture as a real cross-platform product and establishes one codebase for both desktop operating systems.

## What Changes

- Add an executable Compose Desktop application that runs on macOS and Windows from shared JVM code.
- Reuse existing shared models and architectural conventions rather than creating separate macOS and Windows codebases.
- Provide a responsive desktop window shell and top-level navigation for garage, reminders, maintenance, and account destinations.
- Add Desktop-safe application configuration and persistent local storage wiring.
- Present explicit availability states for feature workflows that still require migration from Android-only Compose source sets.
- Configure native macOS and Windows distributions while keeping signing and store publication outside this initial change.
- Add Desktop smoke tests and documented Gradle run/package tasks.

## Capabilities

### New Capabilities
- `desktop-application`: Executable Compose Desktop startup, window behavior, shared feature navigation, and cross-platform distribution requirements.

### Modified Capabilities

None.

## Impact

- Adds a new `app:desktop` Gradle module and Compose Desktop application entry point.
- Affects application composition, Desktop persistence configuration, and shared module dependencies.
- Adds Compose Desktop packaging configuration for macOS DMG and Windows MSI artifacts.
- Requires macOS runtime verification locally; Windows execution and packaging verification require a Windows host or CI runner.
