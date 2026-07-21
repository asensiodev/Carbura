## Context

Android UI copy is stored in module-local Android XML resources whose default values are Spanish. Desktop UI copy is embedded directly in Kotlin and is predominantly English. The application already uses Compose Multiplatform, but resource ownership has not followed the presentation code across platforms.

## Goals / Non-Goals

**Goals:**
- Present equivalent Android and Desktop workflows with consistent Spanish terminology.
- Make Desktop user-facing copy resource-backed rather than hardcoded.
- Establish a shared resource location that can be consumed from common, Android, and Desktop Compose code.
- Preserve platform-specific explanations where Desktop intentionally differs from Android.

**Non-Goals:**
- Adding a language selector or runtime locale override.
- Shipping an English translation in this change.
- Rewriting platform-specific layouts or changing workflow behavior.
- Forcing identical wording when authentication, synchronization, or native notifications differ by platform.

## Decisions

### Use Compose Multiplatform resources in the owning feature

Shared workflow strings will live in each feature's `commonMain/composeResources` source set and will be resolved with Compose Multiplatform `stringResource`. Application-shell and Desktop-only copy will live in the closest application module resource set. This keeps ownership aligned with feature boundaries and avoids introducing a global string registry.

Android XML-only resources were considered, but Desktop cannot consume generated Android `R` resources. A Kotlin map was also considered, but it would bypass locale-aware resource tooling and make formatting and future translations harder.

### Keep Spanish as the default product copy

Existing Android default resources define the current product language as Spanish. Migrated resources will preserve that behavior, and Desktop will adopt the same terminology. This change does not infer locale from the operating system because no complete second locale is currently maintained.

### Share semantic text, not platform capability claims

Labels such as vehicle fields, maintenance actions, reminder states, and validation errors will be shared where their meaning is equivalent. Desktop-only local-storage, notification, and account-mode explanations will remain Desktop-owned but will be translated to Spanish and resource-backed.

### Migrate incrementally without changing presentation state

Resource resolution remains at the Compose boundary. Shared MVI state and domain result types will continue carrying semantic values rather than localized strings, preserving deterministic tests and platform independence.

## Risks / Trade-offs

- [Risk] Moving resources can break generated imports or Android packaging. → Migrate one owning module at a time and compile both Android and Desktop targets.
- [Risk] Existing Android copy and newer Desktop concepts do not always map one-to-one. → Reuse established terminology and add platform-specific Spanish resources where behavior differs.
- [Risk] A partial migration leaves hidden English copy in errors or accessibility descriptions. → Search Desktop Kotlin sources for user-visible literals and cover representative resource resolution in tests.
- [Trade-off] Spanish remains the only complete language. → The shared resource structure enables future locale directories without adding an incomplete language selector now.

## Migration Plan

1. Enable or confirm Compose resource generation in modules that own cross-platform UI.
2. Add shared Spanish resource keys using established Android terminology.
3. Replace Desktop hardcoded strings, including formatted and accessibility text.
4. Update Android call sites only where resource ownership moves.
5. Compile and test Android, Desktop, and shared feature targets.

Rollback is a source-level revert because there is no persisted-data or API migration.

## Open Questions

None.
