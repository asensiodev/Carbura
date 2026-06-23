# Carbura - Lecturas Previas Entrega 2

Este documento indica que leer antes de empezar la implementacion de Entrega 2. El objetivo no es estudiar todo, sino llegar con contexto suficiente para crear el proyecto KMP, usar OpenCode/OpenSpec con criterio y configurar Supabase sin improvisar.

Tiempo recomendado antes de empezar: **90-120 minutos**.

## 1. Lectura obligatoria del propio proyecto

Antes de leer documentacion externa, repasa estos documentos del repo:

| Documento | Tiempo | Para que sirve |
|---|---:|---|
| `docs/entrega2-plan.md` | 15 min | Entender objetivo, orden diario y alcance Android-first. |
| `docs/backlog.md` | 15 min | Ver tickets T-01 a T-12 y orden tecnico. |
| `docs/toolchain/carbura_toolchain.md` | 10 min | Recordar flujo OpenCode + OpenSpec + TDD. |
| `openspec/prd.md` | 10 min | Recordar producto, MVP y prioridades. |
| `readme.md` secciones 1-6 | 15 min | Arquitectura, modelo, API, historias y tickets resumidos. |

Resultado esperado: saber cual es el flujo principal y que no debemos construir todavia.

## 2. OpenCode

Documentacion oficial:

- Intro: <https://opencode.ai/docs/>
- TUI: <https://opencode.ai/docs/tui/>
- CLI: <https://opencode.ai/docs/cli/>
- Commands: <https://opencode.ai/docs/commands/>
- Agents: <https://opencode.ai/docs/agents/>
- Permissions: <https://opencode.ai/docs/permissions/>

Lectura minima:

- Como iniciar OpenCode en un proyecto.
- Diferencia entre pedir explicaciones, planificar y pedir cambios.
- Como usar comandos.
- Como revisar cambios antes de aceptarlos.

No hace falta leer ahora:

- SDK.
- Plugins.
- MCP servers.
- Configuracion avanzada de temas/modelos.

Uso previsto en Carbura:

```text
OpenCode no decide el producto.
OpenCode ejecuta cambios siguiendo OpenSpec, backlog y revision humana.
```

## 3. OpenSpec en este repo

OpenSpec es nuestro flujo SDD. La documentacion que mas importa ahora esta dentro del repositorio:

- `docs/toolchain/carbura_toolchain.md`
- `openspec/project.md`
- `openspec/prd.md`
- `.opencode/commands/opsx-propose.md`
- `.opencode/commands/opsx-apply.md`
- `.opencode/commands/opsx-archive.md`

Lectura minima:

- Entender `proposal -> apply -> archive`.
- Entender que cada feature grande debe partir de un cambio OpenSpec.
- Entender que `tasks.md` sera nuestro equivalente a subtareas tecnicas.

Flujo que usaremos:

```text
Ticket en docs/backlog.md
  -> OpenSpec proposal
  -> revision humana
  -> OpenSpec apply
  -> tests TDD
  -> codigo
  -> archive
  -> commit
```

Primer cambio que haremos:

```text
bootstrap-kmp-project
```

## 4. Kotlin Multiplatform

Documentacion oficial:

- Get started with Kotlin Multiplatform: <https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html>
- Create your Kotlin Multiplatform app: <https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-create-first-app.html>
- Kotlin Multiplatform project structure: <https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-discover-project.html>
- Android Studio KMP plugin: <https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform>

Lectura minima:

- Como crear un proyecto KMP desde Android Studio o template oficial.
- Que es `commonMain` y `commonTest`.
- Que codigo vive en `shared` y que codigo vive en `androidApp`.
- Como compilar Android desde Gradle.

No hace falta leer ahora:

- iOS.
- Publicacion de librerias.
- KMP avanzado para Native.

Decision Carbura:

```text
shared = dominio, use cases, repositorios, modelos, tests.
androidApp = UI Android, permisos, navegacion y codigo especifico de Android.
desktopApp = opcional si Android queda estable.
```

## 5. Compose Android y Compose Multiplatform

Documentacion oficial:

- Jetpack Compose overview: <https://developer.android.com/jetpack/compose>
- Compose layouts: <https://developer.android.com/develop/ui/compose/layouts/basics>
- State in Compose: <https://developer.android.com/develop/ui/compose/state>
- Compose Multiplatform: <https://www.jetbrains.com/lp/compose-multiplatform/>

Lectura minima:

- Composables basicos.
- Estado simple en Compose.
- Formularios y validaciones simples.
- Listas con `LazyColumn`.

No hace falta leer ahora:

- Animaciones avanzadas.
- Theming avanzado.
- Desktop Compose profundo.

Decision Carbura:

```text
UI simple y funcional primero.
Estados vacios, loading y error antes que pulido visual.
```

## 6. Supabase

Documentacion oficial:

- Getting started: <https://supabase.com/docs/guides/getting-started>
- Android Kotlin quickstart: <https://supabase.com/docs/guides/getting-started/quickstarts/kotlin>
- API keys: <https://supabase.com/docs/guides/getting-started/api-keys>
- Auth overview: <https://supabase.com/docs/guides/auth>
- Google Auth: <https://supabase.com/docs/guides/auth/social-login/auth-google>
- Android Credential Manager: <https://developer.android.com/identity/sign-in/credential-manager>
- Sign in with Google for Android: <https://developer.android.com/identity/sign-in/credential-manager-siwg>
- Row Level Security: <https://supabase.com/docs/guides/database/postgres/row-level-security>

Lectura minima:

- Como crear proyecto Supabase.
- Donde obtener `SUPABASE_URL` y `SUPABASE_ANON_KEY`.
- Diferencia entre anon key y service role key.
- Como activar RLS.
- Como configurar Google OAuth a alto nivel.
- Como funciona Credential Manager como flujo principal de Android.
- Que fallback conviene dejar previsto si Credential Manager no esta disponible o no devuelve credenciales.

No hace falta leer ahora:

- Edge Functions.
- Realtime.
- Storage avanzado.
- Vector/AI.

Decision Carbura:

```text
Supabase gestiona Auth y PostgreSQL.
Android usa Credential Manager + Google ID como login principal.
Google Sign-In/OAuth queda como fallback, no como primera opcion.
No habra backend propio en Entrega 2.
Nunca commitear service role key ni secretos reales.
```

## 7. SQLDelight

Documentacion oficial:

- SQLDelight: <https://cashapp.github.io/sqldelight/>
- Multiplatform SQLite: <https://cashapp.github.io/sqldelight/2.0.2/multiplatform_sqlite/>
- Gradle setup: <https://cashapp.github.io/sqldelight/2.0.2/gradle/>

Lectura minima:

- Como se define un `.sq`.
- Como SQLDelight genera APIs Kotlin.
- Como encaja con KMP.

No hace falta leer ahora:

- Migraciones avanzadas.
- Drivers para plataformas que no usemos.
- Optimizaciones.

Decision Carbura:

```text
SQLDelight es ideal para offline-first.
Si bloquea el avance, se empieza con repositorio fake/local y se integra despues.
```

## 8. Ktor Client o Supabase Kotlin

Documentacion util:

- Ktor Client: <https://ktor.io/docs/client-create-new-application.html>
- Supabase Kotlin: <https://supabase.com/docs/reference/kotlin/introduction>

Lectura minima:

- Como crear cliente HTTP en Kotlin.
- Como configurar headers/API keys.
- Como aislar red detras de un `RemoteDataSource`.

Decision pendiente:

```text
Elegiremos entre Ktor directo o cliente Supabase Kotlin durante el OpenSpec change de datos/backend.
La UI no debe depender de esta decision.
```

## 9. GitHub Actions

Documentacion oficial:

- GitHub Actions quickstart: <https://docs.github.com/actions/quickstart>
- Building and testing Java with Gradle: <https://docs.github.com/actions/use-cases-and-examples/building-and-testing/building-and-testing-java-with-gradle>

Lectura minima:

- Como crear un workflow `.github/workflows/...yml`.
- Como ejecutar Gradle en CI.

No hace falta leer ahora:

- Matrices complejas.
- Releases automatizadas.
- Deploy avanzado.

Decision Carbura:

```text
CI minimo en Entrega 2 si da tiempo: ejecutar tests/build.
CI completo y release quedan para Entrega Final si hace falta.
```

## 10. Orden recomendado para mañana

Como el dia 22 se mueve a manana, manana haremos las tareas del 22 y 23 juntas:

1. Leer este documento.
2. Revisar `docs/entrega2-plan.md`.
3. Leer solo las secciones minimas de KMP.
4. Leer la intro de OpenCode y repasar nuestro flujo OpenSpec.
5. Crear OpenSpec change `bootstrap-kmp-project`.
6. Crear o preparar el proyecto KMP.
7. Intentar que Gradle liste tareas o compile una app Android minima.

Si hay poco tiempo, prioridad absoluta:

```text
KMP scaffold + Android compila > Supabase > UI > Desktop
```

## 11. Senales de que ya podemos empezar

Estamos listos para implementar cuando puedas responder:

- Que rama uso para Entrega 2: `feature-entrega2-AAC`.
- Que plataforma priorizo: Android.
- Que cambio OpenSpec va primero: `bootstrap-kmp-project`.
- Donde vive el dominio: `shared/commonMain`.
- Donde van los tests TDD: `shared/commonTest`.
- Donde van secretos reales: `local.properties`, nunca Git.
- Que queda opcional: Desktop.
