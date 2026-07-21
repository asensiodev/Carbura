## Why

Carbura necesita un modelo de dominio compartido antes de conectar UI, persistencia local o Supabase desde la app. Definir entidades, validaciones y contratos en KMP evita acoplar la aplicacion a detalles de base de datos y habilita TDD barato en `commonTest`.

## What Changes

- Completar los modelos compartidos del MVP en `core:model`: familia, perfil de usuario, vehiculo, tipo de mantenimiento, registro de mantenimiento y recordatorio.
- Añadir value classes/identificadores y enums necesarios para expresar el dominio sin usar strings ambiguos.
- Añadir validaciones de dominio minimas para crear vehiculos y registros de mantenimiento.
- Ampliar contratos de repositorio en `core:domain` para vehiculos, mantenimientos y recordatorios.
- Añadir use cases iniciales testeados para crear vehiculo, crear mantenimiento, consultar historial y generar recordatorios basicos.
- Mantener fuera de alcance SQLDelight, cliente Supabase, UI Android y Auth real.

## Capabilities

### New Capabilities
- `initial-data-model`: Modelo de dominio KMP, contratos y use cases base para el garaje familiar.

### Modified Capabilities
- Ninguna.

## Impact

- `core:model`: entidades, identificadores, enums y tipos de resultado/validacion simples.
- `core:domain`: contratos de repositorio y use cases iniciales.
- `core:testing` o `commonTest`: fakes/datos de prueba si hacen falta para tests de dominio.
- Tests unitarios KMP ejecutables con `./gradlew test`.
