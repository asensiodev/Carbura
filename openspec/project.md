# Contexto del proyecto Carbura

Carbura es una aplicacion local-first para gestionar vehiculos familiares, mantenimientos, recordatorios, historial y costes. Android y Desktop para macOS/Windows son productos ejecutables sobre una base Kotlin Multiplatform; iOS queda fuera del alcance actual.

El PRD completo vive en `openspec/prd.md` y debe usarse como contexto de producto antes de crear o aplicar cambios OpenSpec.

## Producto

- Slogan: "Tu garaje, siempre a punto."
- Usuarios principales: propietario del vehiculo y familiares que comparten un garaje.
- Entrega funcional: Android y Desktop con vehiculos, mantenimientos, recordatorios, historial, sync v0 familiar, Google Auth, familia personal, modo local y eliminacion de cuenta.
- Limite nativo: Android programa notificaciones locales; Desktop persiste y sincroniza recordatorios sin alertas nativas. Invitaciones, exportacion, iOS, combustible, OBD2, push remoto, OCR, recomendaciones IA, multi-familia y roles avanzados quedan fuera.

## Stack

- UI Android: Compose for Android.
- UI Desktop: Compose Desktop para macOS y Windows.
- Logica compartida: Kotlin Multiplatform en `commonMain` para dominio, use cases, contratos, modelos, validaciones, UiState y logica testeable.
- Modularizacion: modulos Gradle desde el inicio con convention plugins en `build-logic`.
- Design system: modulo `core:designsystem` con tema y tokens Android; su evolucion multiplataforma forma parte de la vision futura.
- Base de datos local: SQLDelight.
- HTTP client: Ktor Client.
- Integraciones nativas: adaptadores Android y Desktop sobre contratos compartidos cuando procede.
- Auth cliente Android: Credential Manager + Google ID.
- Auth cliente Desktop: navegador, Authorization Code, PKCE S256, callback loopback y Keychain/Credential Manager.
- DI: Koin.
- Serializacion: kotlinx.serialization.
- Backend: Supabase Auth, PostgreSQL, PostgREST y RLS. Storage queda previsto para adjuntos futuros.
- Sync: full pull de vehiculos, mantenimientos y recordatorios, push de pendientes, tombstones y `last-write-wins` mientras la app esta activa.

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
- No almacenar sesiones Desktop fuera de Keychain o Windows Credential Manager.
- No programar notificaciones nativas en Desktop.
