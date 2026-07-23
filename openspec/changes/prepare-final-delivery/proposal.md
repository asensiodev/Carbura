## Why

Carbura now has working Android and Desktop applications with real Google authentication and shared Supabase synchronization, but final review exposed a synchronization acknowledgement race, incomplete Desktop account management, unverified native packaging, and stale delivery documentation. These gaps must be closed before freezing and validating the academic delivery.

## What Changes

- Make sync acknowledgements version-aware so a mutation or tombstone created during an in-flight upload remains pending for the next sync.
- Verify the real Desktop dependency graph supports local-only mode without resolving an unconfigured Supabase client.
- Add explicit, convergent account deletion to the authenticated Desktop Account workspace.
- Define native reminder notifications as an Android-only product capability; Desktop keeps persistent synchronized reminders without claiming native alerts.
- Produce and inspect the available native Desktop distribution, while recording Windows-only validation as platform-dependent evidence.
- Replace historical planning documentation with a concise final set: user stories, backlog, toolchain, and updated README delivery guidance.
- Create automated and manual release checklists covering deletion propagation, offline recovery, account switching, authentication restoration, and Android/Desktop synchronization.
- Commit and push verified implementation blocks independently to avoid one unreviewable release commit.

## Capabilities

### New Capabilities
- `final-delivery-validation`: Defines the automated gates, platform smoke tests, artifact checks, documentation set, and evidence required before delivery.

### Modified Capabilities
- `sync-v0`: Pending status is cleared only for the exact uploaded version, including tombstones.
- `account-deletion`: Permanent deletion and convergent local cleanup are exposed consistently from authenticated Android and Desktop account UI.
- `desktop-local-account`: Local mode remains usable when Supabase public configuration is absent and authenticated Account exposes complete account actions.
- `desktop-reminders-workflow`: Native operating-system notifications are explicitly mobile-only while Desktop reminders remain persistent and synchronized.
- `desktop-application`: Final delivery uses complete interactive workflows rather than preview labeling and validates native package generation on each supported build host.

## Impact

- Shared sync contracts, SQLDelight acknowledgement queries, and deterministic concurrency tests.
- Desktop Koin composition, Account UI/controller state, Supabase session cleanup, and local family data cleanup.
- Desktop reminder copy and product documentation; no Desktop notification scheduler will be introduced.
- Compose Desktop packaging toolchain and artifact inspection.
- `docs/`, `readme.md`, active OpenSpec task status, Git history, and the final manual acceptance process.
