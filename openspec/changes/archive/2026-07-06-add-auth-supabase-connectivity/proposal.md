## Why

Offline-first sync should not be designed against a theoretical remote. The app needs a verified Supabase client and authenticated session boundary before local SQLDelight data can be coordinated with remote data safely.

## What Changes

- Add Android-first Supabase configuration loading from local, ignored configuration into runtime-safe app settings.
- Implement a shared Supabase client boundary that can be injected without leaking Supabase APIs into domain or feature ViewModels.
- Implement an auth gateway for session state and a login entry point suitable for the Android MVP.
- Add a minimal remote read path against Supabase to validate authenticated connectivity before introducing offline-first sync.
- Keep offline-first repository orchestration and bidirectional sync out of scope for this change.

## Capabilities

### New Capabilities
- `auth-session`: Authentication/session boundary for Android MVP login state.
- `supabase-connectivity`: Supabase client configuration and minimal authenticated remote read validation.

### Modified Capabilities
- `supabase-backend`: Clarify client connectivity requirements against the existing backend schema and local configuration contract.

## Impact

- Affects `core:auth`, `core:data`, `app:shared`, `app:android`, Gradle version catalog and Android build configuration.
- Adds Supabase/Ktor dependencies and safe BuildConfig/local property wiring.
- Introduces tests around configuration/session/remote gateway mapping where feasible without real secrets.
