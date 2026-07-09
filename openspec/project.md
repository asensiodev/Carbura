# Carbura Project Context

Carbura es una app multiplataforma (Android + Desktop) para gestionar el mantenimiento de los vehiculos de una familia: vehiculos, mantenimientos, recordatorios, historial, costes, sincronizacion offline-first y garaje compartido.

El PRD completo vive en `openspec/prd.md` y debe usarse como contexto de producto antes de crear o aplicar cambios OpenSpec.

## Producto

- Slogan: "Tu garaje, siempre a punto."
- Usuarios principales: propietario del vehiculo y familiares que comparten un garaje.
- MVP Entrega 2: Android-first con vehiculos, mantenimientos, recordatorios locales, notificaciones locales, historial, costes, sync v0 familiar, Google Auth y familia personal inicial.
- Fuera de Entrega 2: Desktop funcional, invitaciones familiares completas, exportacion PDF/CSV, iOS, combustible, OBD2, push remoto, OCR, recomendaciones IA, multi-familia y roles avanzados.

## Stack

- UI Android: Compose for Android.
- UI Desktop: Compose for Desktop si entra en alcance; reutiliza design system y componentes compartidos cuando sea practico.
- iOS futuro: preparado por arquitectura KMP; UI futura evaluable entre SwiftUI y Compose Multiplatform, sin implementacion en el MVP.
- Logica compartida: Kotlin Multiplatform en `commonMain` para dominio, use cases, contratos, modelos, validaciones, UiState y logica testeable.
- Modularizacion: modulos Gradle desde el inicio con convention plugins en `build-logic`.
- Design system: modulo `core:designsystem` con tema, tokens y componentes Compose reutilizables.
- Base de datos local: SQLDelight.
- HTTP client: Ktor Client.
- Integraciones nativas: contratos en `commonMain` y adapters por plataforma (`androidMain`, `desktopMain`, `iosMain` futuro) para auth, permisos, notificaciones, storage seguro y APIs de sistema.
- Auth cliente Android: adapter con Credential Manager + Google ID como opcion principal y fallback controlado a Google Sign-In/OAuth si el dispositivo no lo soporta.
- Auth cliente Desktop: adapter OAuth mediante navegador o flujo equivalente, solo si Desktop entra en el alcance de la entrega.
- DI: Koin.
- Serializacion: kotlinx.serialization.
- Backend: Supabase Auth, PostgreSQL y Storage.
- Sync: timestamp-based, last-write-wins.

## Arquitectura

Usar Clean Architecture en capas:

```text
Presentation (Compose Android / Compose Desktop)
      ↓
ViewModel + UiState (commonMain)
      ↓
Use Cases / Domain (commonMain)
      ↓
Repository contracts (commonMain)
      ↓
Platform adapters + LocalDataSource (SQLDelight) + RemoteDataSource (Ktor + Supabase)
      ↓
SyncManager (commonMain)
```

## Metodologia

- SDD con OpenSpec: proposal -> apply -> archive.
- TDD dentro de cada tarea: Red -> Green -> Refactor.
- DDD ligero para modelar entidades, use cases y repositorios sin sobredisenar el MVP.
- SOLID y CUPID durante el refactor, priorizando testabilidad, claridad y simplicidad.
- Las specs son la fuente de verdad funcional.
- Los criterios de aceptacion de historias/specs deben traducirse en tests.
- BDD queda fuera del alcance metodologico del MVP.
- Commits con Conventional Commits: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`.

## Reglas de seguridad

- No commitear secrets, tokens, claves Supabase ni OAuth client secrets.
- Usar `local.properties` o variables locales para credenciales.
- Mantener Row Level Security por `family_id` en Supabase.
