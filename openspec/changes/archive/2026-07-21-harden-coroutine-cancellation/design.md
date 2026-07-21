## Context

Carbura launches suspend work from authenticated shell lifecycle events and from feature ViewModels. Several call sites use `runCatching` or broad `catch` blocks around suspend operations. Kotlin cancellation is represented by `CancellationException`, which those constructs can accidentally convert into ordinary failures. That conversion can emit false UI errors, continue obsolete workflows, or leave transient state inconsistent.

The codebase already has two correct local patterns: `MaintenanceHistoryViewModel` rethrows cancellation before handling other failures, and the pending `LocalFirstSyncManager` fix clears synchronization state and rethrows cancellation. Santoro provides a useful reference for making cancellation propagation and adversarial tests systematic.

Synchronization can be requested concurrently by initial launch, foreground entry, periodic refresh, manual retry, the User screen, and post-mutation hooks. Its existing `Mutex` serializes remote snapshots, pending-record uploads, local merge operations, and `SyncStatus` transitions.

## Goals / Non-Goals

**Goals:**

- Treat cancellation as control flow at every suspend boundary.
- Prevent cancellation from producing user-facing authentication, loading, persistence, or synchronization failures.
- Restore transient state with non-suspending cleanup when owned work is cancelled.
- Preserve lifecycle ownership and prevent obsolete work from publishing results.
- Preserve serialized synchronization and verify lock/status behavior under cancellation.
- Establish reusable tests and architecture guardrails.

**Non-Goals:**

- Making database and notification side effects atomic across cancellation boundaries.
- Introducing an outbox or durable workflow engine.
- Replacing `SyncManager` serialization with a single-flight or dirty-loop coordinator.
- Wrapping cleanup in production `NonCancellable` blocks.
- Changing public domain result types or user-facing error copy.

## Decisions

### Use one explicit exception-handling policy

Production and test code will not use `runCatching`. Suspend code will catch `CancellationException` first and rethrow it before handling expected failures. Synchronous parsing will use explicit `try/catch` with the narrow expected exception, or non-throwing lookup APIs for enums.

This is preferred over `Result.rethrowCancellation()` or a generic `runSuspendCatching` helper because every exception boundary remains visible and reviewers do not need to classify whether a particular `runCatching` call is safe. Expected domain outcomes continue to use existing sealed result types rather than `Result<Throwable>`.

### Keep mutex-based synchronization serialization

`LocalFirstSyncManager` will retain `Mutex.withLock`. Concurrent sync runs can otherwise upload the same pending snapshot, mark records while another run is merging remote state, and race `SyncStatus` updates. `withLock` already releases ownership when a running caller is cancelled and removes cancelled waiters safely.

A single-flight implementation was considered but rejected for this change. Simply sharing or dropping an in-flight request can miss a mutation created after the current run read its pending snapshot. A dirty-loop coalescer could solve that but adds ownership and cancellation complexity without a demonstrated performance problem.

### Clean transient state in `finally`

ViewModels will set loading or mutation state inside the launched coroutine and restore it in `finally` using non-suspending `StateFlow` updates. Error state and one-off effects will be produced only for non-cancellation failures.

No `NonCancellable` context is required for non-suspending state updates. Tests may use `NonCancellable` fakes to verify stale-result defenses when a dependency violates cooperative cancellation.

### Keep work attached to an owner

Production work remains attached to injected feature scopes, Compose effects, or lifecycle-aware collection. Architecture tests will reject `GlobalScope`, direct unmanaged production `CoroutineScope(...)` construction, and non-lifecycle-aware Compose state collection. Test scope injection remains allowed.

Where repeated requests can replace older work and a dependency could ignore cancellation, request identity or version checks will prevent obsolete results and cleanup from overwriting current state.

### Separate propagation from durable convergence

This change guarantees that cancellation is represented correctly and local transient state is restored. It does not guarantee atomic completion across database and alarm scheduler calls. Those workflows require idempotency, persisted intent, or an outbox and are designed in `make-side-effects-cancellation-convergent`.

## Risks / Trade-offs

- [Cancellation propagation exposes assumptions in callers that previously observed normal failure results] -> Add focused tests at repository, ViewModel, credential, and sync boundaries before changing each path.
- [A broad catch can be added later without cancellation handling] -> Add architecture guardrails where feasible and maintain cancellation-specific tests for each orchestration boundary.
- [Moving state activation inside `launch` can alter immediate event-observation timing] -> Preserve synchronous duplicate-admission guards where required and test rapid repeated events.
- [Queued mutex callers can perform redundant sync runs] -> Keep the current correctness-first behavior; evaluate coalescing only with measured contention and an explicit dirty-run contract.
- [Explicit parse catches are more verbose than `runCatching`] -> Centralize repeated parse-or-null behavior in narrowly scoped helpers and prefer non-throwing lookup APIs where available.

## Migration Plan

1. Replace all production and test `runCatching` usage with explicit cancellation-safe or parse-specific handling.
2. Land the pending `LocalFirstSyncManager` cancellation fix and expand mutex cancellation coverage.
3. Harden authentication/profile and Android credential boundaries.
4. Harden Garage, Reminders, Vehicle Form, and verify Maintenance consistency.
5. Add scope ownership, lifecycle collection, and project-wide no-`runCatching` architecture tests.
6. Run common, desktop, Android unit, and instrumented quality gates.

The change requires no data migration and can be rolled back as a code-only change.

## Open Questions

None. Durable convergence and notification side-effect recovery are intentionally deferred to the companion change.
