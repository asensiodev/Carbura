## Context

El repositorio contiene documentación de producto, PRD, backlog y plan de Entrega 2, pero todavía no tiene una base Kotlin Multiplatform ejecutable. La Entrega 2 requiere backend, frontend y base de datos conectados con el flujo principal casi completo, por lo que el primer paso debe ser crear una base técnica estable.

Carbura será Android-first para la Entrega 2. Desktop queda como objetivo opcional y iOS queda fuera del MVP, pero la arquitectura debe permitir añadir iOS en el futuro sin reescribir dominio, datos ni contratos.

## Goals / Non-Goals

**Goals:**

- Crear un scaffold Kotlin Multiplatform modular y verificable.
- Usar Android como target principal y no bloquear la entrega por Desktop.
- Preparar una arquitectura iOS-ready mediante contratos comunes y adapters por plataforma.
- Introducir convention plugins en `build-logic` para mantener Gradle escalable.
- Crear módulos `app`, `core` y `feature` desde el inicio.
- Crear un design system mínimo en `core:designsystem`.
- Dejar comandos de verificación claros para confirmar que el proyecto compila.

**Non-Goals:**

- Implementar pantallas reales del producto.
- Implementar autenticación real con Credential Manager.
- Configurar Supabase completo.
- Implementar persistencia SQLDelight completa.
- Implementar Desktop completo.
- Implementar iOS.

## Decisions

### Android-first con arquitectura multiplataforma

Android será la única plataforma obligatoria para el scaffold inicial. Desktop solo se añadirá si el template o configuración inicial no bloquea el build. iOS no se implementa, pero la arquitectura no debe depender de APIs Android en dominio ni datos compartidos.

Alternativa considerada: crear Android, Desktop e iOS desde el primer día. Se descarta para Entrega 2 porque aumenta el riesgo de dedicar demasiado tiempo a configuración antes de tener un MVP Android funcional.

### Modularización con `app`, `core`, `feature` y `build-logic`

La estructura objetivo será:

```text
build-logic/
app/
  android/
  desktop/         # opcional
core/
  model/
  domain/
  data/
  auth/
  designsystem/
  testing/
feature/
  onboarding/
  garage/
  maintenance/
  reminders/
```

`build-logic` contendrá convention plugins para evitar duplicar configuración Gradle en cada módulo. Esto mejora mantenibilidad y muestra una arquitectura profesional desde el inicio.

Alternativa considerada: un único módulo `shared` con packages internos. Se descarta porque el proyecto busca ser un showcase de ingeniería y crecer hacia varias plataformas/features.

### Contratos comunes y adapters por plataforma

Toda integración nativa debe seguir este patrón:

```text
commonMain contract
  -> androidMain adapter
  -> desktopMain adapter opcional
  -> iosMain adapter futuro
```

Este patrón aplica a auth, permisos, notificaciones, secure storage, deep links, archivos y APIs del sistema.

Para auth, el contrato vivirá en `core:auth`. Android implementará el adapter con Credential Manager + Google ID, manteniendo fallback Google Sign-In/OAuth. Desktop usará OAuth mediante navegador si entra en alcance. iOS futuro podrá implementar Google Sign-In iOS, Sign in with Apple o ambos.

### Design system compartido

`core:designsystem` contendrá el tema, tokens y componentes Compose base reutilizables. Android lo usará desde el inicio. Desktop podrá reutilizarlo si se implementa. iOS futuro podrá replicar tokens en SwiftUI o evaluar Compose Multiplatform para iOS.

No se busca un design system completo en este cambio; solo una base mínima que permita construir UI coherente.

### TDD limitado al scaffold verificable

En este cambio no hay lógica de dominio real, así que TDD se limitará a verificaciones de build y, si es viable, tests mínimos de módulos base. El TDD fuerte empezará en cambios posteriores como `add-initial-data-model` y `add-vehicle-management`.

## Risks / Trade-offs

- Modularización excesiva al inicio → Mitigación: crear módulos mínimos y evitar features vacías si complican el build.
- Convention plugins consumen tiempo → Mitigación: empezar con plugins mínimos para Android/KMP/Compose y añadir más solo cuando hagan falta.
- Desktop bloquea el scaffold → Mitigación: dejar `app:desktop` fuera o desactivado si impide compilar Android rápido.
- Template KMP no encaja con estructura objetivo → Mitigación: adaptar gradualmente, priorizando `./gradlew tasks` y `./gradlew assembleDebug`.
- Dependencias de auth prematuras → Mitigación: definir contratos, pero no implementar Credential Manager real hasta `add-auth-family-garage`.

## Migration Plan

No hay migración de código existente porque el repo todavía no contiene aplicación KMP. El cambio añadirá estructura nueva. Si el scaffold falla, se puede revertir el change antes de implementar features.

## Open Questions

- ¿Se incluirá `app:desktop` en el scaffold inicial o se añadirá solo cuando Android compile estable?
- ¿Se usará cliente Supabase Kotlin o Ktor directo en `core:network`? Esta decisión puede cerrarse en `add-initial-data-model` o `add-auth-family-garage`.
- ¿SQLDelight se configura en este scaffold o se pospone a `add-initial-data-model`? Recomendación: preparar módulo `core:database`, pero configurar SQLDelight completo en el cambio de datos.
