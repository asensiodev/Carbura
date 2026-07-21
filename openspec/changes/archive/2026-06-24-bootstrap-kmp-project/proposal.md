## Why

Carbura necesita pasar de documentación a una base técnica ejecutable para la Entrega 2. Antes de implementar features de producto, el proyecto debe tener un scaffold Kotlin Multiplatform modular, verificable y preparado para crecer hacia Android, Desktop opcional e iOS futuro.

## What Changes

- Crear la base del proyecto Kotlin Multiplatform con Android como plataforma principal.
- Definir una estructura modular escalable con `app`, `core`, `feature` y `build-logic`.
- Introducir convention plugins Gradle para evitar duplicación de configuración.
- Preparar módulos core iniciales para modelo, dominio, datos, auth, design system y testing.
- Preparar features iniciales para onboarding, garaje, mantenimiento y recordatorios.
- Definir el patrón técnico de contratos comunes KMP y adapters por plataforma para integraciones nativas.
- Crear configuración local segura mediante `local.properties.example` sin secretos reales.
- Dejar comandos de verificación mínimos para comprobar que el scaffold compila.

## Capabilities

### New Capabilities

- `kmp-project-structure`: estructura base Kotlin Multiplatform modular, con módulos Gradle, convention plugins, design system inicial, contratos comunes y targets Android/Desktop preparados según alcance.

### Modified Capabilities

- None.

## Impact

- Afecta a la estructura raíz del repositorio, configuración Gradle y módulos iniciales.
- Introduce `build-logic` para convention plugins.
- Introduce módulos `app:*`, `core:*` y `feature:*`.
- Introduce una base para Android como plataforma principal de Entrega 2.
- No implementa todavía pantallas reales, Supabase completo, auth real, Desktop completo ni iOS.
