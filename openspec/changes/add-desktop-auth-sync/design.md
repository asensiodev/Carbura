## Context

Android authenticates with a Google ID token obtained through Credential Manager and uses Supabase Auth, `user_profiles.family_id`, SQLDelight, and `LocalFirstSyncManager`. Desktop currently starts without `authModule`, injects `FamilyId("local-family")`, and overrides the real Desktop `SyncManager` binding with a no-op success implementation. The Desktop data module already provides the Supabase profile gateway, remote sync source, local sync source, and real local-first sync manager.

Desktop may already contain valuable `local-family` data. The shared sync manager currently adopts that data into the authenticated family before push, so enabling real sync without an explicit decision would upload local records silently. Authentication tokens also require stronger storage than ordinary application preferences or the SQLDelight database.

## Goals / Non-Goals

**Goals:**
- Authenticate Desktop users with the same Google/Supabase identity used on Android.
- Use an external system browser and OAuth Authorization Code with PKCE S256.
- Restore authenticated sessions without storing refresh tokens in plaintext application storage.
- Resolve the existing authenticated family and enable the shared local-first sync engine.
- Require explicit, persisted consent before adopting local-only records.
- Keep local data usable when authentication or synchronization fails.
- Define verifiable callback, token, RLS, logging, packaging, and sign-out security controls.

**Non-Goals:**
- Family invitations, member administration, or switching among multiple families.
- Embedded WebView authentication.
- Shipping Linux Desktop support.
- Encrypting all non-credential application data in this change.
- Changing Android's Credential Manager login flow.

## Decisions

### Use external-browser OAuth with PKCE and a loopback callback

Desktop will open the Supabase Google authorization URL in the system browser. Before navigation, the app binds a temporary HTTP listener exclusively to `127.0.0.1` on the configured callback port. The request uses the Supabase SDK's supported Authorization Code flow with PKCE S256 and an exactly allowlisted callback path. Carbura permits one active OAuth attempt and retains its PKCE transaction only in memory.

The listener accepts only one `GET` request with the expected host and path and exactly one `code` or one OAuth `error`; duplicate security-sensitive parameters are rejected. It limits request and header size to 8 KiB, expires after five minutes, and closes on success, cancellation, or failure. The authorization code is exchanged only for the one live PKCE transaction. Supabase Auth owns and validates the provider-facing Google `state` and `nonce`; Carbura MUST NOT claim to independently validate values that Supabase does not return to the loopback callback. Loopback HTTP is permitted for native OAuth because traffic remains on the device; all Google and Supabase traffic remains HTTPS.

An embedded WebView was rejected because it expands credential-handling risk and conflicts with native-app OAuth guidance. A custom URI scheme was rejected for the first version because Windows and macOS registration differ and private schemes can be hijacked. A claimed HTTPS redirect is stronger for application ownership but requires domain and platform association infrastructure that is unnecessary for this increment.

### Keep OAuth SDK details behind shared auth boundaries

Feature and shell presentation will consume semantic authentication events and state. `core/auth` will own authorization URL creation, code exchange, refresh, and sign-out. A Desktop callback coordinator and browser launcher will be injected behind interfaces so callback validation can be tested without opening a browser.

### Store refresh credentials only in operating-system credential vaults

Desktop will introduce an `AuthSessionStore` abstraction with macOS Keychain and Windows Credential Manager implementations. It becomes the Supabase SDK's sole persistence backend; SDK default file or preference persistence is disabled. Vault entries are namespaced by application ID, Supabase project/environment, and account, and refresh-token rotation replaces entries atomically before obsolete values are removed. Refresh credentials and any persisted session secret MUST NOT be stored in SQLDelight, files under the application data directory, Java Preferences, logs, crash reports, command-line arguments, or environment variables.

Release builds fail closed if secure credential storage is unavailable. Debug tests use an in-memory fake. Access tokens remain in memory where possible, are refreshed through Supabase, and are removed from the vault on sign-out or unrecoverable revocation.

### Gate first sync behind profile resolution and local-data consent

After authentication, Desktop ensures or resolves the user's existing profile and family. Before invoking `LocalFirstSyncManager`, it counts active or pending `local-family` records and checks a migration decision keyed to the authenticated user/family.

If local records exist and no decision has been recorded for their immutable snapshot generation, protected synchronized workspaces remain blocked by a decision screen:
- Import and merge: transactionally reassign legacy records to the authenticated family, retain pending sync metadata, then run sync.
- Use account data: retain legacy records under `local-family`, mark the decision, and pull only authenticated-family data.
- Cancel: do not sync or mutate local records and return to local mode or sign-in choice.

The import approval is bound to a digest of the exact legacy record IDs, family ownership, deletion state, and update versions shown to the user. New or changed local records invalidate prior import approval and require a new decision. The real sync manager must no longer adopt legacy data unconditionally. Adoption becomes an explicit orchestration step or a policy supplied to sync. The decision and adoption operation are idempotent so interrupted startup cannot duplicate records or repeat consent for the same committed snapshot.

Before adoption or authenticated pull, Desktop performs a collision preflight. If a legacy ID already exists in the target family or incoming remote data, Desktop transactionally remaps the legacy ID and every child/source reference before any push or pull can overwrite a row. Excluded local records receive the same collision protection so `INSERT OR REPLACE` cannot destroy them.

### Scope all local operations to the active family

Every local read, mutation, tombstone, sync acknowledgement, notification operation, and background task will require the expected active `familyId` in addition to entity IDs. This prevents stale UI or work from account A mutating cached rows after account B or local mode becomes active. Account-switch tests cover display, updates, deletes, reminder processing, and sync acknowledgements.

### Use a session-aware Desktop shell state machine

Startup states are explicit: restoring session, local unauthenticated, authenticating, awaiting data decision, initial sync, authenticated content, and recoverable failure. Authenticated content is not shown for an unresolved family. An initial sync attempt completes before synchronized content is presented, but failure keeps local authenticated-family data available with retry feedback.

### Reuse shared local-first synchronization

Desktop removes `DesktopLocalSyncManager` once authenticated mode is enabled and uses the `SyncManager` from `dataModule`. Desktop adds session-ready, foreground, throttled periodic, post-mutation, and manual triggers with the same single-flight lock and last-write-wins behavior as Android. UI refreshes after successful pulls without discarding active form input.

### Harden profile and family authorization before release

The current initial migration allows broad profile and family updates. A new versioned migration is a release prerequisite: authenticated clients cannot directly change `user_profiles.user_id` or `family_id`; membership/family assignment occurs only through narrowly scoped RPCs that derive the actor from `auth.uid()`. Family update/delete and profile administration are limited to explicitly authorized owner behavior, and self-service profile updates cannot alter identity or family ownership.

The anon/publishable key may ship with the app; OAuth client secrets and Supabase `service_role` keys MUST NOT. Supabase RLS and RPC authorization must derive access from `auth.uid()` and secured membership, never a client-provided family ID alone. Hostile tests attempt self-reassignment to a foreign family plus profile/family insert, update, and delete operations. These tests are release blockers.

### Use session-local Desktop sign-out

Desktop sign-out targets the current Desktop Supabase session and MUST NOT globally revoke Android or other device sessions. Carbura attempts remote revocation of the Desktop refresh session when online, then always removes local vault entries and in-memory credentials. If revocation cannot be confirmed offline, UI states that local sign-out succeeded while the server session remains valid only until server expiry or administrative revocation; Carbura does not retain the token insecurely for retry.

### Redact authentication and synchronization telemetry

Logs and user-visible diagnostics may include stable error categories and correlation identifiers but never authorization URLs containing parameters, authorization codes, PKCE verifiers, access tokens, refresh tokens, ID tokens, cookies, OAuth client secrets, or full Supabase responses.

## Risks / Trade-offs

- [Risk] A malicious local process attempts callback interception. → Bind before opening the browser, use PKCE S256, one-active-attempt handling, strict callback parsing, and immediate listener shutdown.
- [Risk] The configured callback port is occupied. → Fail before authorization begins and offer retry; never fall back to a network interface or an unregistered redirect.
- [Risk] Local records upload without informed consent. → Bind approval to the exact local-data snapshot, block real sync until the decision is complete, and remove unconditional legacy adoption.
- [Risk] Imported or excluded legacy IDs collide with remote IDs. → Preflight and transactionally remap legacy IDs plus all references before authenticated pull or push.
- [Risk] Cached data from one account is mutated by another account or local mode. → Require active family context on every local and background operation.
- [Risk] Secure storage differs across operating systems. → Use platform adapters with shared contract tests and fail closed in release builds.
- [Risk] Session restoration exposes stale or revoked credentials. → Refresh before authenticated content, clear rejected credentials, and return to sign-in without deleting local data.
- [Risk] Last-write-wins overwrites expected values. → Preserve existing timestamp rules, show initial import scope, and test cross-device conflict cases.
- [Risk] Profile reassignment or broad family policies expose another family. → Ship the hardening migration first and test hostile profile/family operations as well as data-table access.
- [Trade-off] Fixed loopback port can suffer local denial of service. → Exact redirect allowlisting and bind-before-browser behavior are simpler and safer than broad wildcard redirect configuration for the first release.

## Migration Plan

1. Create and apply the profile/family RLS hardening migration, then run hostile authorization tests.
2. Confirm Supabase Google provider and exact Desktop redirect allowlist using `docs/desktop-auth-sync-setup.md`.
3. Add Desktop Supabase public settings wiring and secure credential-store abstractions.
4. Implement and test the hardened loopback OAuth coordinator.
5. Add session restoration and authenticated shell state.
6. Scope local operations to active family and add collision-safe legacy handling.
7. Refactor legacy adoption behind snapshot-bound explicit consent and persist migration decisions.
8. Enable the real Desktop sync manager and triggers.
9. Replace local-only Account copy with session, sync, sign-out, and storage information.
10. Validate same-account Android/Desktop sync, account switching, offline recovery, import choices, token redaction, RLS isolation, and signed packages.

Rollback disables Desktop authenticated entry and sync triggers while preserving both authenticated-family and `local-family` SQLDelight rows. Rollback MUST NOT delete credential-vault entries silently; a release rollback procedure must either continue session support or explicitly sign the user out.

## Open Questions

- Select the platform credential-store implementation/library after a focused dependency and maintenance review; the SDK must use it as the only persistence backend.
- Confirm whether release packaging can reserve the exact callback port across supported corporate endpoint environments; otherwise migrate to an allowlisted ephemeral-port pattern after Supabase configuration validation.
