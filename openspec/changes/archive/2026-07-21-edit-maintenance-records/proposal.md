## Why

Maintenance records can be created and deleted but cannot be corrected after entry, forcing users to remove and recreate records and risking reminder inconsistency. Editing must be implemented as a scoped update rather than reusing unrestricted upsert persistence so ownership, tombstones, and deterministic reminders remain safe.

## What Changes

- Add a typed, family-and-vehicle-scoped maintenance update contract that preserves immutable identity and currency fields.
- Reuse creation parsing and validation for editable maintenance values.
- Reconcile automatic and existing planned reminders atomically with the record update.
- Add shared edit state, events, effects, cancellation behavior, and stale-record handling to the Maintenance ViewModel.
- Add accessible edit actions and reusable create/edit forms to Android and Desktop Maintenance interfaces.
- Add domain, SQLDelight, shared presentation, Android UI, and Desktop integration coverage.

## Capabilities

### New Capabilities
- `maintenance-record-editing`: Safe cross-platform editing of active maintenance records with convergent reminder and notification state.

### Modified Capabilities

## Impact

- Extends maintenance and reminder repository contracts with scoped active-record queries and atomic update operations.
- Adds a SQLDelight scoped `UPDATE` query without a schema migration.
- Adds a shared update use case and Maintenance ViewModel edit mode.
- Updates Android and Desktop Maintenance presentation while preserving platform-specific Compose UI.
