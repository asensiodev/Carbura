## MODIFIED Requirements

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

## ADDED Requirements

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
