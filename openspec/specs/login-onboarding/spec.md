## Purpose
Define the authenticated Android entry flow, shared login MVI behavior, and sign-out access.
## Requirements
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

### Requirement: User-Facing Authentication Recovery
Android onboarding SHALL present actionable user-facing authentication errors without exposing raw exceptions, local configuration keys, or backend implementation details in release UI.

#### Scenario: Google sign-in fails
- **WHEN** Credential Manager or Supabase authentication fails
- **THEN** onboarding explains that sign-in could not complete and offers retry while technical detail remains outside primary release copy

### Requirement: Responsive And Accessible Onboarding
Android onboarding SHALL remain scrollable, width-constrained, and accessible when errors are long, the window is short, or font scale is increased.

#### Scenario: Authentication error appears with large text
- **WHEN** an asynchronous authentication error is shown at large font scale
- **THEN** the error is announced, remains readable, and does not hide the retry action

### Requirement: Product-Facing Onboarding Presentation
Android onboarding SHALL present its access panel near the optical center when vertical space allows and SHALL describe Carbura's user benefit without naming backend services or infrastructure.

#### Scenario: Onboarding opens on a regular phone
- **WHEN** the unauthenticated destination has sufficient vertical space
- **THEN** the access panel is visually centered and explains family vehicle, maintenance, and reminder functionality in user-facing language

#### Scenario: Onboarding has constrained height
- **WHEN** landscape, large text, or an authentication error requires more vertical space
- **THEN** the panel remains scrollable and the Google access action remains reachable
