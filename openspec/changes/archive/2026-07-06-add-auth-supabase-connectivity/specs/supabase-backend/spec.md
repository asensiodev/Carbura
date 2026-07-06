## ADDED Requirements

### Requirement: Client Connectivity Uses Existing RLS Schema
The Android Supabase client SHALL access remote MVP data through the existing RLS-protected schema using authenticated user context.

#### Scenario: User profile is family scoped
- **WHEN** the Android client reads the authenticated user's profile
- **THEN** the query uses the existing `user_profiles` and family relationship allowed by Supabase RLS

#### Scenario: Vehicle reads are family scoped
- **WHEN** the Android client later reads vehicles remotely
- **THEN** the query uses the existing `vehicles` table and respects family-scoped RLS policies
