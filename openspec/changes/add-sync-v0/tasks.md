# Tasks

## 1. Sync Metadata And Schema

- [ ] 1.1 Add local sync metadata for vehicles, maintenance records and reminders.
- [ ] 1.2 Add SQLDelight migrations for existing local databases.
- [ ] 1.3 Ensure local repository writes mark changed records as pending sync.
- [ ] 1.4 Define and document v0 delete sync semantics before implementing remote delete propagation.

## 2. Domain Contracts

- [ ] 2.1 Add shared sync result/status models.
- [ ] 2.2 Add shared `SyncManager` contract.
- [ ] 2.3 Add use cases for sync now and observing sync status if needed by UI.

## 3. Supabase Remote Data

- [ ] 3.1 Add remote DTOs/mappers for vehicles.
- [ ] 3.2 Add remote DTOs/mappers for maintenance records.
- [ ] 3.3 Add remote DTOs/mappers for reminders.
- [ ] 3.4 Add Supabase queries/upserts/deletes scoped by active family and protected by RLS.
- [ ] 3.5 Add tests for DTO mapping and remote gateway behavior where feasible.

## 4. Sync Algorithm

- [ ] 4.1 Push pending local vehicle changes.
- [ ] 4.2 Push pending local maintenance changes.
- [ ] 4.3 Push pending local reminder changes.
- [ ] 4.4 Pull remote family data for all v0 entities.
- [ ] 4.5 Merge local/remote records with `last-write-wins`.
- [ ] 4.6 Preserve local pending data when remote sync fails.
- [ ] 4.7 Add unit tests for merge and pending-sync behavior.

## 5. Android Integration

- [ ] 5.1 Trigger sync after login/session ready.
- [ ] 5.2 Trigger throttled sync on app foreground/resume.
- [ ] 5.3 Trigger periodic sync while app is open.
- [ ] 5.4 Attempt sync after local mutations when a session is available.
- [ ] 5.5 Add manual sync action and minimal status in `Usuario`.

## 6. Verification

- [ ] 6.1 Run affected unit tests.
- [ ] 6.2 Run `./gradlew test assembleDebug`.
- [ ] 6.3 Run `openspec validate add-sync-v0 --strict`.
- [ ] 6.4 Smoke Android sync with Supabase using one account/family.
