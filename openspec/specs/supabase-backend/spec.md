## Purpose
Define the versioned Supabase schema, relational integrity, synchronization metadata, and family-scoped security.
## Requirements
### Requirement: Versioned Supabase Schema
The system SHALL define the initial Supabase PostgreSQL schema as versioned SQL migrations in the repository.

#### Scenario: Initial migration exists
- **WHEN** a developer inspects the repository
- **THEN** the Supabase schema is available under `supabase/migrations/`

#### Scenario: Migration can be reviewed
- **WHEN** the schema changes
- **THEN** the change is visible as SQL in Git before being applied to Supabase

### Requirement: MVP Data Tables
The Supabase schema SHALL include tables for families, user profiles, vehicles, maintenance types, maintenance records and reminders.

#### Scenario: Family garage data is represented
- **WHEN** the initial migration is applied
- **THEN** the database contains the tables needed to represent a family garage and its vehicles

#### Scenario: Maintenance history is represented
- **WHEN** the initial migration is applied
- **THEN** the database contains the tables needed to store maintenance records and reminders per vehicle

### Requirement: Relational Integrity
The Supabase schema SHALL enforce relational integrity between family, user profile, vehicle, maintenance record and reminder data using primary keys, foreign keys and basic constraints.

#### Scenario: Vehicle belongs to a family
- **WHEN** a vehicle row is inserted
- **THEN** it must reference an existing family

#### Scenario: Maintenance belongs to a vehicle
- **WHEN** a maintenance record row is inserted
- **THEN** it must reference an existing vehicle; its maintenance type may use `maintenance_type_id` or the optional maintenance type keys supported by sync v0

### Requirement: Sync Metadata
The Supabase schema SHALL include sync metadata for synchronizable tables using creation time, update time and optional deletion time.

#### Scenario: Row timestamps are available
- **WHEN** synchronizable data is inserted or updated
- **THEN** `created_at`, `updated_at` and `deleted_at` fields are available for sync decisions

#### Scenario: Updated timestamp changes
- **WHEN** a synchronizable row is updated
- **THEN** its `updated_at` value is refreshed by the database

### Requirement: Family Scoped Security
The Supabase schema SHALL enable Row Level Security for family-scoped tables and restrict access to authenticated users whose server-controlled profile or membership authorizes the target family.

#### Scenario: User accesses own family data
- **WHEN** an authenticated user queries data for a family linked through secured server-managed membership
- **THEN** Supabase allows access according to the least-privilege policy

#### Scenario: User accesses another family data
- **WHEN** an authenticated user queries data for a family not linked through secured membership
- **THEN** Supabase denies read and mutation access even when the client supplies that family's identifier

#### Scenario: User attempts self-reassignment
- **WHEN** an authenticated client attempts to change its own `user_profiles.family_id` to an arbitrary family
- **THEN** Supabase rejects the direct mutation and does not grant access to that family

### Requirement: Safe Local Configuration
The project SHALL document the local Supabase configuration variables without committing real secrets or environment-specific values.

#### Scenario: Developer configures Supabase locally
- **WHEN** a developer sets up the project
- **THEN** the repository provides example variable names and instructions without exposing real keys

#### Scenario: Secrets remain local
- **WHEN** Supabase URL, anon key or OAuth values are configured
- **THEN** they are stored in local ignored files or external configuration, not committed to Git

### Requirement: Client Connectivity Uses Existing RLS Schema
The Android Supabase client SHALL access remote MVP data through the existing RLS-protected schema using authenticated user context.

#### Scenario: User profile is family scoped
- **WHEN** the Android client reads the authenticated user's profile
- **THEN** the query uses the existing `user_profiles` and family relationship allowed by Supabase RLS

#### Scenario: Vehicle reads are family scoped
- **WHEN** the Android client later reads vehicles remotely
- **THEN** the query uses the existing `vehicles` table and respects family-scoped RLS policies

### Requirement: Immutable Client Profile Ownership
Authenticated clients MUST NOT directly change `user_profiles.user_id` or `user_profiles.family_id`; family assignment and membership changes SHALL occur only through narrowly scoped server operations that derive the actor from `auth.uid()`.

#### Scenario: Client updates profile display fields
- **WHEN** an authenticated user updates an allowed self-service profile field
- **THEN** the update can change only explicitly granted fields and cannot alter user or family ownership

#### Scenario: Client inserts or deletes membership
- **WHEN** a client directly inserts, reassigns, or deletes a profile or membership outside an authorized RPC
- **THEN** Supabase rejects the operation

### Requirement: Restricted Family Administration
Family update, delete, and profile administration operations MUST require an explicitly authorized owner role or secured server operation rather than generic family membership.

#### Scenario: Non-owner member mutates family administration
- **WHEN** an authenticated family member without owner authority updates or deletes the family or another profile
- **THEN** Supabase rejects the operation

### Requirement: Versioned Authorization Hardening
The repository SHALL include a forward-only migration that replaces vulnerable broad family/profile policies and grants before Desktop authentication is released.

#### Scenario: Security migration is applied
- **WHEN** the migration runs on the existing schema
- **THEN** broad direct profile/family privileges are revoked, hardened policies or grants are installed, and existing valid user-family mappings remain intact

### Requirement: Hostile Authorization Verification
Backend verification MUST test profile and family privilege-escalation attempts in addition to ordinary cross-family data access.

#### Scenario: Modified client probes authorization
- **WHEN** tests attempt foreign family reads, writes, profile self-reassignment, profile administration, family update, and family delete
- **THEN** every unauthorized operation is denied while authorized same-family product operations continue to succeed
