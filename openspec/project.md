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
- Auth cliente: KMPAuth con Google Sign-In.
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
- Las specs son la fuente de verdad funcional.
- Los criterios de aceptacion de historias/specs deben traducirse en tests.
- Commits con Conventional Commits: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`.

## Reglas de seguridad

- No commitear secrets, tokens, claves Supabase ni OAuth client secrets.
- Usar `local.properties` o variables locales para credenciales.
- Mantener Row Level Security por `family_id` en Supabase.
