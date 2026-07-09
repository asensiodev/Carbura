## Context

Carbura uses SQLDelight as the local-first source of truth and Supabase as the remote family store. Sync v0 already pushes pending local changes, pulls remote changes, and resolves conflicts with `updated_at` last-write-wins semantics.

The remaining risk is not architectural scope, but confidence in demo-critical edge cases: remote failure, offline-created data, soft deletes, and data restoration after authentication with an empty local database.

## Goals / Non-Goals

**Goals:**
- Add deterministic tests for retryable pending changes after remote failure.
- Add deterministic tests for tombstone propagation and deleted entity filtering.
- Add deterministic tests for pulling remote family data into an empty local store.
- Keep fixes minimal and inside the existing sync v0 architecture.

**Non-Goals:**
- Add WorkManager, background sync, realtime, notifications, conflict UI, or manual merge.
- Change Supabase schema unless a real blocker is found.
- Redesign sync status UI.

## Decisions

- Use existing desktop tests for sync hardening because they execute shared KMP data logic quickly without Android runtime dependencies.
- Prefer fake remote data sources in tests over real Supabase calls so failure and retry cases are deterministic.
- Keep local-first semantics unchanged: SQLDelight remains immediately available, and remote failures do not delete or mutate pending local data.
- Treat notifications as a separate OpenSpec change because they introduce Android permissions/scheduling behavior and are not part of sync v0 hardening.

## Risks / Trade-offs

- Tests may reveal behavior that is acceptable for v0 but imperfect for later multi-device conflict handling. Mitigation: only fix MVP-critical regressions and leave advanced conflict handling for sync v1/v2.
- Tombstone tests can be brittle if they depend on exact timestamps. Mitigation: assert entity presence/pending/deleted states rather than wall-clock formatting.
