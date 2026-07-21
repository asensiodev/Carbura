## 1. Project Bootstrap

- [x] 1.1 Confirm current branch is `feature-entrega2-AAC` and working tree has only intended OpenSpec change files.
- [x] 1.2 Choose a Kotlin Multiplatform bootstrap approach compatible with Android-first development and modular Gradle structure.
- [x] 1.3 Create root Gradle settings and version catalog required for a Kotlin Multiplatform project.
- [x] 1.4 Add Gradle wrapper if missing and confirm Gradle can start.

## 2. Build Logic

- [x] 2.1 Create `build-logic` for project convention plugins.
- [x] 2.2 Add minimal convention plugin for Kotlin Multiplatform library modules.
- [x] 2.3 Add minimal convention plugin for Android application modules.
- [x] 2.4 Add minimal convention plugin for Compose-enabled modules.
- [x] 2.5 Add minimal convention plugin or shared configuration for tests and Kotlin serialization if needed.

## 3. Module Structure

- [x] 3.1 Create `app:android` as the primary runnable Android target.
- [x] 3.2 Create `app:desktop` only if it does not block Android build stability; otherwise document it as deferred.
- [x] 3.3 Create core modules: `core:model`, `core:domain`, `core:data`, `core:auth`, `core:designsystem`, `core:testing`.
- [x] 3.4 Create feature modules or directories for `feature:onboarding`, `feature:garage`, `feature:maintenance`, and `feature:reminders` when feasible without overcomplicating the first build.
- [x] 3.5 Ensure module dependencies point inward: app depends on features/core; features depend on core; core domain/model do not depend on platform APIs.

## 4. Shared Architecture Foundations

- [x] 4.1 Add placeholder package structure for domain models, use cases, repository contracts, and shared UiState.
- [x] 4.2 Add `core:auth` contract for future authentication without implementing real Credential Manager logic yet.
- [x] 4.3 Document or stub platform adapter locations for Android, Desktop optional, and iOS future.
- [x] 4.4 Add `core:designsystem` base theme/tokens or placeholders that compile.
- [x] 4.5 Add `core:testing` placeholder utilities or module scaffolding for future fakes.

## 5. Safe Local Configuration

- [x] 5.1 Create or update `local.properties.example` with placeholder `SUPABASE_URL`, `SUPABASE_ANON_KEY`, and `GOOGLE_CLIENT_ID`.
- [x] 5.2 Confirm `.gitignore` excludes `local.properties`, environment files, keystores, and secrets.
- [x] 5.3 Ensure no real credentials are introduced in code, docs, or Gradle files.

## 6. Verification

- [x] 6.1 Run `./gradlew tasks` and resolve configuration errors.
- [x] 6.2 Run `./gradlew test` or the closest available test task after scaffold creation.
- [x] 6.3 Run `./gradlew assembleDebug` or the actual Android debug assemble task exposed by the scaffold.
- [x] 6.4 If any verification command cannot run yet, document the exact reason and next fix in the implementation summary.

## 7. Documentation And Closure

- [x] 7.1 Update `readme.md` setup section with actual scaffold commands if they differ from the plan.
- [x] 7.2 Update `prompts.md` with the prompt/process used for `bootstrap-kmp-project`.
- [x] 7.3 Review generated file structure against `openspec/changes/bootstrap-kmp-project/specs/kmp-project-structure/spec.md`.
- [x] 7.4 Commit the bootstrap implementation with a Conventional Commit message.
