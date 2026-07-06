## ADDED Requirements

### Requirement: Login Gate
The Android app SHALL gate MVP content behind the current authentication session.

#### Scenario: No session shows login
- **WHEN** the Android app starts and no authenticated session exists
- **THEN** the user sees the login/onboarding screen instead of the garage

#### Scenario: Existing session opens app content
- **WHEN** the Android app starts and an authenticated session exists
- **THEN** the user enters the garage without manually logging in again

### Requirement: Login MVI Contract
The onboarding feature SHALL expose login UI through separated shared MVI contracts.

#### Scenario: User starts Google login
- **WHEN** the user taps the Google login action
- **THEN** the onboarding ViewModel receives a login event and emits loading state while delegating to `AuthGateway`

#### Scenario: Login fails
- **WHEN** authentication fails or required Supabase configuration is missing
- **THEN** the login screen shows an actionable error state without crashing the app

#### Scenario: Login succeeds
- **WHEN** authentication succeeds and the authenticated profile can be read
- **THEN** the onboarding flow emits a one-off effect that lets the app navigate to the garage

### Requirement: Temporary Sign Out
The Android MVP SHALL provide a temporary sign-out action for validating session lifecycle during development.

#### Scenario: User signs out from authenticated state
- **WHEN** the authenticated user triggers sign out
- **THEN** the auth gateway clears the session and the app returns to the login screen
