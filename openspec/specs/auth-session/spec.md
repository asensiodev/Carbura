## Requirements

### Requirement: Auth Session Gateway
The system SHALL expose authentication session state through a shared gateway that hides platform and Supabase auth details from feature ViewModels.

#### Scenario: Current session is available
- **WHEN** an authenticated Supabase session exists
- **THEN** the auth gateway returns an `AuthSession` with user identity data

#### Scenario: No current session
- **WHEN** no authenticated Supabase session exists
- **THEN** the auth gateway returns no session without throwing

### Requirement: Android MVP Login Entry Point
The system SHALL provide an Android login entry point that authenticates the user through Supabase-compatible credentials.

#### Scenario: User starts login
- **WHEN** the Android user triggers login
- **THEN** the app delegates authentication to the auth gateway instead of feature UI depending on Supabase SDK APIs

#### Scenario: User signs out
- **WHEN** the user signs out
- **THEN** the auth gateway clears the authenticated session
