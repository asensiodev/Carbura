# Contexto del proyecto Carbura

Carbura es una aplicacion local-first para gestionar vehiculos familiares, mantenimientos, recordatorios, historial y costes. Android es el producto ejecutable actual; la vision mantiene Desktop para macOS y Windows como siguiente plataforma y contempla iOS como posible evolucion sobre la base Kotlin Multiplatform.

El PRD completo vive en `openspec/prd.md` y debe usarse como contexto de producto antes de crear o aplicar cambios OpenSpec.

## Producto

- Slogan: "Tu garaje, siempre a punto."
- Usuarios principales: propietario del vehiculo y familiares que comparten un garaje.
- MVP Entrega 2: Android-first con vehiculos, mantenimientos, recordatorios locales, notificaciones locales, historial, costes, sync v0 familiar, Google Auth y familia personal inicial.
- Evolucion de producto: Desktop funcional, invitaciones familiares completas y exportacion PDF/CSV; iOS queda sujeto a evaluacion posterior. Combustible, OBD2, push remoto, OCR, recomendaciones IA, multi-familia y roles avanzados permanecen fuera del MVP.

## Stack

- UI Android: Compose for Android.
- UI Desktop futura: Compose for Desktop, reutilizando la base KMP cuando sea practico.
- UI iOS posible: SwiftUI o Compose Multiplatform a evaluar en una fase posterior.
- Logica compartida: Kotlin Multiplatform en `commonMain` para dominio, use cases, contratos, modelos, validaciones, UiState y logica testeable.
- Modularizacion: modulos Gradle desde el inicio con convention plugins en `build-logic`.
- Design system: modulo `core:designsystem` con tema y tokens Android; su evolucion multiplataforma forma parte de la vision futura.
- Base de datos local: SQLDelight.
- HTTP client: Ktor Client.
- Integraciones nativas: contratos en `commonMain`, adaptadores productivos Android y adaptadores Desktop/iOS cuando esas plataformas entren en alcance.
- Auth cliente Android: Credential Manager + Google ID; los errores permiten reintentar y el fallback OAuth alternativo queda pendiente.
- Auth cliente Desktop: adapter OAuth mediante navegador o flujo equivalente, solo si Desktop entra en el alcance de la entrega.
- DI: Koin.
- Serializacion: kotlinx.serialization.
- Backend: Supabase Auth, PostgreSQL, PostgREST y RLS. Storage queda previsto para adjuntos futuros.
- Sync: full pull de vehiculos, mantenimientos y recordatorios, push de pendientes, tombstones y `last-write-wins` mientras la app esta activa.

## Arquitectura

Usar Clean Architecture en capas:

```text
Presentation (Compose Android / Compose Desktop futuro)
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

- La documentacion de producto y academica se redacta en espanol. Los artefactos tecnicos de OpenSpec (`proposal.md`, `design.md`, `tasks.md` y deltas de specs) se redactan en ingles, preservando identificadores y terminos tecnicos.
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
