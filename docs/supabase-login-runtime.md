# Supabase Login Runtime Check

Use this checklist after installing the Android debug build on an emulator or device.

## Local configuration

- `local.properties` contains `SUPABASE_URL` with the project URL.
- `local.properties` contains `SUPABASE_ANON_KEY` with the public anon key.
- `local.properties` contains `GOOGLE_CLIENT_ID` for the Android MVP OAuth client.

## Supabase configuration

- Google provider is enabled in Supabase Auth.
- The OAuth redirect/deep link configured in Supabase matches the Android auth flow used by the SDK.
- The signed-in user has a readable row in `user_profiles`.
- The `user_profiles.family_id` points to a family allowed by RLS.

## Expected app behavior

- Fresh install with no session opens the login screen.
- Tapping "Continuar con Google" starts Supabase Google sign-in.
- Successful login with a readable profile opens the garage.
- Successful login without a readable profile shows the missing-profile error.
- "Cerrar sesion" returns to the login screen and clears protected navigation state.
