## Why

User-entered custom maintenance labels lose capitalization because only a lowercased technical slug is persisted and later reused as display text. Other editable text fields already preserve case, so the project needs an explicit contract separating normalized identifiers from user-visible values.

## What Changes

- Persist the exact trimmed custom maintenance label independently from its normalized technical ID.
- Carry the label through local storage, synchronization, and remote persistence.
- Render canonical types from localized enum labels and custom types from preserved user text on Android and Desktop.
- Keep a readable fallback for legacy records whose original casing was already lost.
- Add regression coverage confirming Garage, Reminders, and maintenance text casing behavior.

## Capabilities

### New Capabilities
- `user-input-casing`: Preservation of meaningful casing in user-entered display text while allowing separate technical normalization.

### Modified Capabilities

## Impact

- Adds a nullable field to `MaintenanceRecord`, SQLDelight rows, sync entities, and Supabase maintenance records.
- Requires additive local and remote migrations.
- Updates shared, Android, and Desktop maintenance display and edit behavior.
