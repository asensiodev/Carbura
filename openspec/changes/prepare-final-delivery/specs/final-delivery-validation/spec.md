## ADDED Requirements

### Requirement: Automated release gate
The delivery candidate SHALL pass formatting, static analysis, architecture checks, unit and integration tests, Android assembly, Desktop compilation, strict OpenSpec validation, and diff integrity checks from a clean revision.

#### Scenario: Candidate is prepared for delivery
- **WHEN** the final automated release command runs
- **THEN** every required gate completes successfully without skipped release-blocking checks

### Requirement: Installed-platform acceptance
The delivery evidence SHALL distinguish automated verification from behavior that requires real operating systems, identity providers, secure credential vaults, and installed packages.

#### Scenario: macOS candidate is validated
- **WHEN** the macOS distribution is prepared
- **THEN** the exact packaged artifact is installed and tested for Google login, session restoration, offline startup, synchronization, sign-out, and local database migration

#### Scenario: Windows validation is unavailable on the build host
- **WHEN** no supported Windows signing and runtime environment is available
- **THEN** the delivery records Windows validation as an explicit residual gap rather than claiming it passed

### Requirement: Cross-device deletion and synchronization acceptance
The final manual checklist SHALL cover create, update, complete, and delete propagation between Android and Desktop for the same authenticated family.

#### Scenario: Desktop deletion reaches Android
- **WHEN** a vehicle, maintenance record, or reminder is deleted on Desktop and both devices synchronize
- **THEN** Android no longer exposes the deleted entity and neither device resurrects it on a later sync

### Requirement: Minimal final documentation set
The repository SHALL retain a concise, internally consistent final documentation set and SHALL contain no broken links to removed historical plans.

#### Scenario: Historical documentation is removed
- **WHEN** obsolete delivery plans and superseded roadmaps are deleted
- **THEN** README and retained documents reference only files that still exist

### Requirement: Verified delivery checkpoints
Implementation SHALL be committed and pushed in independently reviewable blocks only after the relevant automated checks pass.

#### Scenario: A delivery block is pushed
- **WHEN** a coherent implementation block passes its required checks
- **THEN** only intended files are committed with a descriptive message before pushing to the tracked branch
