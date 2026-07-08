# Tasks

## 1. Sync Metadata And Schema

- [x] 1.1 Add local sync metadata for vehicles, maintenance records and reminders.
- [x] 1.2 Add SQLDelight migrations for existing local databases.
- [x] 1.3 Ensure local repository writes mark changed records as pending sync.
- [x] 1.4 Define and document v0 delete sync semantics before implementing remote delete propagation.

## 2. Domain Contracts

- [x] 2.1 Add shared sync result/status models.
- [x] 2.2 Add shared `SyncManager` contract.
- [x] 2.3 Add use cases for sync now and observing sync status if needed by UI.

## 3. Supabase Remote Data

- [x] 3.1 Add remote DTOs/mappers for vehicles.
- [x] 3.2 Add remote DTOs/mappers for maintenance records.
- [x] 3.3 Add remote DTOs/mappers for reminders.
- [x] 3.4 Add Supabase queries/upserts/deletes scoped by active family and protected by RLS.
- [x] 3.5 Add tests for DTO mapping and remote gateway behavior where feasible.

## 4. Sync Algorithm

- [x] 4.1 Push pending local vehicle changes.
- [x] 4.2 Push pending local maintenance changes.
- [x] 4.3 Push pending local reminder changes.
- [x] 4.4 Pull remote family data for all v0 entities.
- [x] 4.5 Merge local/remote records with `last-write-wins`.
- [x] 4.6 Preserve local pending data when remote sync fails.
- [x] 4.7 Add unit tests for merge and pending-sync behavior.

## 5. Android Integration

- [x] 5.1 Trigger sync after login/session ready.
- [x] 5.2 Trigger throttled sync on app foreground/resume.
- [x] 5.3 Trigger periodic sync while app is open.
- [x] 5.4 Attempt sync after local mutations when a session is available.
- [x] 5.5 Add manual sync action and minimal status in `Usuario`.

## 6. Verification

- [x] 6.1 Run affected unit tests.
- [x] 6.2 Run `./gradlew test assembleDebug`.
- [x] 6.3 Run `openspec validate add-sync-v0 --strict`.
- [ ] 6.4 Smoke Android sync with Supabase using one account/family.
