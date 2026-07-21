## Context

Carbura ya tiene un scaffold KMP modular y un PRD que define Supabase como backend remoto para Auth, PostgreSQL y Storage. El MVP necesita una base relacional remota para familias, perfiles, vehiculos, mantenimientos y recordatorios, con aislamiento de datos por garaje familiar.

El repositorio todavia no contiene migraciones Supabase ni una forma versionada de recrear el esquema remoto. Crear el esquema solo desde el Dashboard seria rapido, pero dejaria poca trazabilidad para OpenSpec, PRs y evaluacion del TFM.

## Goals / Non-Goals

**Goals:**

- Versionar el esquema inicial de Supabase en `supabase/migrations/`.
- Definir tablas, relaciones, indices, timestamps y campos de sincronizacion necesarios para el MVP.
- Activar RLS y policies por `family_id` desde el primer esquema.
- Documentar como crear el proyecto Supabase y aplicar la migracion sin guardar secretos en Git.
- Mantener el backend preparado para una integracion posterior desde KMP mediante Ktor/Supabase client.

**Non-Goals:**

- Implementar el login Google en Android.
- Implementar repositorios remotos KMP o sincronizacion bidireccional.
- Automatizar despliegues con GitHub conectado a Supabase.
- Cubrir roles avanzados, multi-familia o auditoria historica completa.
- Implementar Storage de adjuntos en este cambio.

## Decisions

1. **Usar migraciones SQL versionadas en Git**

   El esquema se definira en `supabase/migrations/<timestamp>_initial_schema.sql`. Esto permite revisar cambios en PR, reproducir el backend y usar el SQL Editor o Supabase CLI para aplicarlo.

   Alternativa considerada: crear tablas manualmente en el Dashboard. Se descarta como fuente principal porque dificulta trazabilidad y reproducibilidad.

2. **PostgreSQL con UUIDs generados en base de datos**

   Las tablas usaran `uuid` como clave primaria con `gen_random_uuid()`. Esto encaja con Supabase/PostgreSQL y permite sincronizacion entre dispositivos sin depender de IDs incrementales globales.

   Alternativa considerada: IDs seriales. Se descarta porque complican creacion offline y merge posterior.

3. **Aislamiento por `family_id` y RLS**

   Las entidades familiares incluiran `family_id` y las policies comprobaran que el usuario autenticado pertenece a esa familia mediante `user_profiles.user_id = auth.uid()`.

   Alternativa considerada: filtrar solo desde cliente. Se descarta por seguridad; el aislamiento debe vivir en base de datos.

4. **Soft delete y timestamps para sync**

   Las tablas sincronizables incluiran `created_at`, `updated_at` y `deleted_at`. `updated_at` se actualizara con trigger. Esto prepara last-write-wins sin implementar todavia el SyncManager completo.

   Alternativa considerada: eliminacion fisica inmediata. Se descarta porque dificulta sincronizar borrados entre dispositivos.

5. **Aplicacion manual/CLI antes de GitHub integration**

   Para Entrega 2 se documentara aplicar migraciones con SQL Editor o Supabase CLI. La conexion GitHub-Supabase queda opcional para evitar complejidad operacional prematura.

   Alternativa considerada: activar despliegue automatico desde GitHub desde el inicio. Se pospone porque no es necesaria para el MVP y puede introducir fallos dificiles de depurar.

## Risks / Trade-offs

- **Policies RLS demasiado restrictivas** -> Mitigar con pruebas manuales en Supabase usando usuarios de prueba y consultas autenticadas.
- **Policies RLS demasiado permisivas** -> Mitigar definiendo acceso siempre via pertenencia a `family_id`, nunca por filtros del cliente.
- **Esquema inicial insuficiente para sync avanzado** -> Mitigar incluyendo timestamps, soft delete e indices, dejando resolucion de conflictos avanzada para cambios posteriores.
- **Supabase CLI añade friccion local** -> Mitigar permitiendo aplicacion manual inicial desde SQL Editor.
- **Auth Google depende de configuracion externa** -> Mitigar documentando variables y dejando el adapter Android para un change separado.

## Migration Plan

1. Crear proyecto Supabase desde Dashboard.
2. Copiar `SUPABASE_URL` y `SUPABASE_ANON_KEY` a `local.properties` local, no versionado.
3. Ejecutar la migracion inicial desde SQL Editor o Supabase CLI.
4. Verificar tablas, RLS y policies en Dashboard.
5. Crear datos de prueba manuales o con seed solo si no exponen secretos.

Rollback inicial: eliminar el proyecto Supabase de pruebas o revertir ejecutando drops manuales en un entorno no productivo. No hay datos reales de usuarios en esta fase.

## Open Questions

- Confirmar si el nombre final de tabla para usuarios sera `user_profiles` para evitar conflicto conceptual con `auth.users`.
- Confirmar si los tipos de mantenimiento globales se cargaran mediante seed o se generaran desde la app en el primer arranque.
