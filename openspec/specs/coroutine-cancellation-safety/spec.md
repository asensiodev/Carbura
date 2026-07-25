# coroutine-cancellation-safety Specification

## Purpose
Define project-wide structured concurrency, cancellation propagation, transient-state cleanup, and serialized synchronization guarantees.

## Requirements
### Requirement: Cancellation propagates through suspend boundaries
The system SHALL propagate `CancellationException` through suspend orchestration and SHALL NOT convert cancellation into domain failure, fallback data, telemetry failure, or user-facing error state.

#### Scenario: Suspend dependency is cancelled
- **WHEN** an authentication, profile, loading, persistence, credential, or synchronization dependency throws `CancellationException`
- **THEN** the owning coroutine remains cancelled and no ordinary failure result or error feedback is produced

#### Scenario: Suspend boundary handles failures
- **WHEN** suspend orchestration handles operational exceptions
- **THEN** it uses explicit cancellation-first exception handling and does not wrap the operation with `runCatching`

#### Scenario: Synchronous parsing fails
- **WHEN** a non-suspending parser receives malformed local input
- **THEN** it catches only the expected parse exception or uses a non-throwing lookup and returns its defined fallback

### Requirement: Exception handling is project-wide consistent
Production and test Kotlin code SHALL NOT use `runCatching`. Expected domain outcomes SHALL use typed domain results, suspend failures SHALL use explicit cancellation-first handling, and parse fallbacks SHALL catch narrow exceptions.

#### Scenario: Architecture rules inspect Kotlin sources
- **WHEN** the quality architecture suite scans production and test source sets
- **THEN** any `runCatching` usage fails the quality gate

### Requirement: Cancellation restores transient presentation state
Presentation components SHALL restore transient loading, deletion, and mutation state when their owned coroutine completes or is cancelled, and SHALL NOT publish cancellation as a persistence or load error.

#### Scenario: ViewModel mutation is cancelled
- **WHEN** a feature mutation is cancelled while suspended
- **THEN** its active mutation indicator is cleared without emitting a persistence failure or success effect

#### Scenario: ViewModel load is cancelled
- **WHEN** a feature load is cancelled while suspended
- **THEN** its in-progress indicator is cleared and cancellation does not transition the feature to an error state

#### Scenario: Replaced request ignores cancellation
- **WHEN** an obsolete dependency completes after its request was replaced and cancelled
- **THEN** request identity protection prevents the obsolete result or cleanup from overwriting current state

### Requirement: Coroutine work has an explicit lifecycle owner
Production coroutine work SHALL be owned by a ViewModel, lifecycle-aware Compose effect, injected application owner, or another explicit structured-concurrency scope. Production code SHALL NOT use `GlobalScope` or create an unmanaged scope without an explicit cancellation owner.

#### Scenario: Compose observes state and effects
- **WHEN** Android Compose collects long-lived application or feature flows
- **THEN** collection is bound to lifecycle-aware APIs or a lifecycle-aware effect boundary

#### Scenario: Feature is destroyed
- **WHEN** the owner of feature work is destroyed
- **THEN** its child work is cancelled without producing user-facing failure feedback

### Requirement: Synchronization remains serialized and cancellation-safe
`SyncManager` SHALL serialize synchronization runs that mutate pending and remote state, SHALL clear active synchronization state when a run is cancelled, and SHALL release synchronization ownership for waiting or subsequent callers.

#### Scenario: Running synchronization is cancelled
- **WHEN** the active synchronization run is cancelled while holding serialization ownership
- **THEN** `isSyncing` becomes false, `lastErrorMessage` remains clear, cancellation propagates, and a later synchronization can acquire ownership

#### Scenario: Waiting synchronization is cancelled
- **WHEN** a caller waiting for synchronization ownership is cancelled
- **THEN** it leaves the wait queue without cancelling the active run or blocking later callers

#### Scenario: Concurrent synchronization requests succeed serially
- **WHEN** lifecycle, periodic, manual, or post-mutation triggers request synchronization concurrently
- **THEN** remote and local merge operations do not overlap and each completed run publishes a coherent `SyncStatus`

### Requirement: Cancellation behavior has dedicated regression coverage
Cancellation SHALL be tested as an outcome distinct from success and ordinary failure at each broad recovery boundary.

#### Scenario: Cancellation-specific test
- **WHEN** a test fake suspends and the owning job is cancelled or throws a cancellation exception
- **THEN** the test asserts propagation, absence of error feedback, transient state cleanup, and retry availability as applicable

#### Scenario: Ordinary failure remains recoverable
- **WHEN** the same boundary throws a non-cancellation failure
- **THEN** existing recoverable error behavior remains unchanged
