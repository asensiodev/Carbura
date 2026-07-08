# Design

## Architecture

Sync v0 is KMP-first. Shared modules own the sync model and behavior:

- `core:domain`: sync use cases/contracts and result models.
- `core:data`: SQLDelight metadata, local pending queries, Supabase remote gateways and `SyncManager` implementation.
- Android app: lifecycle/manual triggers and minimal UI state.
- Desktop future: can reuse shared sync and provide platform-specific triggers later.

## Data Strategy

Each syncable local record needs metadata:

- `updated_at`: timestamp used for `last-write-wins`.
- `pending_sync`: whether local state still needs remote push.
- deletion marker or compatible delete strategy for v0.

The implementation should prefer a deletion strategy that can evolve to remote sync without breaking UI contracts. If hard deletes are kept locally for MVP, v0 must still represent deletes in the outbound sync flow or explicitly document the limitation before implementation proceeds.

## Sync Flow

1. Load active authenticated family/session.
2. Push pending local changes to Supabase.
3. Pull remote changes for active family.
4. Merge by entity id with `last-write-wins` using `updated_at`.
5. Mark successfully synced records as not pending.
6. Report sync result and update `lastSyncedAt`.

## Triggers

- Session ready after login or session restore.
- App start or foreground/resume, throttled by a minimum interval.
- Periodic timer while app is open.
- After local mutations when a session is available.
- Manual `Sync now` action, likely from `Usuario`.

## UX

Sync must not block local creation/deletion/completion. Failures are non-blocking and should leave local data intact for retry.

Android should expose a minimal status surface:

- syncing/not syncing
- last successful sync
- last non-blocking error, if any
- manual sync action

## Risks

- Existing SQLDelight rows may need migration defaults for sync metadata.
- Remote schema must match local sync fields and RLS constraints.
- Delete sync is the most sensitive part; tombstone semantics may be needed for robust remote propagation.
- Supabase auth/session refresh must be handled before remote calls.
