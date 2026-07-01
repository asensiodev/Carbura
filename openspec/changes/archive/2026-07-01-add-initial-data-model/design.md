## Context

El scaffold KMP ya contiene `core:model` y `core:domain` con tipos iniciales, pero todavia no expresa todo el dominio minimo del MVP. Supabase ya tiene un esquema remoto versionado, por lo que el modelo compartido debe alinearse con esas entidades sin depender de SQL, Supabase ni Android.

## Goals / Non-Goals

**Goals:**

- Definir el modelo de dominio compartido en `commonMain`.
- Usar identificadores tipados para evitar mezclar IDs de familia, vehiculo, mantenimiento y recordatorio.
- Representar vehiculos, tipos de mantenimiento, mantenimientos y recordatorios con tipos explicitos.
- Añadir use cases iniciales con validaciones testeables.
- Mantener contratos de repositorio independientes de persistencia local/remota.

**Non-Goals:**

- Implementar SQLDelight.
- Implementar Supabase client o DTOs remotos.
- Implementar UI Android, ViewModels o navegacion.
- Implementar Auth Google real.
- Implementar sincronizacion completa.

## Decisions

1. **Modelos en `core:model`, casos de uso en `core:domain`**

   Las entidades y value classes viven en `core:model`; los use cases y repositorios viven en `core:domain`. Esto mantiene las dependencias limpias y permite que UI, data y tests reutilicen los mismos tipos.

2. **Identificadores como value classes sobre `String`**

   Se mantiene el enfoque existente (`FamilyId`, `VehicleId`, etc.) usando `String` para facilitar UUIDs de Supabase, IDs generados localmente y tests sin introducir dependencia de UUID multiplataforma.

3. **Enums para conceptos cerrados del MVP**

   `VehicleType`, `MaintenanceTypeCode` y estados simples se modelan como enums cuando el dominio tiene un conjunto pequeño y conocido. Los tipos personalizados siguen siendo posibles mediante `MaintenanceType.name` y codigo opcional.

4. **Use cases devuelven resultados explicitos**

   Las validaciones de dominio devuelven sealed results en lugar de lanzar excepciones. Esto facilita tests, UI states y errores controlados.

5. **Repositorios suspend simples por ahora**

   Los contratos usan funciones `suspend` y listas ordenadas. No se introducen `Flow` ni paginacion hasta que UI/persistencia lo necesiten.

## Risks / Trade-offs

- **Sobremodelar antes de tener UI** -> Mitigar manteniendo solo entidades y casos de uso necesarios para Entrega 2.
- **Enums demasiado rigidos** -> Mitigar permitiendo `MaintenanceType` personalizados asociados a familia.
- **No usar `Flow` desde el inicio** -> Mitigar cambiando contratos en un change posterior si UI reactiva/persistencia lo requiere.
- **Validaciones duplicadas con base de datos** -> Mitigar aceptando validacion en capas: dominio para UX/testabilidad, base de datos para integridad.

## Migration Plan

1. Reemplazar modelos placeholder por modelos completos manteniendo nombres existentes cuando sea posible.
2. Añadir tests de dominio en `commonTest` antes o junto al codigo minimo.
3. Ejecutar `./gradlew test` y `./gradlew assembleDebug` para asegurar que el scaffold sigue compilando.

## Open Questions

- El formato final de generacion de IDs offline se definira en persistencia/sync posterior.
- El uso de `Flow` se reevaluara al conectar ViewModels y repositorios reales.
