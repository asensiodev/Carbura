## Why

Sync v0 already works end-to-end, but it is the highest-risk area for the Android demo because failures can hide until real offline/online and delete flows are exercised. This change hardens the existing local-first behavior with explicit coverage for retry, tombstones, and restore-after-login scenarios.

## What Changes

- Add focused tests for offline-first sync behavior across vehicles, maintenance records, and reminders.
- Verify failed remote sync keeps local data and pending changes retryable.
- Verify deleted entities sync as tombstones and do not reappear after pull.
- Verify remote data for the authenticated family is restored into an empty local store after login/reinstall-like conditions.
- Apply minimal bug fixes if the tests expose gaps.

## Capabilities

### New Capabilities

### Modified Capabilities
- `sync-v0`: Harden existing sync requirements with explicit retry, tombstone, and restore scenarios.

## Impact

- Affected modules: `core:data`, `core:domain` if a contract gap is found, and Android sync trigger/status code only if needed.
- Affected tests: primarily `core/data/src/desktopTest` sync tests.
- No new external dependencies, schema migrations, or user-facing UI changes are planned.
