## Why

Carbura Desktop currently operates under a synthetic local family and cannot access the authenticated family data used on Android. Secure Google authentication and shared synchronization are required so the same account presents the same vehicles, maintenance, and reminders on both platforms without silently uploading existing Desktop-only data.

## What Changes

- Add Desktop Google OAuth Authorization Code flow using the system browser, PKCE S256, and a temporary loopback callback restricted to the local machine.
- Persist and restore Desktop sessions through operating-system credential storage rather than application files or the SQLDelight database.
- Resolve the authenticated user's existing Supabase profile and family before exposing synchronized product data.
- Ask for explicit consent before adopting existing `local-family` records into the authenticated family.
- Replace the Desktop no-op sync override with the shared local-first synchronization engine.
- Add initial, foreground, periodic, post-mutation, and manual Desktop sync behavior with non-destructive failure feedback.
- Add session-local secure sign-out, best-effort Desktop refresh-session revocation, callback hardening, and token redaction requirements.
- Document required Supabase and Google configuration plus release security checks.

## Capabilities

### New Capabilities
- `desktop-oauth-security`: Secure external-browser Google OAuth, loopback callback handling, and operating-system credential storage.
- `desktop-local-data-adoption`: Explicit first-login handling of existing local-only Desktop data.

### Modified Capabilities
- `auth-session`: Extend session startup and sign-out behavior to Desktop.
- `sync-v0`: Add Desktop synchronization triggers, status, and authenticated-family behavior.
- `desktop-local-account`: Replace the permanent local-only workspace with session-aware account, sync, and data-origin controls.
- `supabase-backend`: Harden profile and family RLS so a client cannot grant itself access by changing `family_id`.
- `local-persistence`: Enforce active-family isolation and collision-safe local adoption across account changes.

## Impact

- Affects `app/desktop`, `core/auth`, `core/data`, shared sync/domain contracts, and Desktop packaging.
- Requires Supabase redirect allowlisting and verification of the existing Google provider configuration.
- Requires a versioned Supabase security migration before Desktop authentication is released.
- Requires platform credential-store adapters for macOS Keychain and Windows Credential Manager.
- Reuses the existing `user_profiles.family_id`, shared sync engine, SQLDelight metadata, and Supabase RLS model; family invitations and family switching remain deferred.
