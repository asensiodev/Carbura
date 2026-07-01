## 1. Supabase Migration Structure

- [x] 1.1 Create `supabase/migrations/` in the repository.
- [x] 1.2 Add an initial timestamped migration for the MVP Supabase schema.
- [x] 1.3 Ensure the migration can be reviewed as plain SQL and contains no secrets.

## 2. Database Schema

- [x] 2.1 Add required PostgreSQL extensions and shared timestamp helper function.
- [x] 2.2 Create `families` and `user_profiles` tables with UUID primary keys and auth user linkage.
- [x] 2.3 Create `vehicles` table with family ownership, basic vehicle fields and sync metadata.
- [x] 2.4 Create `maintenance_types` table for global and family-specific maintenance categories.
- [x] 2.5 Create `maintenance_records` table with vehicle, type, date, odometer, cost and notes fields.
- [x] 2.6 Create `reminders` table with date/km reminder fields and completion state.
- [x] 2.7 Add foreign keys, basic constraints and useful indexes for family and vehicle queries.
- [x] 2.8 Add `updated_at` triggers for synchronizable tables.

## 3. Row Level Security

- [x] 3.1 Enable RLS on all family-scoped tables.
- [x] 3.2 Add helper SQL logic or policy predicates to check membership through `user_profiles`.
- [x] 3.3 Add policies that allow authenticated users to access only their own family data.
- [x] 3.4 Add policies for global maintenance types while keeping family-specific types isolated.

## 4. Local Configuration And Documentation

- [x] 4.1 Review `local.properties.example` and update Supabase variable names if needed.
- [x] 4.2 Add documentation for creating the Supabase project and applying the migration manually.
- [x] 4.3 Document that GitHub-Supabase integration is optional and deferred for the MVP.
- [x] 4.4 Document a manual smoke test checklist for tables and RLS.

## 5. Verification

- [x] 5.1 Run `git diff --check`.
- [x] 5.2 Verify there are no committed secrets or real Supabase credentials.
- [x] 5.3 Run `openspec status --change "add-supabase-backend"` and confirm the change is ready to apply/archive after implementation.
