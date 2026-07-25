## 1. Synchronization Safety

- [x] 1.1 Add deterministic tests that mutate vehicles, maintenance records, reminders, and tombstones while an older version is in flight
- [x] 1.2 Extend local acknowledgement contracts with the uploaded `updatedAt` version
- [x] 1.3 Make SQLDelight acknowledgement queries clear `pendingSync` only when family, ID, and uploaded version still match
- [x] 1.4 Verify newer concurrent mutations remain pending and synchronize on the next attempt
- [x] 1.5 Run shared, Android, and Desktop sync tests plus data quality checks
- [x] 1.6 Review, commit, and push the synchronization-safety block

## 2. Desktop Local Composition

- [x] 2.1 Add a real Desktop Koin/composition smoke test with blank Supabase public configuration
- [x] 2.2 Prevent local feature ViewModels from resolving authenticated Supabase or remote-sync dependencies
- [x] 2.3 Verify local Garage, Maintenance, Reminders, and Account mutations persist without network configuration
- [x] 2.4 Declare Desktop public configuration files as inputs to generated-config Gradle tasks to prevent stale builds
- [x] 2.5 Run Desktop tests, static analysis, and local-mode smoke checks
- [x] 2.6 Review, commit, and push the Desktop local-composition block

## 3. Desktop Account Deletion

- [x] 3.1 Extend `DesktopAppController` with a single-flight convergent account-deletion operation
- [x] 3.2 Add a destructive Account section and explicit irreversible confirmation dialog on Desktop
- [x] 3.3 Clear the authenticated family cache, secure session, and active scope after confirmed deletion dispatch
- [x] 3.4 Test cancellation before confirmation, duplicate clicks, confirmed success, unconfirmed response, and cleanup failure
- [x] 3.5 Verify sign-out remains session-local and distinct from permanent deletion
- [x] 3.6 Run Desktop account/auth tests and quality checks
- [x] 3.7 Review, commit, and push the Desktop account-deletion block

## 4. Final Product Scope

- [x] 4.1 Update Desktop reminder copy to state that native alerts are delivered only by the mobile app
- [x] 4.2 Verify Desktop-created reminders synchronize to Android and enter Android notification scheduling
- [ ] 4.3 Complete manual acceptance for maintenance save-only and save-with-reminder flows
- [x] 4.4 Update active OpenSpec checklists to reflect verified Google/Supabase configuration and final platform scope
- [x] 4.5 Confirm no preview or unavailable interactive workflow remains in the Desktop shell

## 5. Documentation Cleanup

- [x] 5.1 Remove `.DS_Store`, Entrega 2 planning/reading files, superseded Desktop deferral/final plans, product and sync roadmaps, account-deletion release notes, Supabase runtime/setup guides, and Desktop auth threat/setup guides approved by the user
- [x] 5.2 Condense essential build, public configuration, migration, OAuth callback, and run instructions into Spanish `readme.md`
- [x] 5.3 Refresh `docs/user-stories.md` to describe implemented Android/Desktop behavior and mobile-only native notifications
- [x] 5.4 Refresh `docs/backlog.md` and remove references to deleted files
- [x] 5.5 Refresh `docs/toolchain/carbura_toolchain.md` with the final modules, migrations, security boundaries, and validation commands
- [x] 5.6 Search README, docs, prompts, and OpenSpec for broken references to deleted files and repair them
- [x] 5.7 Review, commit, and push the product-scope and documentation block

## 6. Packaging And Automated Release Gate

- [x] 6.1 Select or install a full JDK containing `jpackage` without changing the Android toolchain unexpectedly
- [x] 6.2 Generate a clean macOS DMG and inspect it for privileged credentials, tokens, and sensitive logs
- [x] 6.3 Install the exact DMG and verify launch, database migration, Google login, Keychain restoration, offline startup, and sign-out
- [x] 6.4 Record Windows MSI, signing, Credential Manager, and installed-runtime validation as external until run on Windows
- [x] 6.5 Run strict OpenSpec validation, full quality checks, all tests, Android assembly, Desktop package checks, and `git diff --check`
- [x] 6.6 Review, commit, and push packaging and release-evidence changes
- [x] 6.7 Add an app-level Android E2E test for vehicle -> future ITV maintenance -> history -> planned reminder
- [x] 6.8 Run the E2E test twice on an Android emulator with deterministic external boundaries
- [x] 6.9 Align Android `versionName` and Desktop `packageVersion` at `1.0.0`

## 7. Manual Android/Desktop Acceptance

- [ ] 7.1 Verify same-account family identity and bidirectional vehicle create/update/delete propagation
- [ ] 7.2 Verify bidirectional maintenance create/update/delete propagation and individual cost presentation
- [ ] 7.3 Verify reminder create/complete/delete propagation and Android mobile notification scheduling
- [ ] 7.4 Verify offline mutations, restart persistence, retry, and last-write-wins behavior
- [ ] 7.5 Verify Desktop local-data import, exclusion, cancellation, and account switching without cross-family visibility
- [ ] 7.6 Verify Desktop session restoration, refresh, secure sign-out, and Android session independence
- [ ] 7.7 Verify two-account RLS denial for reads, writes, tombstones, family reassignment, and profile/family administration
- [ ] 7.8 Archive completed OpenSpec changes and publish the final verified revision
