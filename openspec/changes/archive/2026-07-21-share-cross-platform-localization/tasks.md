## 1. Resource Foundation

- [x] 1.1 Inventory Desktop user-facing literals and map equivalent copy to established Android Spanish terminology
- [x] 1.2 Configure shared Compose Multiplatform resources in the application and feature modules that need cross-platform copy
- [x] 1.3 Add Spanish shared and Desktop-specific string resources, including format arguments and accessibility descriptions

## 2. Desktop Migration

- [x] 2.1 Migrate shell navigation and destination metadata to resource-backed Spanish copy
- [x] 2.2 Migrate Garage labels, dialogs, states, snackbars, validation messages, and accessibility descriptions
- [x] 2.3 Migrate Reminders labels, dialogs, states, snackbars, validation messages, and accessibility descriptions
- [x] 2.4 Migrate Maintenance labels, dialogs, states, snackbars, validation messages, and accessibility descriptions
- [x] 2.5 Migrate local Account copy and platform-action feedback

## 3. Cross-Platform Alignment

- [x] 3.1 Update Android resource call sites where ownership moves to shared Compose resources
- [x] 3.2 Keep semantic MVI and domain state free of localized strings
- [x] 3.3 Remove remaining hardcoded English user-facing copy from Desktop presentation sources

## 4. Verification

- [x] 4.1 Add or update tests for representative Spanish labels, formatted messages, and resource-backed Desktop rendering
- [x] 4.2 Run OpenSpec strict validation for the change
- [x] 4.3 Run quality checks, tests, and Android/Desktop assembly tasks
