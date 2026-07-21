## Why

Coroutine cancellation is currently handled inconsistently: some suspend boundaries propagate it correctly, while others convert it into authentication, loading, persistence, or synchronization failures. The recent false synchronization warning after leaving and reopening the app demonstrates that cancellation needs an explicit cross-cutting contract and regression coverage.

## What Changes

- Define cancellation as control flow that must propagate through suspend boundaries rather than being mapped to user-facing failures.
- Establish one project-wide rule: production and test code do not use `runCatching`; suspend boundaries use cancellation-first `try/catch`, while synchronous parsing catches only its expected exception.
- Harden onboarding, remote profile resolution, Garage, Reminders, Maintenance, and synchronization paths against swallowed cancellation.
- Guarantee non-suspending cleanup of transient loading and mutation state when owned work is cancelled.
- Add adversarial cancellation tests covering thrown cancellation, wrapped cancellation, state cleanup, duplicate actions, and synchronization lock release.
- Add architecture guardrails for unmanaged coroutine scopes and non-lifecycle-aware Compose collection.
- Exclude database/notification atomicity and durable side-effect recovery, which are covered by the separate `make-side-effects-cancellation-convergent` change.

## Capabilities

### New Capabilities
- `coroutine-cancellation-safety`: Cross-cutting requirements for cancellation propagation, transient state cleanup, coroutine ownership, synchronization serialization, and cancellation regression tests.

### Modified Capabilities

None.

## Impact

- Affects common presentation code in onboarding, Garage, Reminders, and Maintenance.
- Affects Android credential handling and common Supabase profile resolution.
- Affects `LocalFirstSyncManager` status/error handling and tests while preserving its public `SyncManager` API and mutex-based serialization.
- Adds architecture/test coverage for the uniform exception-handling policy without introducing new runtime dependencies.
