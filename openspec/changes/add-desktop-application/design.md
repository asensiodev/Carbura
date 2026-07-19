## Context

Carbura has Android application UI and KMP libraries with JVM Desktop targets, but no executable Desktop module. Current feature screens and design-system composables live in `androidMain`, so they cannot be imported directly by a Desktop entry point. Shared models, repositories, database code, and presentation contracts can support gradual Desktop adoption.

The initial increment must produce a real macOS application window now, preserve a path to Windows from the same source set, and avoid duplicating business logic or claiming unsupported feature parity.

## Goals / Non-Goals

**Goals:**

- Add an executable `app:desktop` Compose Desktop module.
- Use one JVM Desktop source set for macOS and Windows.
- Establish Carbura's desktop visual shell, navigation model, and window behavior.
- Persist SQLDelight data under an operating-system-appropriate application-data directory.
- Configure macOS DMG and Windows MSI distributions from the same module.
- Make unfinished feature availability explicit and accessible.

**Non-Goals:**

- Migrating every Android Compose screen in this change.
- Implementing browser OAuth callback handling or secure desktop credential storage.
- Implementing native desktop notifications.
- Signing, notarizing, or publishing installers.
- Verifying Windows runtime behavior from macOS.

## Decisions

### Add a dedicated JVM application module

`app:desktop` will apply Kotlin JVM and JetBrains Compose plugins and depend only on modules that are safe for the Desktop target. This keeps executable and packaging concerns out of KMP libraries.

A second macOS-specific application was rejected because Compose Desktop can produce macOS and Windows distributions from one JVM codebase.

### Build a desktop-first shell instead of copying Android screens

The first window will use a persistent navigation rail, desktop spacing, restrained pale-blue surfaces, and clear content states. Android screens will not be copied into Desktop because that would create parallel UI implementations before common presentation primitives are extracted.

### Keep feature availability honest

Garage, reminders, maintenance, and account destinations will be visible so the information architecture is established. Destinations whose interactive UI remains Android-only will show concise migration status rather than non-functional controls.

### Use persistent platform-aware database storage

Desktop SQLDelight will use a file database under `~/Library/Application Support/Carbura` on macOS, `%APPDATA%/Carbura` on Windows, and the standard user-data directory on Linux. Tests may continue to use in-memory drivers.

### Configure host-independent package declarations

Compose Desktop will declare DMG and MSI target formats. A host builds and validates its native package; CI or a Windows machine will later verify MSI output.

## Risks / Trade-offs

- [The first Desktop release has less feature parity than Android] -> Show explicit status and follow with incremental extraction of common Compose UI.
- [Desktop persistence changes existing in-memory assumptions] -> Keep the production driver persistent and tests explicitly in-memory.
- [Native packaging differs by operating system] -> Configure both formats but only claim runtime verification on the current host.
- [Authentication is not yet usable on Desktop] -> Keep OAuth and secure token storage out of this shell change and expose account availability accurately.
