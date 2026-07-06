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
