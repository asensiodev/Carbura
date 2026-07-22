## 1. Security Foundation

- [ ] 1.1 Record the Desktop authentication threat model and trust boundaries for browser, loopback callback, credential vault, local database, Supabase, and logs
- [ ] 1.2 Add a versioned migration that removes direct client profile family reassignment and restricts family/profile administration
- [ ] 1.3 Add hostile RLS tests for self-reassignment, profile administration, family administration, and cross-family product data
- [ ] 1.4 Review and select maintained macOS Keychain and Windows Credential Manager adapters without a plaintext release fallback
- [ ] 1.5 Add Desktop Supabase public configuration wiring without packaging privileged keys or secrets
- [ ] 1.6 Verify the external Supabase and Google configuration in `docs/desktop-auth-sync-setup.md`

## 2. Secure Desktop OAuth

- [ ] 2.1 Extend the auth boundary for authorization URL creation, code exchange, session refresh, and semantic failures
- [ ] 2.2 Integrate the Supabase-supported PKCE S256 transaction without duplicating provider-owned state or nonce validation
- [ ] 2.3 Implement a one-shot callback listener bound only to `127.0.0.1` before browser navigation
- [ ] 2.4 Enforce one active attempt, five-minute timeout, 8 KiB request limits, exact host/path/method, and exactly one code or OAuth error parameter
- [ ] 2.5 Reject duplicate, replayed, expired, oversized, concurrent, and malformed callbacks
- [ ] 2.6 Close the listener and clear transient OAuth material on success, OAuth error, cancellation, timeout, and failure
- [ ] 2.7 Open Google authorization only in the operating-system browser
- [ ] 2.8 Add deterministic tests for successful OAuth, interception resistance, replay rejection, malformed requests, timeout, cancellation, concurrency, and occupied callback port

## 3. Secure Session Lifecycle

- [ ] 3.1 Add a shared `AuthSessionStore` contract with in-memory test implementation
- [ ] 3.2 Implement macOS Keychain credential persistence and deletion
- [ ] 3.3 Implement Windows Credential Manager credential persistence and deletion
- [ ] 3.4 Disable Supabase default persistence and make the OS vault the sole project-, environment-, and account-namespaced session backend
- [ ] 3.5 Restore, refresh, atomically rotate, and validate the Supabase session before authenticated content
- [ ] 3.6 Fail closed when secure credential storage is unavailable in release builds
- [ ] 3.7 Implement session-local Desktop sign-out that leaves Android sessions active and clears local credentials even if offline revocation fails
- [ ] 3.8 Add automated checks that application storage, logs, and diagnostics contain none of the specified authentication secrets

## 4. Local Data Consent

- [ ] 4.1 Add local sync queries that count unresolved active and pending `local-family` records
- [ ] 4.2 Compute an immutable approval snapshot from legacy IDs, ownership, deletion state, and update versions
- [ ] 4.3 Persist the import decision by authenticated user, family, and snapshot without storing credentials in SQLDelight
- [ ] 4.4 Present Import and merge, Use account data, and Cancel before first sync when local records exist
- [ ] 4.5 Refactor unconditional legacy adoption out of `LocalFirstSyncManager`
- [ ] 4.6 Preflight ID collisions and transactionally remap legacy IDs plus every dependent reference before pull or push
- [ ] 4.7 Implement transactional and idempotent adoption that preserves identifiers when safe, relationships, tombstones, and pending flags
- [ ] 4.8 Keep excluded `local-family` records recoverable, collision-safe, and visible as a separate local-data status
- [ ] 4.9 Test import, exclusion, cancellation, changed snapshots, ID collisions, interrupted adoption, failed first sync, and a different account signing in

## 5. Desktop Authenticated Sync

- [ ] 5.1 Start Desktop with `authModule` and remove synthetic authenticated family and no-op sync overrides
- [ ] 5.2 Resolve or provision the authenticated profile and family before synchronization
- [ ] 5.3 Add active-family parameters to all local reads, mutations, tombstones, sync acknowledgements, notifications, and background operations
- [ ] 5.4 Add account-switch tests proving one cached family cannot be viewed or mutated by another account or local mode
- [ ] 5.5 Add the Desktop startup state machine for restore, login, data decision, initial sync, authenticated content, local mode, and recoverable failure
- [ ] 5.6 Add session-ready, foreground, throttled periodic, post-mutation, and manual sync triggers
- [ ] 5.7 Keep all Desktop sync attempts single-flight and preserve pending data on failure
- [ ] 5.8 Refresh visible Desktop repositories after successful pulls without clearing active form input
- [ ] 5.9 Add same-account Android/Desktop restore, account switching, and last-write-wins integration tests

## 6. Account Experience

- [ ] 6.1 Add Spanish Desktop Google sign-in, authentication progress, and recoverable failure UI
- [ ] 6.2 Update Account to show authenticated user, family, last sync, active progress, retry, manual sync, and secure sign-out
- [ ] 6.3 Preserve exact database and data-directory visibility in local and authenticated modes
- [ ] 6.4 Disclose excluded local-only records without claiming they were uploaded or deleted

## 7. Release Security Verification

- [ ] 7.1 Add tests proving callback listeners never bind external interfaces and accept only one validated request
- [ ] 7.2 Add tests proving release code cannot persist credentials outside operating-system vaults
- [ ] 7.3 Run authenticated RLS tests proving cross-family denial plus profile and family privilege-escalation denial
- [ ] 7.4 Inspect packaged artifacts for service-role keys, OAuth client secrets, database passwords, tokens, and sensitive logs
- [ ] 7.5 Verify signed Windows and signed/notarized macOS packages with real Google login, restore, refresh, sign-out, and offline recovery
- [ ] 7.6 Run strict OpenSpec validation and full quality, test, Android assembly, and Desktop packaging checks
