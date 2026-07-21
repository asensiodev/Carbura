# Supabase Login Runtime Check

Use this checklist after installing the Android debug build on an emulator or device.

## Local configuration

- `local.properties` contains `SUPABASE_URL` with the project URL.
- `local.properties` contains `SUPABASE_ANON_KEY` with the public anon key.
- `local.properties` contains `GOOGLE_CLIENT_ID` — this must be the **Web OAuth client ID** from Google Cloud Console, **not** the Android client ID.

## Supabase configuration

- Google provider is enabled in Supabase Auth (Authentication → Providers → Google).
- The Web OAuth client ID from Google Cloud Console is pasted in the Supabase Google provider config.
- The app creates `families` and `user_profiles` automatically on first login.
- The `authenticated` role has table privileges. If you see `permission denied for table user_profiles`, apply `supabase/migrations/202607070001_grant_authenticated_table_access.sql` in the Supabase SQL Editor.

## Google Cloud Console configuration

1. Go to Google Cloud Console → APIs & Services → Credentials.
2. Create or select an **OAuth 2.0 Web Client** (type: Web application).
3. Copy the **Client ID** (looks like `xxxx.apps.googleusercontent.com`).
4. Paste it as `GOOGLE_CLIENT_ID` in `local.properties`.
5. Also paste it in Supabase Dashboard → Authentication → Providers → Google → **Client ID**.
6. Create or select an **OAuth 2.0 Android Client** for package `com.asensiodev.carbura`.
7. Add the debug/release SHA-1 (and SHA-256 if available) for the signing certificate used to install the app.

To inspect the local debug SHA, run:

```bash
./gradlew signingReport
```

If Android Credential Manager shows `16 account reauth failed`, re-check the Android OAuth client package name and SHA fingerprints first.

No need to configure redirect URIs or deep links — the app uses Android Credential Manager to obtain a Google ID token natively, then signs into Supabase via the `IDToken` provider.

## Expected app behavior

- Fresh install with no session opens the login screen.
- Tapping "Continuar con Google" opens the native Google account picker (Credential Manager).
- After selecting an account, the app signs into Supabase with the obtained ID token.
- Successful login creates the user's family/profile if missing, then opens the garage.
- "Cerrar sesion" returns to the login screen and clears protected navigation state.
