# Hoja de ruta de sincronizacion de Carbura

Este documento fija el alcance de sincronizacion para no depender del contexto de conversaciones. La implementacion es KMP-first: dominio, contratos, estrategia de merge y repositorios compartidos viven en modulos comunes; Android aporta los triggers actuales y Desktop los incorporara en su futura aplicacion.

## Principios

- Local-first: la app debe seguir funcionando sin red.
- Supabase es la fuente remota compartida por familia.
- SQLDelight es la fuente local inmediata para UI y modo offline.
- La UI no debe bloquear creacion, borrado o consulta por falta de red.
- La sincronizacion debe ser observable: estado, ultima sync y errores discretos.
- La resolucion de conflictos debe ser simple al principio y evolucionar solo si el producto lo necesita.

## Sync v0 - Funcional MVP

Objetivo: sincronizacion end-to-end simple y funcional para Android y reutilizable por Desktop. Implementada en Entrega 2.

### Incluido

- Sincronizar vehiculos, mantenimientos y recordatorios.
- Guardar cambios locales con metadata de sync: `updated_at`, `pending_sync` y estrategia de borrado compatible con remoto.
- Subir cambios locales pendientes a Supabase.
- Bajar datos remotos de la familia activa.
- Resolver conflictos simples con `last-write-wins` usando `updated_at`.
- Ejecutar sync despues de login o restauracion de sesion.
- Ejecutar sync inicial al restaurar sesion para evitar mostrar garaje vacio antes del pull remoto.
- Ejecutar sync manual desde la pantalla Usuario.
- Accion manual `Sincronizar ahora`, probablemente en `Usuario`.
- Mostrar estado minimo: sincronizando, ultima sincronizacion y error no bloqueante.
- Tests de merge, pending sync y conservacion local ante error remoto.

Cada ciclo resuelve la familia activa, adopta filas antiguas asociadas a `local-family`, realiza un full pull para comparar con los cambios pendientes, sube solo los cambios cuya version remota no es mas reciente y termina con otro full pull. No existe cursor incremental; los borrados convergen como tombstones mediante `deleted_at`.

### Fuera de v0

- WorkManager/background sync con la app cerrada.
- Realtime Supabase subscriptions.
- Resolucion manual de conflictos.
- Merge avanzado por campo.
- Adjuntos, fotos, OCR o storage.
- Notificaciones remotas.
- Sync de invitaciones familiares completas.

### Triggers v0

- Android: session ready, app foreground, timer in-app, after local mutation, manual action.
- Desktop futuro: app start/session ready, timer mientras la ventana este activa, mutaciones y accion manual.

En Android los triggers reales son sesion/familia lista, `ON_START` con limitacion temporal, temporizador durante la composicion autenticada, mutaciones desde los ViewModels y accion manual. Un `Mutex` serializa las ejecuciones.

## Sync v1 - Robustez y UX

Objetivo: hacer la sincronizacion mas fiable y transparente cuando el uso real crezca.

### Posibles mejoras

- Backoff/retry mas explicito para errores temporales.
- Indicadores por entidad: pendiente, sincronizado, error.
- Mejor handling de auth/session refresh durante sync.
- Cola de operaciones mas estructurada si `pending_sync` simple se queda corto.
- Boton de reintentar errores concretos.
- Limpieza de tombstones/soft deletes antiguos.
- Mejor soporte Desktop si la app ya esta en uso real.

## Sync v2 - Colaboracion avanzada

Objetivo: cubrir escenarios de colaboracion familiar y datos frescos sin interaccion directa.

### Posibles mejoras

- WorkManager/background sync en Android para subir pendientes o bajar cambios aunque la app no este abierta.
- Realtime remoto para cambios familiares en sesiones activas.
- Notificaciones locales basadas en datos remotos nuevos.
- Resolucion manual o asistida de conflictos si `last-write-wins` no basta.
- Sync de adjuntos y documentos.
- Auditoria o historial de cambios si el modelo familiar lo requiere.

## Decision actual

`Sync v0` ya esta implementado. No se implementa WorkManager ni realtime todavia. El objetivo de v0 es funcionalidad completa pero acotada: datos locales y remotos convergen cuando la app se usa, sin prometer actividad con la app cerrada.
