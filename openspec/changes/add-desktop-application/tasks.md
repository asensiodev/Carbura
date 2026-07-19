## 1. Desktop Module Setup

- [x] 1.1 Add the Compose Multiplatform plugin and register `app:desktop`.
- [x] 1.2 Configure the Desktop JVM entry point, application metadata, and DMG/MSI target formats.
- [x] 1.3 Add the required shared model and Compose Desktop dependencies.

## 2. Desktop Application Shell

- [x] 2.1 Implement the desktop window, fixed soft-light Carbura theme, and responsive sizing.
- [x] 2.2 Implement desktop navigation for garage, reminders, maintenance, and account destinations.
- [x] 2.3 Add clear accessible availability states for workflows that remain Android-only.
- [x] 2.4 Add shell tests for destination selection and compact navigation behavior.

## 3. Persistent Desktop Storage

- [x] 3.1 Add tests for macOS, Windows, and fallback Carbura application-data paths.
- [x] 3.2 Replace the production Desktop in-memory SQLDelight driver with a persistent file driver.
- [x] 3.3 Verify first-run directory and schema creation and existing-schema reopening.

## 4. Verification And Launch

- [x] 4.1 Run Desktop unit tests and compile the Desktop application.
- [x] 4.2 Run `qualityCheck`, strict OpenSpec validation, and `git diff --check`.
- [x] 4.3 Launch the Desktop application on macOS and confirm the process and window start successfully.
