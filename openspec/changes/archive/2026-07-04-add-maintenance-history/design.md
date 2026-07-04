## Context

Carbura ya tiene el primer vertical slice del garaje: creacion de vehiculos, ViewModel compartido KMP, MVI completo, Koin y rutas compartidas preparadas. El dominio ya incluye contratos y casos de uso para crear registros de mantenimiento y consultar historial por vehiculo, pero aun no hay implementacion de repositorio ni UI.

## Goals / Non-Goals

**Goals:**

- Implementar un vertical slice de mantenimiento con repositorio en memoria, ViewModel MVI compartido y pantalla Android.
- Permitir seleccionar un vehiculo desde el garaje y abrir su historial.
- Permitir registrar un mantenimiento minimo y verlo ordenado por fecha descendente.
- Mantener la arquitectura actual: KMP commonMain para estado y logica, AndroidMain para Compose, DI con Koin y tests con Turbine.

**Non-Goals:**

- Persistencia real con SQLDelight o Supabase.
- Adjuntos, OCR, filtros avanzados o edicion de registros.
- Recordatorios automaticos desde ITV/seguro; queda para un cambio posterior.
- Auth, perfiles o family_id real; se mantiene el scope MVP local/in-memory.

## Decisions

- Usar `feature:maintenance` como modulo propietario de la UI y repositorio in-memory de mantenimiento. Alternativa: poner el repositorio en `core:data`, pero seria prematuro mientras no haya persistencia real compartida.
- Crear `MaintenanceHistoryViewModel` en `commonMain` con `MaintenanceHistoryUiState`, `MaintenanceHistoryEvent` y `MaintenanceHistoryEffect`. Alternativa: usar Controller Android-only, descartado para mantener la arquitectura compartida.
- Resolver navegacion Android con estado Compose simple entre `CarburaRoute.Garage` y `CarburaRoute.VehicleDetail`. Alternativa: montar Navigation 3 completo ahora; se difiere hasta que haya mas rutas y patrones estables.
- Mantener formulario minimo: tipo, fecha simple, kilometros, coste opcional, taller y notas. Alternativa: modelar catalogo visual completo de tipos, diferido para evitar sobredisenar.

## Risks / Trade-offs

- In-memory storage pierde datos al reiniciar proceso -> aceptado para el slice MVP; persistencia real se abordara en cambio posterior.
- Navegacion con estado simple no explota todo Navigation 3 -> aceptado; las rutas tipadas ya preparan la migracion al graph real.
- Fecha como entrada textual puede requerir validaciones basicas -> limitar a formato ISO `YYYY-MM-DD` para mantener testabilidad y compatibilidad con `CalendarDate`.
