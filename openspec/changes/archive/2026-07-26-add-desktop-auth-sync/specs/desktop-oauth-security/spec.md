## ADDED Requirements

### Requirement: External Browser Google Authentication
Desktop SHALL authenticate Google users through the operating system browser and Supabase OAuth Authorization Code flow with PKCE S256.

#### Scenario: User starts Google login
- **WHEN** an unauthenticated Desktop user selects Continue with Google
- **THEN** Carbura starts one fresh Supabase PKCE S256 transaction, binds the loopback listener, and only then opens the authorization URL in the system browser

#### Scenario: Embedded browser is unavailable by design
- **WHEN** Desktop authentication is presented
- **THEN** Carbura does not collect Google credentials or render the Google login page in an embedded WebView

### Requirement: Loopback Callback Isolation
The Desktop OAuth callback listener MUST bind only to the loopback interface and MUST be temporary, one-shot, and strictly validated.

#### Scenario: Callback listener starts
- **WHEN** OAuth begins
- **THEN** the listener binds successfully to `127.0.0.1` before the browser opens and does not listen on `0.0.0.0`, LAN, or public interfaces

#### Scenario: Valid callback arrives
- **WHEN** one callback uses `GET`, matches the expected host and path, remains within five minutes, and contains exactly one authorization `code`
- **THEN** Carbura consumes it once, exchanges the code with the in-memory PKCE verifier, and closes the listener

#### Scenario: Invalid callback arrives
- **WHEN** a callback has an unexpected path, host, method, oversized request, duplicate `code` or `error` parameters, both code and error, a duplicated request, or no supported result
- **THEN** Carbura rejects it without exchanging credentials or exposing validation details to the caller

#### Scenario: OAuth provider returns an error
- **WHEN** the callback contains exactly one OAuth `error` and no code
- **THEN** Carbura terminates the active attempt, clears transient PKCE material, and presents a redacted semantic failure

#### Scenario: Authentication times out or is cancelled
- **WHEN** the callback does not complete within the configured timeout or the user cancels
- **THEN** Carbura closes the listener, clears transient OAuth material, and remains unauthenticated

#### Scenario: Callback port cannot be bound
- **WHEN** the exact configured loopback endpoint is unavailable
- **THEN** Carbura fails before opening the browser and does not fall back to an external interface or unregistered redirect

### Requirement: OAuth Replay and Substitution Protection
Desktop MUST use the Supabase-supported PKCE S256 transaction, permit only one active attempt, rely on Supabase Auth to validate provider-facing Google `state` and `nonce`, and prevent callback replay.

#### Scenario: Authorization code is intercepted
- **WHEN** another process obtains an authorization code without Carbura's PKCE verifier
- **THEN** the code cannot establish a Carbura session

#### Scenario: Previous callback is replayed
- **WHEN** a consumed or expired callback is submitted again
- **THEN** Carbura rejects it and does not alter the current session

#### Scenario: Concurrent login is requested
- **WHEN** another login starts while an OAuth attempt is active
- **THEN** Carbura rejects or explicitly cancels the previous attempt before creating new PKCE material

### Requirement: Secure Desktop Session Storage
Desktop MUST use macOS Keychain or Windows Credential Manager as the Supabase SDK's sole persisted session backend through a platform credential-store abstraction.

#### Scenario: Session is persisted
- **WHEN** login or token rotation produces refresh credentials
- **THEN** Carbura atomically writes a project-, environment-, and account-namespaced entry to the operating-system credential vault, removes superseded entries, and writes no session copy to SQLDelight, application files, preferences, command-line arguments, or environment variables

#### Scenario: Supabase SDK persistence initializes
- **WHEN** the Desktop Supabase Auth client is created
- **THEN** default file or preference session persistence is disabled or replaced by the operating-system vault adapter

#### Scenario: Secure storage is unavailable in release
- **WHEN** a release build cannot access its operating-system credential vault
- **THEN** Carbura fails closed, does not persist the session insecurely, and explains that secure sign-in storage is unavailable

#### Scenario: Session is restored
- **WHEN** Desktop starts with stored refresh credentials
- **THEN** Carbura refreshes and validates the Supabase session before exposing authenticated content

#### Scenario: Stored credentials are revoked
- **WHEN** session restoration or refresh is rejected as revoked or invalid
- **THEN** Carbura removes the stored credentials and returns to unauthenticated state without deleting local vehicle data

### Requirement: Secure Sign Out
Desktop sign-out MUST target only the current Desktop Supabase session, attempt its remote revocation, remove operating-system credential entries, clear in-memory tokens, preserve local application records, and leave other device sessions active.

#### Scenario: User signs out
- **WHEN** an authenticated Desktop user confirms sign-out
- **THEN** the session is invalidated, credential-vault entries are removed, protected UI state is cleared, and local data remains intact

#### Scenario: User signs out while offline
- **WHEN** remote revocation cannot be confirmed
- **THEN** Carbura still removes all local Desktop credentials and protected state, does not sign out Android, and accurately reports that remote expiry or administrative revocation remains server-managed

### Requirement: Authentication Secret Redaction
Desktop MUST NOT log or display authentication secrets.

#### Scenario: Authentication fails
- **WHEN** OAuth, callback exchange, refresh, or sign-out fails
- **THEN** logs and UI omit authorization codes, PKCE verifiers, state, nonce, access tokens, refresh tokens, ID tokens, cookies, client secrets, and full authorization URLs

### Requirement: Public Client Credentials Only
Desktop MUST operate as a public OAuth client and MUST NOT contain privileged backend credentials.

#### Scenario: Release artifact is inspected
- **WHEN** a packaged Desktop binary and configuration are examined
- **THEN** they contain no Google OAuth client secret, Supabase `service_role` key, database password, or other privileged server credential
