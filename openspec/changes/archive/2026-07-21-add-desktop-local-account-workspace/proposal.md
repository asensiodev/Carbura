## Why

Account is the only Desktop destination still rendered as a migration placeholder, leaving users without a clear explanation of local mode or access to their application data location. A focused local workspace can provide immediate value without pretending that Desktop authentication or cloud synchronization already exists.

## What Changes

- Replace the Desktop Account placeholder with a dedicated local Account workspace.
- Explain that Desktop currently stores data locally without sign-in or cloud synchronization.
- Display the exact application data directory and database file location used by the SQLDelight driver.
- Provide guarded native actions to open the data directory and Carbura project website.
- Show user-visible feedback when a native action is unavailable or fails.
- Keep account deletion, local-data reset, authentication, and remote synchronization out of scope.

## Capabilities

### New Capabilities
- `desktop-local-account`: Local-mode disclosure, storage-location visibility, and safe Desktop platform actions from the Account destination.

### Modified Capabilities

None.

## Impact

- Adds a Desktop Account workspace and platform-action adapter.
- Exposes read-only Desktop data-directory and database-path helpers from `core:data` so UI metadata and driver configuration cannot diverge.
- Updates Desktop shell routing and destination copy.
- Adds no external dependency and no destructive data operation.
