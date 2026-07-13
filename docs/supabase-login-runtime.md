# Comprobacion del login con Supabase

Usa esta lista despues de instalar la compilacion debug de Android en un emulador o dispositivo.

## Configuracion local

- `local.properties` contiene `SUPABASE_URL` con la URL del proyecto.
- `local.properties` contiene `SUPABASE_ANON_KEY` con la anon key publica.
- `local.properties` contiene `GOOGLE_CLIENT_ID`: debe ser el **Client ID OAuth web** de Google Cloud Console, no el Client ID de Android.

## Configuracion de Supabase

- El proveedor Google esta habilitado en Supabase Auth (`Authentication > Providers > Google`).
- El Client ID OAuth web esta configurado en el proveedor Google de Supabase.
- La RPC `ensure_user_profile` recupera o crea automaticamente `families` y `user_profiles` en el primer login.
- La migracion `supabase/migrations/202607070001_ensure_user_profile_rpc.sql` contiene la RPC, sus permisos de ejecucion y los grants de tablas para `authenticated`.

## Configuracion de Google Cloud Console

1. Abre `APIs & Services > Credentials` en Google Cloud Console.
2. Crea o selecciona un cliente **OAuth 2.0 Web** de tipo aplicacion web.
3. Copia su **Client ID** (`xxxx.apps.googleusercontent.com`).
4. Asignalo a `GOOGLE_CLIENT_ID` en `local.properties`.
5. Configuralo tambien como **Client ID** del proveedor Google en Supabase.
6. Crea o selecciona un cliente **OAuth 2.0 Android** para el package `com.asensiodev.carbura`.
7. Registra SHA-1 y, si esta disponible, SHA-256 del certificado debug/release usado para instalar la app.

To inspect the local debug SHA, run:

```bash
./gradlew signingReport
```

Si Credential Manager muestra `16 account reauth failed`, comprueba primero el package y las huellas SHA del cliente OAuth Android.

No se necesitan URI de redireccion ni deep links: Credential Manager obtiene de forma nativa el Google ID token y la app inicia sesion en Supabase mediante el proveedor `IDToken`.

## Comportamiento esperado

- Una instalacion sin sesion abre la pantalla de login.
- `Continuar con Google` abre el selector nativo de cuentas de Credential Manager.
- Tras seleccionar una cuenta, la app inicia sesion en Supabase con el ID token obtenido.
- Un login correcto recupera o crea familia y perfil antes de abrir el garaje.
- `Cerrar sesion` vuelve al login y limpia el estado de navegacion protegido.
