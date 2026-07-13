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
The Supabase schema SHALL enable Row Level Security for family-scoped tables and restrict access to authenticated users that belong to the target family.

#### Scenario: User accesses own family data
- **WHEN** an authenticated user queries data for a family linked to their profile
- **THEN** Supabase allows access according to the policy

#### Scenario: User accesses another family data
- **WHEN** an authenticated user queries data for a family not linked to their profile
- **THEN** Supabase denies access according to the policy

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
