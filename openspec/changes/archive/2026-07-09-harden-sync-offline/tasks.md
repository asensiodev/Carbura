## 1. Tests

- [x] 1.1 Add sync test for failed remote push preserving pending local vehicle data.
- [x] 1.2 Add sync test for successful retry clearing a previously pending vehicle change.
- [x] 1.3 Add sync test for deleted vehicle tombstone remaining pending after remote failure and hidden from active lists.
- [x] 1.4 Add sync test for successful tombstone push clearing local pending status and sending remote deletion metadata.
- [x] 1.5 Add sync test for pulling remote vehicles, maintenance records, and reminders into an empty local store.

## 2. Implementation

- [x] 2.1 Fix any sync manager, local data source, repository, or mapper behavior exposed by the tests with minimal changes.
- [x] 2.2 Keep the implementation KMP/shared and avoid Android-only sync logic unless required by a failing trigger/status test.

## 3. Verification

- [x] 3.1 Run targeted data tests.
- [x] 3.2 Run relevant broader build/test checks.
- [x] 3.3 Validate the OpenSpec change strictly.
