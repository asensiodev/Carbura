## ADDED Requirements

### Requirement: Post-Login Profile Validation
The login flow SHALL validate Supabase connectivity by reading the authenticated user's remote profile after sign-in.

#### Scenario: Authenticated profile exists
- **WHEN** login succeeds and `user_profiles` contains a row for the authenticated user
- **THEN** the login flow treats remote connectivity as ready for family-scoped MVP content

#### Scenario: Authenticated profile is missing
- **WHEN** login succeeds but the authenticated user has no readable `user_profiles` row
- **THEN** the login flow exposes a recoverable missing-profile state instead of entering family-scoped content silently
