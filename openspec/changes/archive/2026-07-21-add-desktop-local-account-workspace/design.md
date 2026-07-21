## Context

Desktop currently runs with `FamilyId("local-family")`, a no-op sync manager, and persistent SQLDelight storage in the operating system's application-data directory. The Account destination falls through to generic migration content even though its future authentication copy does not describe the current product behavior.

## Goals / Non-Goals

**Goals:**
- Replace the Account placeholder with a useful Desktop-native workspace.
- Make local-only behavior and storage locations explicit.
- Ensure displayed paths are exactly the paths used by the database driver.
- Guard operating-system actions and report failures without crashing.
- Keep platform calls testable without opening native applications during tests.

**Non-Goals:**
- Add authentication, profile management, cloud synchronization, or secure credential storage.
- Delete, reset, import, export, or mutate local data.
- Add runtime dependencies or shell-command fallbacks.
- Claim a numeric application version until build/runtime version metadata is unified.

## Decisions

### Expose read-only path resolvers from `core:data`

`desktopDataDirectory` and a new `desktopDatabasePath` will be public Desktop APIs. `createDesktopSqlDriver` will use the database-path resolver. Recomputing the path in `app:desktop` was rejected because UI copy could drift from persistence behavior.

### Isolate AWT behind a small platform-action interface

`DesktopPlatformActions` will expose folder and URI opening operations that return success or failure. The production implementation will use `java.awt.Desktop` only after capability checks and will catch platform failures. Direct AWT calls from composables were rejected because they are difficult to test and would mix platform effects with rendering.

### Keep Account state local to the workspace

The workspace has no domain mutations and does not need a shared ViewModel or Koin binding. It receives path metadata and platform actions through default parameters, allowing focused unit tests and keeping the local-only feature out of shared presentation modules.

### Use user-visible snackbar feedback

Successful actions do not need persistent state. Unsupported or failed actions will produce concise snackbar feedback while leaving the workspace usable.

## Risks / Trade-offs

- [Risk] `java.awt.Desktop` can be unsupported in headless or restricted environments. -> Check support before invocation and return a failure result.
- [Risk] Displaying a full local path reveals the operating-system username during screen sharing. -> Present it only inside the user's local Account workspace and do not transmit or log it.
- [Risk] Opening the data folder may encourage manual modification of the database. -> Label the file as managed by Carbura and avoid offering direct file editing or deletion.
