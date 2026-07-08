# Add Sync v0

## Why

Carbura already has a functional Android local-first MVP for vehicles, maintenance records and reminders. To support the product promise of a shared family garage and future Desktop reuse, local data must synchronize with Supabase while preserving offline-first behavior.

## What Changes

- Add KMP sync contracts and a `SyncManager` for vehicles, maintenance records and reminders.
- Add local sync metadata so records can be marked pending and compared by update timestamp.
- Add Supabase remote data access for the v0 sync entities.
- Implement push of local pending changes and pull of remote family data.
- Resolve simple conflicts with `last-write-wins` using `updated_at`.
- Trigger sync after session readiness, foreground/app start, local mutations when feasible, periodic in-app interval and manual user action.
- Expose minimal sync state to Android UI.

## Out Of Scope

- WorkManager/background sync with the app closed.
- Supabase realtime subscriptions.
- Manual conflict resolution.
- Field-level merge.
- Attachments or storage sync.
- Remote notifications.

## References

- `docs/sync-roadmap.md`
- `docs/backlog.md`
