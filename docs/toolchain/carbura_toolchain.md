# Carbura — Toolchain y Procedimiento de Desarrollo

> Documento de referencia del entorno de desarrollo y metodología de trabajo.
> Proyecto: Carbura · TFM AI4Devs · Mayo 2026

---

## Stack de herramientas

| Herramienta | Rol | Para qué se usa en Carbura |
|---|---|---|
| **Android Studio** | IDE principal | Compilar, Gradle, emulador Android, debug KMP, plugin KMP oficial |
| **VS Code** | Editor secundario | Edición de ficheros, navegación del proyecto, extensión OpenCode |
| **Warp** | Terminal inteligente | Correr OpenCode, OpenSpec CLI, Git, comandos de build |
| **OpenCode** | Agente de IA | Generación y modificación de código asistida por IA |
| **OpenSpec** | Framework de specs | Gestión de especificaciones versionadas, metodología SDD |

### Herramientas IA usadas

- **OpenCode**: agente principal para explorar el repositorio, proponer cambios, editar documentación, generar código y ayudar en la verificación.
- **OpenSpec**: flujo asistido por IA para convertir requisitos en proposals, tareas, specs versionadas y archives trazables.
- **ChatGPT/OpenAI vía OpenCode**: soporte conversacional para decisiones de arquitectura, revisión de documentación, planificación y ejecución guiada.

---

## Metodología: SDD sobre TDD + DDD ligero

El proyecto combina metodologías complementarias en capas, manteniendo el alcance acotado al MVP:

```text
┌─────────────────────────────────────────────┐
│              SDD (capa superior)            │
│  Specification-Driven Development           │
│  OpenSpec define QUÉ construir y POR QUÉ   │
│  Las specs son la fuente de verdad          │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│              TDD (capa inferior)            │
│  Test-Driven Development                    │
│  Los tests definen CÓMO se implementa       │
│  Red → Green → Refactor por cada tarea      │
└─────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────┐
│          DDD ligero (diseño de dominio)      │
│  Modelo explícito del garaje familiar        │
│  Entidades, casos de uso y repositorios      │
└─────────────────────────────────────────────┘
```

### SDD (Specification-Driven Development)
- Las specs de OpenSpec definen el **comportamiento esperado** antes de escribir código o tests.
- Cada spec incluye criterios de aceptación que se traducen directamente en tests.
- OpenCode usa las specs como contexto para generar código y tests coherentes.

### TDD (Test-Driven Development)
- Dentro de cada tarea del `/openspec-apply`, se sigue el ciclo **Red → Green → Refactor**:
  1. **Red**: escribir el test que falla (a partir de los criterios de aceptación de la spec).
  2. **Green**: escribir el código mínimo para que el test pase.
  3. **Refactor**: mejorar el código sin romper los tests.
- Los tests son la red de seguridad que garantiza que el código cumple la spec.

### DDD ligero (Domain-Driven Design)

- Se aplicará DDD de forma pragmática para modelar el dominio principal de Carbura: familia, vehículo, mantenimiento, recordatorio y sincronización.
- El dominio se expresará mediante entidades, value objects solo cuando aporten claridad, use cases y repositorios como contratos.
- La UI no debería depender directamente de Supabase, SQLDelight ni detalles de red/local storage.
- No se aplicará DDD táctico pesado si introduce complejidad innecesaria para el MVP.

### Principios de diseño de código: SOLID + CUPID

Durante el paso de refactor se aplicarán principios de diseño pragmáticos, sin sobrediseñar el MVP:

- **SOLID** para mantener responsabilidades claras, dependencias invertidas hacia interfaces y unidades de código fáciles de testear.
- **CUPID** para favorecer código composable, predecible, idiomático, de dominio claro y simple.
- La aplicación de estos principios se validará con tests verdes, revisión humana y consistencia con las specs OpenSpec.
- No se crearán abstracciones preventivas si no hay una necesidad concreta en el MVP.

### Flujo combinado SDD + TDD + DDD ligero

```text
Spec (OpenSpec)
     ↓
Criterios de aceptación
     ↓
Modelo de dominio mínimo (DDD ligero)
     ↓
Tests (TDD Red) ← OpenCode genera propuesta de tests desde la spec
     ↓
Código mínimo (TDD Green) ← OpenCode implementa para pasar los tests
     ↓
Refactor (TDD Refactor) ← OpenCode o dev mejora el código
     ↓
Archive (OpenSpec) ← spec actualizada, cambio cerrado
```

BDD queda fuera del alcance metodológico del MVP para evitar duplicar documentación de comportamiento y mantener el proceso simple.

---

## Cómo encajan las herramientas

```text
┌─────────────────────────────────────────────────────────┐
│                    ANDROID STUDIO                       │
│  Compilar · Gradle · Emulador Android · Debug KMP      │
│  Plugin KMP oficial · Recarga automática de ficheros   │
│  Ejecutar tests (unitarios, instrumentados)            │
└─────────────────────────────────────────────────────────┘
         ↕ mismo directorio del proyecto
┌──────────────────────┐    ┌───────────────────────────┐
│      VS CODE         │    │          WARP             │
│  Editar ficheros     │    │  Terminal inteligente     │
│  Extensión OpenCode  │    │  Integración nativa       │
│  Navegar el proyecto │    │  con OpenCode             │
└──────────────────────┘    └───────────────────────────┘
         ↕                           ↕
┌─────────────────────────────────────────────────────────┐
│                     OPENCODE                            │
│  Agente de IA · Lee y modifica ficheros del repo       │
│  Genera tests a partir de specs · Implementa código    │
│  Trabaja contra las specs de OpenSpec                  │
└─────────────────────────────────────────────────────────┘
         ↕
┌─────────────────────────────────────────────────────────┐
│                     OPENSPEC                            │
│  Specs versionadas · Fuente de verdad del proyecto     │
│  /openspec-proposal · /openspec-apply · /openspec-archive│
└─────────────────────────────────────────────────────────┘
```

---

## Procedimiento de desarrollo

---

### Fase 1 — PRD y especificación inicial

**Herramientas:** VS Code + OpenSpec

1. Inicializar OpenSpec en el repo:
   ```bash
   openspec init
   ```
   Esto crea la estructura:
   ```text
   openspec/
     project.md      ← contexto general del proyecto
     prd.md          ← PRD completo del producto
     specs/          ← specs activas (fuente de verdad)
     changes/        ← propuestas en curso
     archive/        ← historial de cambios completados
     agents.md       ← instrucciones para el agente (no editar a mano)
   ```
2. Escribir el **PRD** en `openspec/prd.md` y mantener `openspec/project.md` como contexto breve del proyecto:
   - Descripción del producto y visión.
   - Stack tecnológico (KMP, Supabase, SQLDelight, Compose).
   - Convenciones de código y arquitectura.
   - Principios de diseño (local-first, simplicidad, privacidad).
   - **Metodología: SDD + TDD** (cada spec debe incluir criterios de aceptación verificables).
3. Escribir las **specs iniciales** en `openspec/specs/`:
   - Una spec por caso de uso (UC-01 a UC-09).
   - Cada spec incluye obligatoriamente una sección **"Criterios de aceptación"** que se usará para generar los tests TDD.
   - Spec del modelo de datos (`data-model.md`).
   - Spec del backend Supabase (`backend.md`).

---

### Fase 2 — Implementación iterativa con SDD + TDD

**Herramientas:** Warp (OpenCode) + VS Code + Android Studio

Para cada feature o caso de uso:

#### Paso 1 — Proposal (planificación SDD)
Desde Warp, lanzar OpenCode y ejecutar:
```text
/openspec-proposal
```
El agente:
- Lee el `project.md`, el `prd.md` y las specs relevantes.
- Hace preguntas si necesita aclarar algo.
- Genera una propuesta de implementación con tareas desglosadas.
- **Incluye los tests TDD a escribir** basados en los criterios de aceptación de la spec.
- Guarda la propuesta en `openspec/changes/`.

#### Paso 2 — Revisión humana
- Revisar la propuesta en VS Code: tareas de implementación **y** tests propuestos.
- Aprobar, rechazar o ajustar.
- El humano siempre decide; el agente siempre propone.

#### Paso 3 — Apply: TDD Red (tests primero)
```text
/openspec-apply
```
El agente implementa **primero los tests**:
- Genera los tests unitarios a partir de los criterios de aceptación de la spec.
- Los tests deben **fallar** en este punto (Red): verificar en Android Studio.
  ```bash
  ./gradlew test
  # Expected: tests failing ✗
  ```

#### Paso 4 — Apply: TDD Green (código mínimo)
El agente implementa el código mínimo necesario para que los tests pasen:
- ViewModels, Use Cases, Repositories, DTOs, etc.
- Compilar y ejecutar tests:
  ```bash
  ./gradlew test
  # Expected: tests passing ✓
  ```

#### Paso 5 — TDD Refactor
- Revisar el código generado con criterio de senior.
- Refactorizar sin romper tests (OpenCode puede ayudar).
- Re-ejecutar tests para confirmar que siguen en verde:
  ```bash
  ./gradlew test
  # Expected: tests still passing ✓
  ```

#### Paso 6 — Compilar y testear en dispositivo
- Android Studio recarga los ficheros modificados automáticamente.
- Compilar y lanzar en emulador:
  ```bash
  ./gradlew assembleDebug
  ```
- Verificar el flujo visualmente en emulador o dispositivo Android. Desktop queda diferido para Entrega 2.

#### Paso 7 — Archive (cierre del cambio SDD)
```text
/openspec-archive
```
El agente:
- Actualiza la spec fuente de verdad en `openspec/specs/`.
- Mueve el cambio completado a `openspec/archive/`.
- Limpia `openspec/changes/`.

#### Paso 8 — Commit local
```bash
git add .
git commit -m "feat(vehicles): implement UC-02 add vehicle flow with tests"
```

Los cambios se agrupan en las Pull Requests oficiales de entrega, no en una PR independiente por cada feature pequeña.

Para la Entrega 1 se usara una rama `dev` como base temporal porque parte de la documentacion inicial ya fue sincronizada en `main`. La PR visible de Entrega 1 sera `feature-entrega1-AAC` -> `dev`. Despues de esa entrega, el flujo vuelve a ser `feature-*` -> `main` y no se trabajara directamente sobre `main`.

Ramas y PRs oficiales:

| Entrega | Rama origen | Rama destino | Contenido |
|---|---|---|---|
| Entrega 1 | `feature-entrega1-AAC` | `dev` | Documentacion tecnica: README, PRD, user stories, arquitectura, modelo de datos, API y tickets. |
| Entrega 2 | `feature-entrega2-AAC` | `dev` | MVP funcional Android-first: frontend, backend/datos, base de datos, sync v0, notificaciones locales y flujo principal. |
| Entrega final | `finalproject-AAC` | `main` | Version final con flujo E2E, tests, despliegue/evidencia y documentacion cerrada. |

---

### Fase 3 — Configuración del backend (Supabase)

**Herramientas:** Warp + VS Code (para la spec) + Dashboard de Supabase

1. Crear proyecto en Supabase.
2. Aplicar las migraciones versionadas en `supabase/migrations/`:
   - `202607010001_initial_schema.sql`
   - `202607070001_ensure_user_profile_rpc.sql`
   - `202607080001_sync_v0_schema.sql`
   - `202607080002_sync_v0_text_entity_ids.sql`
3. Configurar **Row Level Security (RLS)** por `family_id` para cada tabla.
4. Configurar **Google OAuth** en Supabase Auth.
5. Añadir las variables de entorno a `local.properties`:
   ```properties
   SUPABASE_URL=https://xxxx.supabase.co
   SUPABASE_ANON_KEY=xxxx
   GOOGLE_CLIENT_ID=xxxx.apps.googleusercontent.com
   ```
6. Verificar conectividad desde la app Android.
7. Ejecutar los tests de sync (`:core:data:desktopTest`) y dominio.

---

### Fase 4 — Refinado y pulido

**Herramientas:** todas

1. Revisar UX en emulador o dispositivo Android.
2. Revisar cobertura de tests (`./gradlew koverReport`).
3. Añadir tests de integración para flujos críticos (auth, sync, reminders).
4. Pulir animaciones, estados vacíos, estados de error, estados de carga.
5. Revisar el flujo de sincronización (offline → online → sync).

---

### Fase 5 — Documentación y memoria del TFM

**Herramientas:** VS Code + OpenCode

1. Generar **README.md** con:
   - Nombre + slogan ("Tu garaje, siempre a punto").
   - Screenshots o GIFs de la app si se preparan para la entrega final.
   - Stack tecnológico y metodología (SDD + TDD).
   - Sección "AI-assisted development": cómo se usó IA en cada fase.
   - Instrucciones de setup con `local.properties.example`.
2. Documentar el proceso AI + SDD + TDD en la **memoria del TFM**:
   - Usar el historial de `openspec/archive/` como evidencia de SDD.
   - Mostrar trazabilidad: spec → criterios de aceptación → tests (Red) → código (Green) → refactor.
   - Incluir métricas de cobertura de tests como indicador de calidad.
3. Preparar el guion de la **demo final**.

### Evidencias

- `openspec/specs/`: fuente de verdad viva de las capacidades aceptadas.
- `openspec/changes/`: proposals y tareas durante la implementación.
- `openspec/changes/archive/`: historial de cambios aplicados y cerrados.
- Commits y PRs: trazabilidad entre documentación, specs, código y entregas.
- Comandos de verificación: `./gradlew test`, `./gradlew assembleDebug` y revisiones manuales en Android Studio/emulador.

---

## Convenciones de commits

Usar **Conventional Commits** para mantener el historial limpio y legible:

```text
feat(vehicles): implement UC-02 add vehicle flow with tests
test(maintenance): add TDD tests for MaintenanceRecord validation
fix(sync): resolve conflict on simultaneous offline edits
docs(specs): update UC-05 reminder completion flow
refactor(domain): extract VehicleValidator use case
chore(deps): update supabase-kt to 3.x
```

Formato: `tipo(scope): descripción breve`

Tipos: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `style`

---

## Estructura del repositorio

```text
carbura/
├── openspec/
│   ├── project.md              ← contexto breve del proyecto para OpenCode
│   ├── prd.md                  ← PRD completo del producto
│   ├── specs/                  ← fuente de verdad viva
│   │   ├── auth-session/
│   │   ├── login-onboarding/
│   │   ├── vehicle-management/
│   │   ├── maintenance-history/
│   │   ├── reminders-mvp/
│   │   ├── sync-v0/
│   │   └── supabase-backend/
│   ├── changes/                ← propuestas en curso
│   └── archive/                ← historial de cambios completados
├── build-logic/                ← convention plugins Gradle
├── app/
│   ├── android/                ← UI Android, navegación y adapters Android
│   └── shared/                 ← rutas/contratos compartidos de app
├── core/
│   ├── model/                  ← modelos compartidos
│   ├── domain/                 ← entidades, use cases y contratos
│   ├── data/                   ← repositorios
│   ├── auth/                   ← adapter Supabase/Auth Android y settings
│   ├── designsystem/           ← tema, tokens y componentes base
│   ├── string-resources/       ← strings compartidas/type-safe
│   └── testing/                ← fakes y utilidades de test
├── feature/
│   ├── onboarding/
│   ├── garage/
│   ├── maintenance/
│   └── reminders/
├── local.properties.example
├── README.md
└── .gitignore
```

---

## Flujo diario de trabajo (resumen)

```text
1. Abrir Android Studio con el proyecto Carbura
2. Abrir Warp en el directorio del proyecto
3. (Opcional) Abrir VS Code en el mismo directorio
4. En Warp: lanzar `opencode`
5. /openspec-proposal → revisar propuesta + tests TDD → aprobar
6. /openspec-apply (tests primero: Red) → verificar que fallan
7. /openspec-apply (código: Green) → verificar que pasan
8. Refactor → re-ejecutar tests → compilar en Android Studio → testear
9. /openspec-archive → commit local
10. Agrupar cambios en la PR oficial correspondiente a la entrega
```

---

*Documento sujeto a revisión iterativa durante el desarrollo del proyecto.*
