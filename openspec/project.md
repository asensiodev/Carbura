# Carbura Project Context

Carbura es una app multiplataforma (Android + Desktop) para gestionar el mantenimiento de los vehiculos de una familia: vehiculos, mantenimientos, recordatorios, historial, costes, sincronizacion offline-first y garaje compartido.

El PRD completo vive en `openspec/prd.md` y debe usarse como contexto de producto antes de crear o aplicar cambios OpenSpec.

## Producto

- Slogan: "Tu garaje, siempre a punto."
- Usuarios principales: propietario del vehiculo y familiares que comparten un garaje.
- MVP: vehiculos, mantenimientos, recordatorios locales, historial, costes, sync familiar, Google Auth, invitaciones y exportacion PDF/CSV.
- Fuera del MVP: iOS, combustible, OBD2, push remoto, OCR, recomendaciones IA, multi-familia y roles avanzados.

## Stack

- UI Android: Compose for Android.
- UI Desktop: Compose for Desktop.
- Logica compartida: Kotlin Multiplatform en `commonMain`.
- Base de datos local: SQLDelight.
- HTTP client: Ktor Client.
- Auth cliente Android: Credential Manager con Google ID como opcion principal y fallback controlado a Google Sign-In/OAuth si el dispositivo no lo soporta.
- Auth cliente Desktop: OAuth mediante navegador o flujo equivalente, solo si Desktop entra en el alcance de la entrega.
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
Repository (commonMain)
      ↓
LocalDataSource (SQLDelight) + RemoteDataSource (Ktor + Supabase)
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
