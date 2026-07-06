## 1. Supabase Configuration

- [x] 1.1 Add Supabase/Ktor dependencies needed for Android-first auth and PostgREST reads.
- [x] 1.2 Wire Android `local.properties` values into `BuildConfig` without committing real credentials.
- [x] 1.3 Add injectable Supabase settings and actionable missing-config errors.

## 2. Auth Boundary

- [x] 2.1 Implement Supabase-backed `AuthGateway` session mapping behind `core:auth`.
- [x] 2.2 Wire auth gateway into shared Koin modules.
- [x] 2.3 Add tests for session mapping and no-session behavior using fakes where SDK session construction is not practical.

## 3. Remote Connectivity Slice

- [x] 3.1 Add a remote gateway for authenticated user profile/family reads from Supabase.
- [x] 3.2 Keep remote gateway APIs out of feature ViewModels until a dedicated UI slice consumes them.
- [x] 3.3 Add mapping tests for remote profile/family DTOs.

## 4. Verification

- [x] 4.1 Run `./gradlew test`.
- [x] 4.2 Run `./gradlew assembleDebug`.
- [x] 4.3 Run `git diff --check` and inspect working tree status.
