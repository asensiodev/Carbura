## 1. Shared Cancellation Contract

- [x] 1.1 Replace suspend `runCatching` boundaries with explicit cancellation-first handling and retain cancellation propagation tests.
- [x] 1.2 Replace synchronous and test `runCatching` sites with narrow parse handling, non-throwing lookups, or `assertFailsWith`.
- [x] 1.3 Remove `Result.rethrowCancellation()` and add a project-wide architecture guard against `runCatching`.

## 2. Synchronization Safety

- [x] 2.1 Retain the failing regression for cancellation being reported as a synchronization failure after lifecycle teardown.
- [x] 2.2 Complete `LocalFirstSyncManager` cancellation-first handling so status is cleared and cancellation propagates through `Mutex.withLock`.
- [x] 2.3 Add tests for cancelling an actively suspended sync, a cancelled mutex waiter, lock availability afterward, and successful retry after cancellation.
- [x] 2.4 Verify ordinary remote failures still preserve pending data and publish retryable `SyncResult.Failure` state.

## 3. Authentication And Profile Boundaries

- [x] 3.1 Add cancellation tests for session lookup, profile lookup, profile creation, Google sign-in variants, and sign-out in `OnboardingViewModel`.
- [x] 3.2 Replace unsafe suspend `runCatching` handling in onboarding with explicit cancellation-first catches before fallback or failure effects.
- [x] 3.3 Add an Android credential cancellation test and rethrow cancellation before `GoogleSignInError` handling.
- [x] 3.4 Add profile gateway tests proving family-name cancellation propagates while ordinary lookup failure retains the defined fallback.
- [x] 3.5 Harden `SupabaseUserProfileGateway` family-name resolution against swallowed cancellation.

## 4. Feature Presentation Boundaries

- [x] 4.1 Add Reminders cancellation tests for load, create, complete, and delete, including state cleanup and absence of persistence errors.
- [x] 4.2 Harden Reminders broad catches with cancellation-first propagation and non-suspending `finally` cleanup.
- [x] 4.3 Add Garage overview cancellation tests for load and delete, including retry availability and duplicate-action state.
- [x] 4.4 Harden Garage overview broad catches and ensure transient state is activated and cleared within owned coroutine execution.
- [x] 4.5 Add Vehicle Form cancellation tests for create, update, and reconciliation entry points.
- [x] 4.6 Harden Vehicle Form broad catches and prevent cancellation from emitting persistence errors or obsolete success effects.
- [x] 4.7 Verify Maintenance cancellation tests cover the same contract and add any missing stale-result or retry cases.

## 5. Ownership And Stale Results

- [x] 5.1 Add tests using already-cancelled injected scopes to ensure events cannot leave loading, deleting, or mutation state stuck.
- [x] 5.2 Evaluate replaceable work and add request identity guards wherever obsolete results can overwrite current state.
- [x] 5.3 Evaluate cancellation-hostile dependencies and add test-only `NonCancellable` coverage wherever obsolete completion is possible.
- [x] 5.4 Add Konsist guardrails against `GlobalScope`, unmanaged production `CoroutineScope(...)`, and non-lifecycle-aware Compose collection.

## 6. Verification

- [x] 6.1 Run all affected common, desktop, Android unit, and instrumented cancellation tests.
- [x] 6.2 Run `qualityCheck`, `:app:android:assembleDebug`, and `git diff --check`.
- [x] 6.3 Manually verify leaving and reopening the authenticated app does not show a synchronization failure for cancelled work.
