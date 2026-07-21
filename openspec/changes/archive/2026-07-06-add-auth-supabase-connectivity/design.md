## Context

The project already has a Supabase schema and a `local.properties.example` containing `SUPABASE_URL`, `SUPABASE_ANON_KEY`, and `GOOGLE_CLIENT_ID`. `core:auth` currently exposes `AuthGateway` but only has platform placeholders. Local persistence is now implemented with SQLDelight, but remote sync should wait until authentication and remote connectivity are verified.

## Goals / Non-Goals

**Goals:**
- Load Supabase URL/anon key from ignored local configuration into Android BuildConfig.
- Provide a Supabase client through DI without exposing Supabase APIs to ViewModels or domain.
- Implement session inspection and login entry points behind `AuthGateway`.
- Add a minimal remote read gateway for user/family/vehicle connectivity validation.

**Non-Goals:**
- Full offline-first sync and conflict resolution.
- Supabase writes for vehicles/maintenance.
- Desktop/iOS auth flows.
- Complete onboarding or account/family creation UX.

## Decisions

### Android-first configuration

The Android app module will read `local.properties` at build time and expose non-secret runtime config through `BuildConfig`. The anon key is safe for clients by Supabase design, but it still remains environment-specific and must not be committed.

### Supabase hidden behind gateways

`core:data` or `core:auth` may depend on Supabase libraries. Domain contracts and feature ViewModels should depend only on gateway/repository interfaces and model types.

### Minimal auth first

The first implementation prioritizes session state and a login entry point. If full Google Credential Manager integration becomes too large, the change should still leave a clean `AuthGateway` implementation boundary and verified Supabase client creation.

### Remote read before sync

The remote slice will read family-scoped data from existing Supabase tables after authentication. This proves RLS/config/session before local/remote sync orchestration is introduced.

## Risks / Trade-offs

- Supabase auth SDK and Google Credential Manager can expand scope → keep UI/auth minimal and behind `AuthGateway`.
- Real Supabase reads require configured local credentials and remote test data → code should compile/test without secrets, and runtime should fail with actionable messages if missing.
- RLS may block reads until user profile/family rows exist → validate by reading the authenticated profile/family boundary first.
