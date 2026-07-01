## Why

Carbura necesita una base remota segura para compartir el garaje familiar entre dispositivos y preparar la sincronizacion offline-first del MVP. Supabase PostgreSQL debe quedar definido como infraestructura versionada para evitar cambios manuales no trazables y habilitar Row Level Security desde el inicio.

## What Changes

- Añadir el esquema inicial de Supabase para familias, perfiles de usuario, vehiculos, tipos de mantenimiento, registros de mantenimiento y recordatorios.
- Versionar migraciones SQL en el repositorio bajo `supabase/migrations/`.
- Definir relaciones, constraints, indices, timestamps y campos basicos de sincronizacion.
- Activar Row Level Security por `family_id` para aislar datos entre garajes familiares.
- Documentar la configuracion manual inicial de Supabase sin commitear secretos.
- Mantener fuera de alcance la conexion completa de la app, el login Google Android y la sincronizacion bidireccional avanzada.

## Capabilities

### New Capabilities
- `supabase-backend`: Esquema remoto Supabase, politicas RLS y configuracion segura necesaria para persistir datos del garaje familiar.

### Modified Capabilities
- Ninguna.

## Impact

- Nuevos scripts SQL en `supabase/migrations/`.
- Nueva documentacion de setup Supabase en `docs/` o `docs/toolchain/`.
- Posibles ajustes de `local.properties.example` para variables publicas necesarias (`SUPABASE_URL`, `SUPABASE_ANON_KEY`, `GOOGLE_CLIENT_ID`).
- No introduce secretos en Git ni requiere cambios funcionales de UI en este change.
