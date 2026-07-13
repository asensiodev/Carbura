# Carbura

**Tu garaje, siempre a punto.**

## Índice

0. [Ficha del proyecto](#0-ficha-del-proyecto)
1. [Descripción general del producto](#1-descripción-general-del-producto)
2. [Arquitectura del sistema](#2-arquitectura-del-sistema)
3. [Modelo de datos](#3-modelo-de-datos)
4. [Especificación de la API](#4-especificación-de-la-api)
5. [Historias de usuario](#5-historias-de-usuario)
6. [Tickets de trabajo](#6-tickets-de-trabajo)
7. [Pull requests](#7-pull-requests)

---

## 0. Ficha del proyecto

### **0.1. Tu nombre completo:**

Ángel Asensio Cuevasanta

### **0.2. Nombre del proyecto:**

Carbura

### **0.3. Descripción breve del proyecto:**

Carbura es una aplicación Android local-first, construida sobre una arquitectura Kotlin Multiplatform, para gestionar los vehículos de un garaje familiar. Permite registrar vehículos, mantenimientos, costes, kilometraje y recordatorios; persiste la información con SQLDelight y sincroniza vehículos, mantenimientos y recordatorios con Supabase. Android es el entregable funcional actual, mientras que Desktop e iOS se conservan como visión futura multiplataforma.

### **0.4. URL del proyecto:**

https://github.com/asensiodev/Carbura

### **0.5. URL o archivo comprimido del repositorio:**

https://github.com/asensiodev/Carbura

---

## 1. Descripción general del producto

### **1.1. Objetivo:**

El objetivo de Carbura es centralizar el mantenimiento de los vehículos de una familia en una aplicación sencilla y utilizable sin conexión. Resuelve problemas cotidianos como recordar la próxima ITV, la renovación del seguro o una revisión por kilometraje, consultar cuándo se realizó un mantenimiento y conservar su coste, taller y notas.

El valor principal consiste en reducir olvidos y pérdida de información mediante un historial ordenado por vehículo, recordatorios configurables y sugerencias proactivas. El usuario objetivo no es una flota profesional, sino una persona o familia que necesita control sin complejidad operativa.

### **1.2. Características y funcionalidades principales:**

Funcionalidades disponibles en el entregable Android:

- Autenticación con Google ID mediante Credential Manager y sesión de Supabase Auth.
- Creación o recuperación de una familia personal y su perfil con la RPC `ensure_user_profile`.
- Alta, consulta, edición y borrado lógico de vehículos.
- Actualización rápida del odómetro, con confirmación cuando disminuye.
- Registro, consulta y borrado lógico de mantenimientos, incluidos coste opcional, taller y notas.
- Creación manual, finalización y borrado de recordatorios por fecha, kilometraje o ambos.
- Sugerencias proactivas de recordatorios al crear o editar un vehículo con próxima ITV, renovación del seguro o próxima revisión por kilometraje. El usuario confirma su creación y la reconciliación utiliza identificadores estables para evitar duplicados.
- Notificaciones locales Android para recordatorios con fecha.
- Persistencia local con SQLDelight y sincronización v0 con Supabase para vehículos, mantenimientos y recordatorios.

Trabajo pendiente o evolución dentro del alcance descrito por las historias:

- Integrar el recordatorio desde el formulario de mantenimiento. El dominio ya contiene `CreateAutomaticReminderUseCase` y `MaintenanceRecord.nextDueDate`, pero el formulario y su ViewModel todavía no capturan ni conectan ese vencimiento.
- Calcular y presentar el coste acumulado por vehículo.
- Completar el test E2E Android, la publicación de la entrega y sus evidencias.
- Incorporar invitaciones familiares y exportación PDF/CSV en evoluciones posteriores.
- Mantener Desktop e iOS como visión futura sobre la base compartida KMP. Android es el único cliente funcional y el entregable actual.
- Evaluar un fallback OAuth mediante navegador como evolución futura. Actualmente Android solo usa Credential Manager + Google ID y permite reintentar ese mismo flujo ante un error.

La priorización se documenta en [`openspec/prd.md`](openspec/prd.md) y [`docs/user-stories.md`](docs/user-stories.md).

### **1.3. Diseño y experiencia de usuario:**

La experiencia se organiza alrededor de estas áreas:

- **Garaje:** listado de vehículos, alta, edición, borrado y actualización rápida del odómetro.
- **Detalle de vehículo:** acceso al historial y registro de mantenimientos.
- **Recordatorios:** creación manual, consulta de próximos avisos, finalización y borrado.
- **Usuario:** sesión, estado de sincronización y acción de sincronización manual.

El flujo principal disponible es:

```text
Inicio de sesión con Google ID
  -> Crear o recuperar familia personal
  -> Añadir o editar vehículo
  -> Confirmar recordatorios proactivos del vehículo, si procede
  -> Registrar mantenimiento
  -> Consultar historial
  -> Crear o consultar recordatorios
  -> Sincronizar con Supabase
```

La interfaz está implementada con Compose para Android. La arquitectura comparte modelo, dominio, datos y parte de la presentación para facilitar una evolución futura a Desktop e iOS, sin presentar esas plataformas como parte del entregable funcional actual.

### **1.4. Instrucciones de instalación:**

Requisitos:

- JDK 17.
- Android Studio y un SDK Android compatible.
- Proyecto Supabase configurado según [`docs/supabase-setup.md`](docs/supabase-setup.md).
- Cliente OAuth web y cliente OAuth Android configurados según [`docs/supabase-login-runtime.md`](docs/supabase-login-runtime.md).

Crear `local.properties` a partir de [`local.properties.example`](local.properties.example) y completar:

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=xxxx
GOOGLE_CLIENT_ID=xxxx.apps.googleusercontent.com
```

`GOOGLE_CLIENT_ID` corresponde al cliente OAuth de tipo Web application. El cliente Android se registra en Google Cloud con el paquete `com.asensiodev.carbura` y las huellas SHA de firma, pero su identificador no se copia en `local.properties`.

Verificación local equivalente a CI:

```bash
./gradlew qualityCheck test assembleDebug --stacktrace
```

La APK debug se genera desde `app:android`. `local.properties` y cualquier secreto deben permanecer fuera de Git.

---

## 2. Arquitectura del Sistema

### **2.1. Diagrama de arquitectura:**

```mermaid
flowchart TD
    User[Usuario]

    subgraph Clients[Clientes]
        Android[Android actual\nCompose]
        Desktop[Desktop futuro]
        IOS[iOS futuro]
    end

    subgraph Shared[Kotlin Multiplatform]
        Presentation[ViewModels + UiState]
        Domain[Casos de uso + modelos]
        Repositories[Repositorios local-first]
        Sync[LocalFirstSyncManager\nfull pull + last-write-wins]
        Local[(SQLDelight)]
        Remote[Supabase Kotlin\nPostgREST]
    end

    subgraph Backend[Supabase]
        Auth[Supabase Auth]
        DB[(PostgreSQL\nRLS por familia)]
    end

    User --> Android
    User -. visión futura .-> Desktop
    User -. visión futura .-> IOS
    Android --> Presentation
    Desktop -. reutilización futura .-> Presentation
    IOS -. reutilización futura .-> Presentation
    Presentation --> Domain
    Domain --> Repositories
    Repositories --> Local
    Repositories --> Sync
    Sync --> Local
    Sync --> Remote
    Android --> Auth
    Remote --> DB
```

La arquitectura sigue Clean Architecture, modularización Gradle y una estrategia local-first. La UI observa SQLDelight y las mutaciones se guardan primero en local. Cuando existe una sesión válida, `LocalFirstSyncManager` hace converger los cambios con Supabase.

Las dependencias nativas se aíslan mediante contratos cuando existe una implementación real. Android integra Credential Manager, Google ID, ciclo de vida y notificaciones locales. Desktop e iOS permanecen como dirección futura: no existe actualmente una aplicación Desktop ejecutable, target iOS ni fallback OAuth por navegador conectado.

Beneficios principales:

- Dominio, modelos, repositorios y parte de la presentación reutilizables.
- Uso de la aplicación y mutaciones sin conexión.
- Aislamiento entre familias mediante sesión y Row Level Security.
- Tests unitarios sobre dominio, repositorios y sincronización.
- Posibilidad de evolucionar a Desktop e iOS sin declarar soporte funcional prematuro.

Sacrificios o riesgos:

- La sincronización local-first añade complejidad y lecturas remotas completas.
- `last-write-wins` puede ocultar cambios concurrentes.
- No hay sincronización con la aplicación cerrada, Realtime, `WorkManager` ni `Service`.
- Las capacidades nativas requerirán adaptadores específicos para cada plataforma futura.

### **2.2. Descripción de componentes principales:**

| Componente | Tecnología | Responsabilidad |
|---|---|---|
| Android App | Compose para Android | UI funcional, navegación, ciclo de vida, autenticación y notificaciones locales. |
| Clientes futuros | Desktop e iOS | Visión multiplataforma; no forman parte del entregable funcional actual. |
| ViewModels + UiState | Kotlin Multiplatform | Estado de pantalla, eventos y coordinación con casos de uso. |
| Use Cases | Kotlin común | Reglas de negocio para vehículos, mantenimientos, recordatorios y sesión. |
| Domain Models | Kotlin común | Entidades e identificadores independientes de infraestructura. |
| Repositories | Kotlin común | Coordinación de persistencia local y cambios pendientes. |
| LocalDataSource | SQLDelight | Fuente inmediata de la UI y persistencia local-first. |
| RemoteDataSource | Supabase Kotlin/PostgREST | Lectura y upsert de datos remotos protegidos por RLS. |
| SyncManager | Kotlin común | Full pull, push de pendientes, tombstones y `last-write-wins`. |
| Supabase | Auth y PostgreSQL | Sesión, RPC de perfil/familia, datos remotos y RLS. |

### **2.3. Descripción de alto nivel del proyecto y estructura de ficheros**

```text
Carbura/
├── .github/workflows/ci.yml   # Pipeline real de verificación
├── build-logic/               # Convention plugins Gradle
├── app/
│   ├── android/               # Cliente funcional Android
│   └── shared/                # Rutas y contratos compartidos
├── core/
│   ├── model/                 # Modelos e identificadores
│   ├── domain/                # Casos de uso y contratos
│   ├── data/                  # SQLDelight, repositorios, DTO y sync
│   ├── auth/                  # Supabase Auth y adaptadores
│   ├── designsystem/          # Tema y componentes Android
│   ├── string-resources/      # Claves y resolución de textos
│   └── testing/               # Utilidades de test
├── feature/                   # Onboarding, garaje, mantenimiento y recordatorios
├── quality/architecture/      # Reglas de dependencias modulares
├── supabase/migrations/       # Cinco migraciones SQL vigentes
├── docs/                      # Documentación técnica y funcional
├── openspec/
│   ├── specs/                 # Especificaciones vigentes
│   └── changes/
│       └── archive/           # Cambios OpenSpec cerrados
└── readme.md                  # Plantilla académica principal
```

### **2.4. Infraestructura y despliegue**

```mermaid
flowchart LR
    Android[Android actual] <--> LocalDB[(SQLDelight)]
    Android --> GoogleID[Google ID]
    GoogleID --> SupabaseAuth[Supabase Auth]
    Android <--> SupabaseDB[(Supabase PostgreSQL\nRLS)]
    Desktop[Desktop futuro] -. arquitectura compartida .-> SupabaseDB
    IOS[iOS futuro] -. arquitectura compartida .-> SupabaseDB
```

No existe servidor propio. Supabase proporciona Auth, PostgreSQL, PostgREST y RLS. Android es el artefacto instalable actual; Desktop e iOS se mantienen como evolución futura multiplataforma.

**CI/CD y evidencia de despliegue (ticket T-11):**

- `.github/workflows/ci.yml` se ejecuta en `push` y `pull_request` sobre Ubuntu con JDK 17.
- El job real ejecuta `./gradlew qualityCheck test assembleDebug --stacktrace`.
- `qualityCheck` agrega ktlint, detekt y `:quality:architecture:test`.
- La publicación de una release instalable y las capturas o vídeo de evidencia siguen pendientes para el cierre final.
- Las credenciales permanecen fuera del repositorio; CI no necesita secretos de producción para las comprobaciones actuales.

### **2.5. Seguridad**

- Inicio de sesión con Google ID y Supabase Auth.
- RPC `ensure_user_profile` ejecutable solo por el rol `authenticated` para crear o recuperar la familia personal.
- RLS habilitado en las tablas públicas y políticas basadas en `can_access_family`.
- `family_id` limita las operaciones remotas al garaje accesible por el JWT.
- Variables sensibles excluidas del repositorio mediante `local.properties` y `.gitignore`.
- `invite_code` existe como campo opcional, pero todavía no constituye un flujo ni una API de invitación.

### **2.6. Tests**

La estrategia combina SDD con OpenSpec, TDD durante la implementación y DDD ligero:

```text
Spec OpenSpec
  -> criterios de aceptación
  -> tests que fallan
  -> código mínimo
  -> refactor
  -> verificación y archivo del cambio
```

La suite actual incluye tests comunes y JVM/desktop de dominio, repositorios y sincronización, además de reglas de arquitectura, ktlint y detekt. El pipeline ejecuta `qualityCheck`, `test` y `assembleDebug` en cada push y pull request.

Como cierre de calidad permanece pendiente el test E2E Android del recorrido principal. Debe evitar depender del selector real de Google en CI y cubrir una sesión de prueba, alta o edición de vehículo, mantenimiento, historial y recordatorio.

### **2.7. Diseño de dominio y principios de código**

Carbura aplica DDD ligero para conservar un modelo claro sin sobrediseñar el producto:

- Entidades principales: `Family`, `UserProfile`, `Vehicle`, `MaintenanceType`, `MaintenanceRecord` y `Reminder`.
- Casos de uso explícitos para vehículos, mantenimientos, recordatorios, autenticación y sincronización.
- Repositorios como frontera entre dominio, SQLDelight y Supabase.
- Identificadores de texto estables generados por el cliente para las entidades sincronizables.

SOLID y CUPID se aplican como criterios pragmáticos: responsabilidades acotadas, dependencias hacia contratos, comportamiento predecible, Kotlin idiomático y lenguaje de dominio claro. No se añaden capas si no aportan claridad, desacoplamiento o capacidad de prueba.

---

## 3. Modelo de Datos

### **3.1. Diagrama del modelo de datos:**

El esquema remoto vigente resulta de aplicar, en orden, las cinco migraciones de `supabase/migrations/`. La última añade los campos de planificación del vehículo. Vehículos, mantenimientos y recordatorios usan IDs de texto estables; familias, perfiles y tipos de mantenimiento mantienen UUID.

```mermaid
erDiagram
    FAMILY ||--o{ USER_PROFILE : agrupa
    FAMILY ||--o{ VEHICLE : posee
    FAMILY ||--o{ MAINTENANCE_TYPE : define
    VEHICLE ||--o{ MAINTENANCE_RECORD : registra
    VEHICLE ||--o{ REMINDER : programa
    MAINTENANCE_TYPE ||--o{ MAINTENANCE_RECORD : clasifica
    MAINTENANCE_TYPE ||--o{ REMINDER : clasifica

    FAMILY {
        uuid id PK
        string name
        string invite_code UK
        uuid created_by FK
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    USER_PROFILE {
        uuid id PK
        uuid user_id UK
        uuid family_id FK
        string display_name
        string email
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    VEHICLE {
        string id PK
        uuid family_id FK
        string name
        string vehicle_type
        string brand
        string model
        string license_plate
        integer current_odometer_km
        date next_itv_date
        date insurance_renewal_date
        integer next_service_odometer_km
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    MAINTENANCE_TYPE {
        uuid id PK
        uuid family_id FK
        string code
        string name
        boolean is_global
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    MAINTENANCE_RECORD {
        string id PK
        uuid family_id FK
        string vehicle_id FK
        uuid maintenance_type_id FK
        string maintenance_type_key
        string maintenance_type_code
        date performed_on
        integer odometer_km
        integer cost_cents
        string currency
        string workshop
        string notes
        date next_due_date
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    REMINDER {
        string id PK
        uuid family_id FK
        string vehicle_id FK
        uuid maintenance_type_id FK
        string maintenance_type_key
        string title
        date due_date
        integer due_odometer_km
        integer notify_days_before
        datetime completed_at
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }
```

### **3.2. Descripción de entidades principales:**

| Entidad | Descripción | Campos principales | Restricciones |
|---|---|---|---|
| `Family` | Garaje familiar. | UUID, nombre, `invite_code`, `created_by`, timestamps. | `invite_code` opcional y único; creador autenticado. |
| `UserProfile` | Perfil vinculado a Supabase Auth. | UUID, `user_id`, `family_id`, nombre y correo. | `user_id` único y `family_id` obligatorio. |
| `Vehicle` | Vehículo del garaje. | ID texto, familia, nombre, tipo, marca, modelo, matrícula, odómetro y objetivos de ITV, seguro y revisión. | Nombre y tipo obligatorios; kilómetros no negativos. |
| `MaintenanceType` | Catálogo global o específico de familia. | UUID, familia opcional, código, nombre e `is_global`. | Global sin familia o personalizado con familia. |
| `MaintenanceRecord` | Evento del historial. | ID texto, familia, vehículo, tipo/key/code, fecha, odómetro, coste en céntimos, moneda, taller, notas y `next_due_date`. | Vehículo y familia relacionados; tipo remoto opcional desde sync v0. |
| `Reminder` | Aviso por fecha o kilometraje. | ID texto, familia, vehículo, título, tipo/key, vencimientos, antelación y `completed_at`. | Debe tener fecha, kilometraje o ambos. |

Las migraciones vigentes son:

1. `202607010001_initial_schema.sql`: tablas, índices, triggers, funciones auxiliares y RLS.
2. `202607070001_ensure_user_profile_rpc.sql`: grants y RPC de creación o recuperación de perfil/familia.
3. `202607080001_sync_v0_schema.sql`: tipo de mantenimiento opcional, claves de tipo y `next_due_date`.
4. `202607080002_sync_v0_text_entity_ids.sql`: IDs de texto para vehículos, mantenimientos y recordatorios.
5. `202607120001_vehicle_planning_fields.sql`: próxima ITV, renovación del seguro y próxima revisión por kilometraje.

SQLDelight mantiene `updatedAt`, `pendingSync` y `deletedAt` en las tres familias sincronizables. `deleted_at` representa tombstones y `updated_at` resuelve conflictos mediante `last-write-wins`.

---

## 4. Especificación de la API

Carbura no mantiene un backend REST propio ni un contrato agregado de sincronización. El cliente usa Supabase Auth, una RPC PostgREST y operaciones `select`/`upsert` de Supabase Kotlin. Se documentan tres endpoints representativos, máximo exigido por la plantilla; Supabase genera la especificación OpenAPI completa del esquema desplegado.

### **4.1. Autenticación y perfil familiar**

```yaml
operaciones:
  - nombre: iniciarSesionConGoogleId
    endpoint: POST /auth/v1/token?grant_type=id_token
    entrada:
      provider: google
      id_token: string
    salida: sesión Supabase
  - nombre: asegurarPerfilYFamilia
    endpoint: POST /rest/v1/rpc/ensure_user_profile
    entrada:
      profile_display_name: string
      profile_email: string | null
    salida:
      user_id: uuid
      family_id: uuid
      display_name: string
      email: string | null
seguridad: JWT de usuario y rol authenticated
```

No existe actualmente fallback OAuth mediante navegador conectado. Puede mantenerse como evolución futura para plataformas o dispositivos que lo requieran.

### **4.2. Sincronización de garaje**

La sincronización no utiliza un cursor incremental ni un endpoint agregado. `LocalFirstSyncManager` serializa ciclos con un `Mutex` y, para `vehicles`, `maintenance_records` y `reminders`, realiza:

1. Resolución de sesión, perfil y familia activa.
2. Adopción de filas locales heredadas de `local-family`.
3. Descarga completa por `family_id` de cada familia de entidades.
4. Comparación con filas locales `pendingSync`; solo se suben por upsert las que no tienen una versión remota más reciente.
5. Marcado local de las filas subidas como sincronizadas.
6. Nueva descarga completa y fusión local mediante `last-write-wins` por `updated_at`.

Los borrados convergen como tombstones mediante `deleted_at`. El ciclo se activa al iniciar la sesión, al volver la app a primer plano con limitación temporal, mediante temporizador mientras la composición autenticada está activa, después de mutaciones y por acción manual. No se ejecuta con la aplicación cerrada.

### **4.3. Invitación a garaje familiar**

La invitación familiar no dispone de endpoint, RPC, caso de uso ni interfaz. El campo opcional `families.invite_code` forma parte del esquema inicial, pero no representa por sí mismo un contrato funcional. Su diseño se mantiene fuera del entregable Android actual y deberá especificar membresía, caducidad, permisos y aceptación antes de implementarse.

### **4.4. Especificación OpenAPI de los endpoints Supabase**

La siguiente especificación académica limita la muestra a tres endpoints reales. Las operaciones equivalentes sobre `maintenance_records` y `reminders` se realizan con el SDK de Supabase siguiendo el esquema y las políticas RLS desplegadas, sin introducir un contrato ficticio de sincronización.

```yaml
openapi: 3.0.3
info:
  title: Carbura - API remota Supabase
  version: 1.0.0
servers:
  - url: https://{project_ref}.supabase.co
paths:
  /auth/v1/token:
    post:
      summary: Intercambiar Google ID token por una sesión Supabase
      parameters:
        - name: grant_type
          in: query
          required: true
          schema: { type: string, enum: [id_token] }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [provider, id_token]
              properties:
                provider: { type: string, enum: [google] }
                id_token: { type: string }
      responses:
        "200": { description: Sesión creada }
        "400": { description: Token inválido o caducado }
  /rest/v1/rpc/ensure_user_profile:
    post:
      summary: Crear o recuperar el perfil y la familia personal
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [profile_display_name]
              properties:
                profile_display_name: { type: string }
                profile_email: { type: string, nullable: true }
      responses:
        "200": { description: Perfil y familia resueltos }
        "401": { description: Sesión ausente o inválida }
  /rest/v1/vehicles:
    get:
      summary: Descargar los vehículos accesibles de una familia
      security: [{ bearerAuth: [] }]
      parameters:
        - name: family_id
          in: query
          required: true
          description: Filtro PostgREST con formato eq.UUID
          schema: { type: string }
        - name: select
          in: query
          schema: { type: string, default: "*" }
      responses:
        "200":
          description: Conjunto remoto completo filtrado por familia
          content:
            application/json:
              schema:
                type: array
                items: { $ref: "#/components/schemas/Vehicle" }
    post:
      summary: Subir vehículos pendientes mediante upsert
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: array
              items: { $ref: "#/components/schemas/Vehicle" }
      responses:
        "201": { description: Filas creadas o actualizadas }
        "403": { description: RLS rechaza el acceso a la familia }
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
  schemas:
    Vehicle:
      type: object
      required: [id, family_id, name, vehicle_type, current_odometer_km]
      properties:
        id: { type: string }
        family_id: { type: string, format: uuid }
        name: { type: string }
        vehicle_type: { type: string }
        brand: { type: string, nullable: true }
        model: { type: string, nullable: true }
        license_plate: { type: string, nullable: true }
        current_odometer_km: { type: integer, minimum: 0 }
        next_itv_date: { type: string, format: date, nullable: true }
        insurance_renewal_date: { type: string, format: date, nullable: true }
        next_service_odometer_km: { type: integer, minimum: 0, nullable: true }
        created_at: { type: string, format: date-time }
        updated_at: { type: string, format: date-time }
        deleted_at: { type: string, format: date-time, nullable: true }
```

Las especificaciones vigentes de backend y sesión están en [`openspec/specs/supabase-backend/spec.md`](openspec/specs/supabase-backend/spec.md) y [`openspec/specs/auth-session/spec.md`](openspec/specs/auth-session/spec.md). La especificación archivada del endurecimiento de sync v0 está en [`openspec/changes/archive/2026-07-09-harden-sync-offline/specs/sync-v0/spec.md`](openspec/changes/archive/2026-07-09-harden-sync-offline/specs/sync-v0/spec.md); el resto de cambios históricos sigue el patrón `openspec/changes/archive/<fecha>-<change-id>/`.

---

## 5. Historias de Usuario

Las historias completas y sus criterios de aceptación están en [`docs/user-stories.md`](docs/user-stories.md). Todas las historias completadas se refieren al cliente Android actual; Desktop e iOS representan la visión futura multiplataforma.

Historias principales de la entrega:

- **US-01 - Iniciar sesión y disponer de un garaje personal:** completada en Android con Google ID, Supabase Auth y `ensure_user_profile`; sin fallback OAuth conectado actualmente.
- **US-02 - Gestionar vehículos:** completada para alta, consulta, edición, borrado lógico, objetivos de planificación y odómetro rápido.
- **US-04 - Registrar mantenimiento o avería:** completada para alta, listado y borrado, incluidos kilometraje, coste opcional, taller y notas.
- **US-07 - Gestionar recordatorios:** completada para creación manual, listado, finalización y borrado.
- **US-08 - Recibir una notificación local:** completada en Android para recordatorios con fecha.
- **US-10 - Sincronizar entre sesiones o dispositivos:** completada con los límites de sync v0, full pull y ejecución solo dentro del proceso de la app.
- **US-13 - Obtener sugerencias proactivas desde el vehículo:** completada. Crear o editar un vehículo puede sugerir ITV, seguro y revisión por kilometraje; la confirmación reconcilia IDs estables sin duplicados.
- **US-06 - Generar un recordatorio desde un mantenimiento:** pendiente de integración. El caso de uso y `nextDueDate` existen, pero el formulario y el ViewModel de mantenimiento todavía no los conectan. Esta historia es distinta de US-13.

---

## 6. Tickets de Trabajo

El backlog completo está en [`docs/backlog.md`](docs/backlog.md). Los tickets reflejan tanto el trabajo cerrado como las tareas pendientes de integración y calidad, sin confundir piezas de dominio aisladas con flujos de usuario completos.

### **6.1. Backlog inicial derivado de user stories**

| Ticket | Área | Historia relacionada | Prioridad | Estado / resultado |
|---|---|---|---|---|
| T-01 | Datos | US-01, US-02, US-04, US-06 | Cerrado | SQLDelight, Supabase, RLS y RPC de perfil implementados. |
| T-02 | Auth / onboarding | US-01 | Cerrado | Google ID y Supabase Auth implementados; fallback OAuth no conectado. |
| T-03 | Vehículos | US-02 | Cerrado | Alta, listado y borrado local-first implementados. |
| T-04 | Mantenimiento | US-04, US-05 | Cerrado parcial | Historial y costes individuales disponibles; total pendiente. |
| T-05 | Recordatorios | US-06 | Alta | Caso de uso aislado; integración desde mantenimiento pendiente. |
| T-06 | Sincronización | US-02, US-04, US-07 | Cerrado | Sync v0 con full pull, pendientes, tombstones y LWW. |
| T-07 | Presentación | US-02 | Cerrado | Formulario Android de vehículo implementado. |
| T-08 | Presentación | US-04, US-05 | Cerrado | Formulario de mantenimiento e historial implementados. |
| T-09 | Recordatorios | US-07 | Cerrado | Lista y gestión manual implementadas. |
| T-10 | Plataforma | US-08 | Cerrado Android | Alarmas y notificaciones locales para fechas. |
| T-11 | CI/CD | Transversal | Alta | CI implementada; release y evidencias pendientes. |
| T-12 | Calidad | Flujo principal | Alta | E2E Android pendiente. |
| T-13 | Vehículos | US-02, US-09 | Cerrado | Edición y actualización rápida del odómetro. |
| T-14 | Recordatorios | US-13 | Cerrado | Sugerencias proactivas desde vehículo implementadas. |
| T-15 | Costes | US-14 | Alta | Coste acumulado pendiente. |
| T-18 | Multiplataforma | Visión futura | Diferido | Desktop e iOS no son entregables funcionales actuales. |

### **6.2. Tickets principales detallados para la entrega**

#### **Ticket 1 - Frontend: alta de vehículo en el garaje**

**Tipo:** frontend / presentación compartida con UI Android

**Historia relacionada:** US-02 - Gestionar vehículos

**Objetivo:** permitir que el usuario autenticado cree y edite vehículos, actualice el odómetro y confirme sugerencias proactivas.

**Resultado:** implementado en Android con validaciones, estados de carga/error, borrado lógico y persistencia local-first. Los campos opcionales `next_itv_date`, `insurance_renewal_date` y `next_service_odometer_km` generan sugerencias confirmables y reconciliadas mediante IDs estables.

**Criterios de aceptación verificados:**

- Los datos válidos crean o actualizan el vehículo y este aparece en el garaje.
- Los campos obligatorios y kilómetros no negativos se validan.
- Un descenso de odómetro exige confirmación.
- Rechazar una sugerencia guarda el vehículo sin crear esos avisos; aceptarla evita duplicados.

#### **Ticket 2 - Backend/datos: registro de mantenimiento e historial**

**Tipo:** datos / dominio / repositorio / presentación Android

**Historia relacionada:** US-04 - Registrar mantenimiento o avería

**Objetivo:** registrar mantenimientos local-first y consultar el historial del vehículo.

**Resultado:** implementado para fecha, kilometraje, coste opcional en céntimos, moneda, taller, notas, listado y borrado lógico. Los registros se marcan como pendientes y participan en sync v0.

**Pendiente asociado:** el formulario no captura todavía `nextDueDate` ni invoca `CreateAutomaticReminderUseCase`. Por ello, registrar una ITV o un seguro desde mantenimiento no crea aún su recordatorio asociado. La integración requiere ID estable o política de duplicados, notificación y tests de extremo a extremo.

**Criterios de aceptación verificados:**

- Un mantenimiento válido queda persistido y visible en el historial.
- El coste individual se conserva y se muestra.
- Sin red, el registro permanece local y pendiente de sincronización.

#### **Ticket 3 - Base de datos: esquema local y remoto del MVP**

**Tipo:** base de datos / infraestructura

**Historias relacionadas:** US-01, US-02, US-04, US-06, US-07 y US-13

**Objetivo:** soportar familias, perfiles, vehículos, catálogo de mantenimiento, historial, recordatorios y sincronización local-first.

**Resultado:** implementado mediante cinco migraciones Supabase, esquemas SQLDelight, índices, constraints, triggers, grants, RPC de perfil/familia y políticas RLS. Las entidades sincronizables utilizan IDs de texto estables y campos `updated_at`/`deleted_at`; el cliente mantiene además `pendingSync`.

**Criterios de aceptación verificados:**

- Vehículos, mantenimientos y recordatorios conservan la relación con su familia y vehículo.
- RLS restringe las operaciones a familias accesibles por el usuario autenticado.
- Los tombstones sincronizan borrados lógicos.
- Los campos de planificación del vehículo soportan recordatorios proactivos ya disponibles.

---

## 7. Pull Requests

Esta sección conserva las tres Pull Requests exigidas por la plantilla y las alinea con las entregas académicas del proyecto.

Para la Entrega 1 se utiliza una rama `dev` como base de comparación porque la documentación inicial ya se había sincronizado con `main`. La Entrega 2 mantiene la PR académica desde `feature-entrega2-AAC` hacia `dev`; la entrega final se integra desde `finalproject-AAC` hacia `main`.

PRs previstas:

- **PR 1 - Entrega 1 / Documentación técnica:** `feature-entrega1-AAC` hacia `dev`, con PRD, historias, arquitectura, modelo, API y tickets iniciales.
- **PR 2 - Entrega 2 / MVP funcional:** `feature-entrega2-AAC` hacia `dev`, con autenticación, datos, UI Android, sync v0, recordatorios y notificaciones locales.
- **PR 3 - Entrega final:** `finalproject-AAC` hacia `main`, con cierre del flujo Android, integración pendiente priorizada, E2E, release/evidencias y documentación académica final.
