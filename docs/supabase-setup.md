# Supabase Setup

Este documento guia la configuracion inicial de Supabase para Carbura. No incluye secretos reales y debe usarse junto con `local.properties.example`.

## Uso En Carbura

Supabase se usa como backend remoto para:

- PostgreSQL: datos compartidos del garaje familiar.
- Auth: autenticacion con Google en un cambio posterior.
- RLS: aislamiento de datos por `family_id`.

La app Android seguira siendo Android-first. La conexion completa desde KMP se implementara en cambios posteriores.

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

## Aplicar Migracion Inicial

Opcion recomendada para empezar:

1. Abre `supabase/migrations/202607010001_initial_schema.sql`.
2. Copia el contenido completo.
3. En Supabase Dashboard, abre `SQL Editor`.
4. Pega el SQL.
5. Ejecuta la query.
6. Revisa en `Table Editor` que existan las tablas:
   - `families`
   - `user_profiles`
   - `vehicles`
   - `maintenance_types`
   - `maintenance_records`
   - `reminders`

Alternativa posterior: usar Supabase CLI para aplicar migraciones desde local. No es obligatorio para Entrega 2.

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
4. Crea un usuario de prueba desde Supabase Auth o mediante login cuando el cambio de auth exista.
5. Inserta una `family` con `created_by` igual al usuario autenticado.
6. Inserta un `user_profile` con `user_id` igual al usuario autenticado y `family_id` de esa familia.
7. Comprueba que ese usuario puede leer/escribir vehiculos de su familia.
8. Comprueba con otro usuario que no puede leer datos de una familia ajena.

## Notas De Seguridad

- `SUPABASE_ANON_KEY` es publica para cliente, pero no debe mezclarse con claves privadas ni service role keys.
- Nunca commitear `service_role`, password de base de datos, OAuth client secret ni `local.properties`.
- El aislamiento real depende de RLS; la app no debe confiar solo en filtros de cliente.
