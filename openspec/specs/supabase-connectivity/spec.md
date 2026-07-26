## Purpose
Define safe Supabase client configuration and authenticated remote profile validation.

## Requirements

### Requirement: Safe Supabase Client Configuration
The system SHALL configure the Android Supabase client from local ignored configuration values without committing environment-specific secrets.

#### Scenario: Config is present
- **WHEN** `SUPABASE_URL` and `SUPABASE_ANON_KEY` are available locally
- **THEN** the Android app can create an injectable Supabase client

#### Scenario: Config is missing
- **WHEN** required Supabase values are missing locally
- **THEN** the app exposes an actionable configuration error instead of silently using invalid credentials

### Requirement: Minimal Authenticated Remote Read
The system SHALL provide a minimal remote read gateway that validates Supabase connectivity for authenticated family-scoped data.

#### Scenario: Authenticated profile read succeeds
- **WHEN** an authenticated user has a Supabase profile/family row
- **THEN** the remote gateway returns the profile/family data allowed by RLS

#### Scenario: Remote read remains outside ViewModels
- **WHEN** a feature needs remote validation data
- **THEN** it accesses a project gateway/use case rather than depending directly on Supabase client APIs

### Requirement: Post-Login Profile Validation
The login flow SHALL validate Supabase connectivity by reading the authenticated user's remote profile after sign-in.

#### Scenario: Authenticated profile exists
- **WHEN** login succeeds and `user_profiles` contains a row for the authenticated user
- **THEN** the login flow treats remote connectivity as ready for family-scoped MVP content

#### Scenario: Authenticated profile is missing
- **WHEN** login succeeds but the authenticated user has no readable `user_profiles` row
- **THEN** the login flow exposes a recoverable missing-profile state instead of entering family-scoped content silently
