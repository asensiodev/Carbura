## Why

Desktop Garage can now persist vehicles, but the Reminders destination remains a placeholder despite having a platform-neutral state machine and repository implementation. Migrating the existing reminder workflow makes those vehicles actionable on Desktop without duplicating domain validation or Android-specific notification behavior.

## What Changes

- Replace the Desktop Reminders placeholder with a responsive pending-reminders workspace.
- Reuse the shared Reminders ViewModel for loading, vehicle filtering, creation, completion, deletion, validation, and mutation coordination.
- Navigate users without vehicles directly to the functional Desktop Garage.
- Persist reminder mutations in the existing Desktop SQLDelight database under the shared local family.
- Generalize Desktop local-mode dependency injection so Garage and Reminders share one explicit application-level family and no-op remote synchronization policy.
- Clearly communicate that native operating-system reminder notifications are not available in this increment.
- Keep reminder editing out of scope until a shared update and provenance contract can protect source-generated reminders.

## Capabilities

### New Capabilities
- `desktop-reminders-workflow`: Persistent local reminder listing, filtering, creation, completion, deletion, and Garage navigation in the Compose Desktop application.

### Modified Capabilities

## Impact

- Adds the Reminders feature dependency and module to `app:desktop`.
- Adds Desktop-native reminder presentation while retaining shared presentation and domain rules.
- Refactors Desktop local-mode wiring from Garage-specific naming to application-level naming.
- Adds Desktop integration coverage for cross-feature vehicle/reminder persistence without changing Android behavior.
