## Context

The current worktree contains a large cross-platform authentication, family-scoped persistence, and Desktop synchronization implementation. Automated checks pass and real macOS Google login succeeds, but final review identified an acknowledgement race that can lose a local mutation, a real dependency-graph gap in unconfigured Desktop local mode, missing Desktop account-deletion UI, no native package evidence, and obsolete academic documentation. Product scope now treats native reminder notifications as mobile-only; Desktop remains responsible for persistent synchronized reminder management.

## Goals / Non-Goals

**Goals:**

- Preserve every mutation and tombstone created during an in-flight sync.
- Keep authenticated and unconfigured local Desktop modes functional through the real Koin graph.
- Complete Desktop account management with convergent permanent deletion.
- State and test the final Android/Desktop capability boundary.
- Produce reviewable release commits, minimal documentation, package evidence, and a repeatable acceptance checklist.
- Verify the primary value journey through the real Android application composition and local persistence stack.

**Non-Goals:**

- Native reminder notification scheduling on macOS or Windows.
- Reminder editing, family invitations, export, iOS, or Linux packaging.
- Claiming signed Windows validation from a non-Windows host.
- Redesigning the existing shared ViewModels or sync conflict strategy beyond acknowledgement safety.

## Decisions

### Acknowledge uploaded versions, not entity identities

`LocalSyncDataSource` acknowledgement operations will receive the captured `updatedAt` and execute a conditional update on `(familyId, id, updatedAt, pendingSync)`. A local mutation that advances `updatedAt` while remote I/O is active therefore cannot be acknowledged by an older upload. This is preferred over blocking all local editing during sync because local-first interaction must remain available.

### Separate local-only sync dependencies from authenticated sync

Desktop local ViewModels must receive a sync implementation that never resolves Supabase dependencies. Authenticated startup can retain lazy providers, but the Koin graph must not make a blank `SupabaseSettings` reachable from local feature construction. This is preferred over accepting a runtime validation exception because local mode is an explicit product capability.

### Reuse the existing deletion backend and local cleaner

Desktop deletion will use `AuthGateway.deleteAccount`, `AccountLocalDataCleaner`, and the active family scope already used by Android. The controller will own one destructive operation and converge to local mode after dispatch. A second deletion implementation or privileged Desktop credential is prohibited.

### Keep notifications mobile-only

Desktop will not implement partially tested native schedulers. Reminder records and advance settings continue to synchronize, allowing Android to schedule local notifications. Desktop copy and retained documentation will describe this as a deliberate platform boundary, not a temporary preview limitation.

### Validate artifacts honestly

A full JDK with `jpackage` is required for DMG generation. macOS installation and runtime checks apply to the exact generated artifact. MSI configuration remains build-ready, but Windows signing, Credential Manager, and installed-runtime evidence remain explicit external validation until run on Windows.

### Keep E2E external boundaries deterministic

The required Android E2E test will launch the real activity and traverse production navigation, ViewModels, use cases, repositories, and SQLDelight. Authentication/profile restoration, remote synchronization, and native notification scheduling will use deterministic test boundaries so the journey verifies application behavior without depending on Google, Supabase availability, or wall-clock notification delivery.

### Reduce documentation to canonical delivery sources

Retain and update `user-stories.md`, `backlog.md`, and `toolchain/carbura_toolchain.md`. Remove the user-approved historical plans, roadmaps, setup guides, release note, threat model, and `.DS_Store`; update README and surviving cross-references so no broken links remain. Operational facts required to run the submitted project will be condensed into README rather than duplicated across guides.

### Commit verified blocks independently

The current worktree will be reviewed and partitioned into coherent commits for shared persistence/sync, Desktop auth/backend hardening, Desktop startup/account UI, and final documentation/release evidence. Each block must pass targeted checks before commit and the full gate before final push.

## Risks / Trade-offs

- [Conditional acknowledgement misses records with non-monotonic timestamps] -> Existing mutation code must continue assigning a new `updatedAt`; deterministic tests will force interleavings for all entity types and tombstones.
- [Local/authenticated dependency split duplicates composition] -> Keep one shared implementation and introduce only the minimum no-remote boundary required for local mode.
- [Deletion response is ambiguous after dispatch] -> Follow the existing convergent deletion contract and avoid claiming remote success or failure.
- [Removing setup guides harms reproducibility] -> Move only essential build, public configuration, migration, and callback instructions into the Spanish README before deleting them.
- [macOS-only package evidence can be mistaken for cross-platform validation] -> Record Windows validation separately and never mark it complete without a Windows host.
- [Large dirty worktree makes commit partitioning risky] -> Inspect staged diffs for every commit and never stage unrelated files by broad assumption.

## Migration Plan

1. Add sync interleaving tests that fail under identity-only acknowledgement.
2. Implement conditional SQLDelight acknowledgement and run shared/Android/Desktop sync suites.
3. Add a real Desktop composition smoke test for blank configuration and implement the local-only dependency boundary.
4. Add Desktop account deletion UI/controller tests and implementation.
5. Update mobile-only notification copy and acceptance scope.
6. Clean and consolidate documentation, then verify all retained links.
7. Commit and push each verified implementation block.
8. Install/select a full JDK, generate the DMG, inspect it, and execute the final manual checklist.

Rollback uses the pre-change Git revision. SQLDelight acknowledgement changes require no schema migration; Desktop account deletion uses the already deployed Supabase RPC. Documentation deletion can be restored independently from Git if required.

## Open Questions

- Which Windows host and signing identity will be used for the post-delivery MSI acceptance evidence?
- Which public account-deletion URL will be supplied if a production Google Play listing is created?
