# Supabase Setup

Este documento guia la configuracion inicial de Supabase para Carbura. No incluye secretos reales y debe usarse junto con `local.properties.example`.

## Uso En Carbura

Supabase se usa como backend remoto para:

- PostgreSQL: datos compartidos del garaje familiar.
- Auth: autenticacion con Google mediante Supabase Auth.
- RLS: aislamiento de datos por `family_id`.

La app Android es Android-first en Entrega 2. La conexion Supabase ya se usa desde los modulos KMP para auth, perfil familiar y sync v0.

## Crear Proyecto

1. Entra en <https://supabase.com/dashboard>.
2. Crea un nuevo proyecto.
3. Guarda la password de base de datos fuera del repositorio.
4. En `Project Settings > API`, copia:
   - Project URL.
   - anon public key.
5. Crea un archivo local `local.properties` copiando `local.properties.example` y rellena:

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=xxxx
GOOGLE_CLIENT_ID=xxxx.apps.googleusercontent.com
```

No commitees `local.properties` ni claves reales.

## Aplicar Migraciones

Opcion recomendada:

1. En Supabase Dashboard, abre `SQL Editor`.
2. Ejecuta, en orden, las migraciones de `supabase/migrations/`:
   - `202607010001_initial_schema.sql`
   - `202607070001_ensure_user_profile_rpc.sql`
   - `202607080001_sync_v0_schema.sql`
   - `202607080002_sync_v0_text_entity_ids.sql`
3. Revisa en `Table Editor` que existan las tablas principales:
   - `families`
   - `user_profiles`
   - `vehicles`
   - `maintenance_types`
   - `maintenance_records`
   - `reminders`

Alternativa posterior: usar Supabase CLI para aplicar migraciones desde local si el flujo de backend crece.

## GitHub Integration

Supabase permite conectar GitHub para aplicar cambios de schema automaticamente al hacer push o merge. Para Carbura queda diferido porque ahora interesa mantener el flujo simple y controlado:

- El esquema ya vive en Git como SQL versionado.
- La aplicacion inicial puede hacerse manualmente desde SQL Editor.
- La integracion automatica se evaluara si el flujo de migraciones crece.

## Smoke Test Manual

Tras aplicar la migracion:

1. Verifica que Row Level Security esta activo en las tablas publicas creadas.
2. Verifica que existen policies en cada tabla.
3. Verifica que `maintenance_types` contiene los tipos globales iniciales.
4. Inicia sesion desde la app Android con Google.
5. Verifica que la RPC de perfil crea o carga `family` y `user_profile`.
6. Crea un vehiculo, mantenimiento o recordatorio desde la app.
7. Ejecuta sync manual desde la pantalla Usuario.
8. Comprueba que ese usuario puede leer/escribir datos de su familia y que otro usuario no puede leer datos ajenos.

## Notas De Seguridad

- `SUPABASE_ANON_KEY` es publica para cliente, pero no debe mezclarse con claves privadas ni service role keys.
- Nunca commitear `service_role`, password de base de datos, OAuth client secret ni `local.properties`.
- El aislamiento real depende de RLS; la app no debe confiar solo en filtros de cliente.
