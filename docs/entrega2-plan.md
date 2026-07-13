# Carbura - Plan Entrega 2

> **Nota histórica:** este documento conserva íntegramente la planificación utilizada para la Entrega 2; no describe por sí solo el estado actual del producto.

Este documento define como empezar y ejecutar la Entrega 2 de Carbura. La meta es llegar al 10 de julio con un primer MVP ejecutable: backend, frontend y base de datos conectados, con el flujo principal casi completo.

> Estado actual: plan historico de ejecucion. La Entrega 2 se cerro como MVP Android-first con auth Google/Supabase, vehiculos, mantenimientos, recordatorios, notificaciones locales y sync v0. Desktop, invitaciones completas, exportacion, CI/release final y test E2E quedan para Entrega final o post-MVP.

## 1. Objetivo de la Entrega 2

La Entrega 2 debe demostrar que Carbura ya no es solo documentacion: debe existir una app Android ejecutable con un flujo vertical funcional.

Objetivo principal:

```text
Android funcional primero
  -> login/onboarding minimo
  -> garaje familiar
  -> alta de vehiculo
  -> registro de mantenimiento
  -> historial por vehiculo
  -> recordatorio automatico basico
  -> datos persistidos y conectados a backend/base de datos
```

Desktop queda como objetivo opcional. La arquitectura seguira siendo Kotlin Multiplatform para poder incorporar Desktop si el tiempo lo permite, pero la entrega no debe depender de tener Desktop completo.

## 2. Alcance

### Incluido

- Proyecto Kotlin Multiplatform inicializado y compilando.
- App Android como target principal.
- Modulo shared con dominio, casos de uso, repositorios y tests unitarios.
- Supabase configurado para Auth y PostgreSQL.
- Persistencia local preparada para offline-first.
- Flujo principal casi completo en Android.
- Tests unitarios clave para dominio/use cases.
- README y documentacion actualizados con decisiones y avances.
- PR academica `feature-entrega2-AAC` -> `dev`, manteniendo `main` sincronizada con los cambios aprobados.

### Opcional planificado inicialmente

- App Desktop ejecutable desde la misma base KMP.
- Notificaciones locales completas. Se implementaron en Android para Entrega 2.
- Sincronizacion avanzada entre dispositivos. En Entrega 2 se implemento sync v0; WorkManager/realtime quedan fuera.
- UI mas pulida, animaciones o estados visuales avanzados.

### Fuera de alcance hasta Entrega Final

- Exportacion PDF/CSV.
- Adjuntos de facturas.
- OCR.
- Reglas avanzadas de recordatorios por kilometraje.
- Resolucion manual de conflictos de sincronizacion.
- Multi-garaje por usuario.

## 3. Stack recomendado

| Area | Decision | Motivo |
|---|---|---|
| Lenguaje | Kotlin | Encaja con Android y KMP. |
| Arquitectura | Kotlin Multiplatform modular | Compartir dominio, contratos, datos, UiState y tests entre Android/Desktop/iOS futuro. |
| Build | Convention plugins en `build-logic` | Evitar duplicacion Gradle y mostrar arquitectura escalable. |
| UI principal | Compose for Android | Target prioritario de Entrega 2. |
| UI opcional | Compose for Desktop | Solo si Android queda estable; reutiliza design system y componentes compartidos. |
| UI futura | SwiftUI o Compose Multiplatform iOS | iOS queda preparado por contratos y modulos, no implementado en el MVP. |
| Design system | `core:designsystem` | Tema, tokens y componentes Compose base reutilizables. |
| Backend | Supabase | Auth, PostgreSQL y RLS sin servidor propio. |
| Persistencia local | SQLDelight si no bloquea | Base local multiplataforma y testeable. |
| Red | Ktor Client o Supabase Kotlin | Acceso a Supabase desde shared. |
| Tests | kotlin.test + dobles/fakes | TDD en dominio/use cases/repositorios. |
| CI | GitHub Actions minimo | Ejecutar checks/tests cuando existan. |

Decision importante: si SQLDelight o Supabase bloquean demasiado el avance, se puede usar un repositorio local fake/in-memory temporal para cerrar primero el flujo vertical. La condicion es mantener interfaces limpias para sustituirlo por implementacion real sin romper la arquitectura.

Patron general para dependencias de plataforma: todo lo que dependa del sistema operativo se define como contrato comun y se implementa con adapters por target. Esto aplica a auth, permisos, notificaciones, storage seguro, deep links, archivos y APIs especificas de Android/Desktop/iOS futuro.

## 4. IDE y herramientas

### Android Studio

IDE principal para:

- crear o abrir el proyecto KMP;
- configurar Gradle;
- compilar Android;
- ejecutar emulador;
- depurar errores de build;
- revisar previews si se usan.

### OpenCode

Herramienta principal de asistencia para:

- crear y aplicar cambios OpenSpec;
- generar tests TDD;
- implementar dominio, repositorios y UI;
- refactorizar con SOLID/CUPID;
- actualizar documentacion.

### Supabase Dashboard

Se usara para:

- crear el proyecto Supabase;
- configurar Google OAuth;
- crear tablas;
- activar Row Level Security;
- probar queries y policies;
- obtener `SUPABASE_URL` y `SUPABASE_ANON_KEY`.

### Cursor

Opcional. No sera herramienta principal para evitar dividir el flujo. Si se usa, debe respetar OpenSpec como fuente de verdad y no introducir cambios fuera de la rama de Entrega 2.

## 5. Flujo Git

Rama de trabajo:

```text
feature-entrega2-AAC
```

PR oficial:

```text
feature-entrega2-AAC -> dev
```

Reglas:

- No trabajar directamente en `main`.
- Usar `dev` como rama destino academica de Entrega 2 para visualizar el diff; mantener `main` actualizada con los cambios aprobados.
- Hacer commits pequenos y trazables por bloque: `docs`, `chore`, `feat`, `test`, `refactor`, `fix`.
- Antes de abrir la PR, ejecutar los comandos disponibles de build/test.

## 6. Flujo OpenSpec + OpenCode

OpenSpec sera la fuente de verdad de SDD. Cada bloque grande se hara asi:

```text
Ticket en docs/backlog.md
  -> OpenSpec proposal
  -> revision humana
  -> OpenSpec apply
  -> tests TDD Red
  -> codigo minimo Green
  -> refactor SOLID/CUPID
  -> OpenSpec archive
  -> commit
```

No se debe implementar una feature importante directamente desde el backlog sin pasar antes por un cambio OpenSpec.

## 7. OpenSpec changes sugeridos

Orden recomendado:

| Orden | Change ID sugerido | Tickets | Objetivo |
|---|---|---|---|
| 1 | `bootstrap-kmp-project` | T-11 parcial | Crear esqueleto KMP modular, convention plugins, design system base y estructura app/core/feature. |
| 2 | `add-initial-data-model` | T-01 | Definir modelo local/remoto y contratos de datos. |
| 3 | `add-auth-family-garage` | T-02 | Login/onboarding minimo y garaje familiar. |
| 4 | `add-vehicle-management` | T-03, T-07 | Crear vehiculo y verlo en garaje. |
| 5 | `add-maintenance-history` | T-04, T-08 | Registrar mantenimiento y consultar historial. |
| 6 | `add-automatic-reminders` | T-05, T-09 parcial | Crear recordatorio automatico visible. |
| 7 | `add-delivery2-quality-checks` | T-12 parcial | Tests, checks y documentacion de entrega. |

Cada change debe tener:

- `proposal.md` con objetivo y alcance.
- `tasks.md` con tareas ejecutables.
- spec delta si cambia comportamiento esperado.
- tests TDD definidos antes de implementar.

## 8. Orden de implementacion tecnico

Antes de empezar con codigo, revisar la guia de lecturas previas en [`docs/entrega2-reading-list.md`](entrega2-reading-list.md). La lectura debe ser selectiva: KMP, OpenCode/OpenSpec y Supabase minimo, sin entrar aun en documentacion avanzada.

### Paso 1 - Preparar rama y entorno

- Confirmar rama `feature-entrega2-AAC`.
- Confirmar Android Studio instalado y actualizado.
- Confirmar JDK compatible.
- Confirmar que Gradle funciona cuando exista proyecto.
- Mantener `local.properties` fuera de Git.

### Paso 2 - Bootstrap KMP

- Crear proyecto KMP modular.
- Crear `build-logic` con convention plugins basicos.
- Mantener modulos esperados:
  - `app:android` para Android.
  - `app:shared` para rutas/contratos compartidos de app.
  - `core:model`, `core:domain`, `core:data`, `core:auth`, `core:designsystem`.
  - `feature:onboarding`, `feature:garage`, `feature:maintenance`, `feature:reminders` cuando el scaffold lo permita sin bloquear.
- Verificar que Android compila antes de seguir.

Comandos objetivo:

```bash
./gradlew tasks
./gradlew test
./gradlew assembleDebug
```

### Paso 3 - Configuracion segura

- Crear `local.properties.example` con claves ficticias.
- Usar `local.properties` real solo en local.
- Confirmar que `.gitignore` excluye secretos.

Variables previstas:

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=xxxx
GOOGLE_CLIENT_ID=xxxx.apps.googleusercontent.com
```

### Paso 4 - Supabase minimo

- Crear proyecto Supabase.
- Crear tablas principales:
  - `families`
  - `user_profiles`
  - `vehicles`
  - `maintenance_types`
  - `maintenance_records`
  - `reminders`
- Activar RLS.
- Crear policies por `family_id`.
- Configurar Google OAuth.
- Planificar Android Auth con Credential Manager + Google ID como flujo principal.
- Mantener fallback controlado a Google Sign-In/OAuth si Credential Manager no esta disponible o no devuelve credenciales validas.

Primero puede validarse manualmente desde Supabase SQL Editor. Despues se conecta desde la app.

### Paso 5 - Dominio shared

- Crear entidades:
  - `Family`
  - `UserProfile`
  - `Vehicle`
  - `MaintenanceType`
  - `MaintenanceRecord`
  - `Reminder`
- Crear use cases iniciales:
  - `CreateVehicleUseCase`
  - `CreateMaintenanceRecordUseCase`
  - `GetVehicleHistoryUseCase`
  - `CreateAutomaticReminderUseCase`
- Crear contratos de repositorio.
- Crear contratos comunes para integraciones nativas, empezando por auth.

TDD prioritario aqui: los tests de dominio son mas baratos, estables y dan seguridad.

### Paso 6 - Primer flujo vertical

Primer hito funcional:

```text
Abrir app Android
  -> ver garaje vacio
  -> anadir vehiculo
  -> ver vehiculo en lista
```

Se puede empezar con repositorio fake/in-memory si la persistencia real aun no esta conectada. El objetivo es cerrar flujo vertical cuanto antes.

### Paso 7 - Segundo flujo vertical

Segundo hito funcional:

```text
Abrir vehiculo
  -> registrar mantenimiento o ITV
  -> ver historial
  -> generar recordatorio automatico basico
```

### Paso 8 - Conectar persistencia/backend

- Sustituir fake/in-memory por repositorio local/remoto.
- Guardar vehiculos y mantenimientos.
- Probar que los datos sobreviven a reinicio si la persistencia local esta lista.
- Probar conexion a Supabase para al menos una parte del flujo.

### Paso 9 - Calidad Entrega 2

- Tests unitarios de use cases principales.
- Tests de validacion basicos.
- Checks de build.
- README actualizado con instrucciones reales de ejecucion.
- `docs/toolchain/carbura_toolchain.md` actualizado con herramientas IA, proceso y evidencias.

## 9. Criterios de listo para Entrega 2

La entrega se considera lista si cumple esto:

- La app Android compila.
- La app Android se puede abrir en emulador o dispositivo.
- Existe un flujo navegable de garaje.
- Se puede crear un vehiculo.
- Se puede registrar un mantenimiento o ITV.
- Se puede consultar historial del vehiculo.
- Se pueden crear y gestionar recordatorios por fecha y/o kilometraje.
- Hay persistencia local real, Supabase integrado y sync v0 funcional para entidades principales.
- Hay tests unitarios clave en dominio/use cases.
- README incluye instrucciones actualizadas para ejecutar.
- `docs/toolchain/carbura_toolchain.md` incluye herramientas IA, proceso y evidencias de Entrega 2.
- Existe PR academica `feature-entrega2-AAC` -> `dev`.

## 10. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigacion |
|---|---|---|
| KMP setup consume demasiado tiempo | Alto | Usar template oficial y validar build antes de features. |
| SQLDelight bloquea | Medio/alto | Mantener repositorios e iniciar con fake/local simple temporal. |
| Supabase Auth Google se complica | Alto | Implementar auth/onboarding minimo y documentar configuracion pendiente si hace falta. |
| Desktop retrasa Android | Alto | Desktop es opcional; no bloquear Entrega 2. |
| Demasiado scope de sync | Alto | Usar `pending_sync` y `updated_at`; sync avanzada queda para final. |
| UI consume demasiado tiempo | Medio | Priorizar pantallas funcionales y estados claros, no pulido visual final. |

## 11. Plan diario hasta el 5 de julio

Objetivo interno: tener el MVP Android terminado el **5 de julio**. Del **6 al 10 de julio** queda reservado para bugs, integracion, documentacion, evidencia de entrega y Desktop opcional si el MVP Android esta estable.

### 22 junio - Preparacion de Entrega 2

- Confirmar rama `feature-entrega2-AAC`.
- Revisar `docs/entrega2-plan.md`, `docs/backlog.md`, `readme.md` y `openspec/prd.md`.
- Crear el primer cambio OpenSpec: `bootstrap-kmp-project`.
- Definir en la spec la estructura minima KMP y los comandos de verificacion esperados.

Resultado esperado: plan operativo claro y cambio OpenSpec listo para iniciar el proyecto.

### 23 junio - Bootstrap KMP

- Crear el proyecto Kotlin Multiplatform con Android como target principal.
- Crear estructura modular base `app`, `core`, `feature` y `build-logic`.
- Dejar Desktop fuera si bloquea Android; usar `app:shared` para rutas/contratos compartidos.
- Confirmar que Gradle lista tareas correctamente.
- Crear `local.properties.example` si todavia no existe.

Resultado esperado: proyecto KMP inicial compila o, como minimo, Gradle esta configurado y desbloqueado.

### 24 junio - Build Android estable

- Resolver errores de Gradle/KMP.
- Ejecutar `./gradlew test` y `./gradlew assembleDebug`.
- Crear pantalla Android minima de arranque.
- Archivar OpenSpec `bootstrap-kmp-project` si el scaffold queda estable.

Resultado esperado: app Android abre una pantalla basica.

### 25 junio - Modelo de datos y dominio base

- Crear OpenSpec `add-initial-data-model`.
- Definir entidades de dominio: `Family`, `UserProfile`, `Vehicle`, `MaintenanceType`, `MaintenanceRecord`, `Reminder`.
- Crear contratos de repositorio.
- Escribir primeros tests TDD de validacion de dominio.

Resultado esperado: dominio base en `shared` con tests unitarios iniciales.

### 26 junio - Supabase minimo

- Crear proyecto Supabase.
- Crear tablas principales en PostgreSQL.
- Activar RLS.
- Preparar policies iniciales por `family_id`.
- Documentar SQL o pasos relevantes en la repo si aplica.

Resultado esperado: backend remoto minimo creado y alineado con el modelo de datos.

### 27 junio - Auth y garaje familiar

- Crear OpenSpec `add-auth-family-garage`.
- Configurar Google OAuth en Supabase si es viable.
- Implementar estado de sesion/onboarding minimo.
- Crear flujo para cargar o crear garaje familiar.
- Añadir tests de casos de uso o repositorio con fakes.

Resultado esperado: base de auth/onboarding preparada, aunque la integracion Google pueda requerir ajuste final.

### 28 junio - Alta de vehiculo dominio/datos

- Crear OpenSpec `add-vehicle-management` si no se creo antes.
- Implementar `CreateVehicleUseCase` con TDD.
- Implementar repositorio fake/local para vehiculos.
- Preparar persistencia local o puente hacia Supabase si ya esta desbloqueado.

Resultado esperado: vehiculo se puede crear desde dominio/repositorio con tests verdes.

### 29 junio - Alta de vehiculo UI Android

- Crear pantalla de garaje vacio.
- Crear formulario de alta de vehiculo.
- Conectar UI con ViewModel/use case.
- Mostrar validaciones, loading, error y exito basicos.

Resultado esperado: primer flujo vertical Android funciona: garaje vacio -> anadir vehiculo -> ver lista.

### 30 junio - Registro de mantenimiento dominio/datos

- Crear OpenSpec `add-maintenance-history`.
- Implementar `CreateMaintenanceRecordUseCase` con TDD.
- Implementar validaciones de tipo, fecha, kilometros y coste.
- Implementar consulta de historial ordenado por fecha.

Resultado esperado: mantenimientos se crean y el historial se consulta desde dominio/repositorio.

### 1 julio - Registro de mantenimiento UI Android

- Crear detalle de vehiculo.
- Crear formulario de mantenimiento/ITV.
- Conectar formulario con use case.
- Mostrar historial por vehiculo.
- Validar estados vacios y errores.

Resultado esperado: segundo flujo vertical funciona: vehiculo -> registrar mantenimiento -> ver historial.

### 2 julio - Recordatorio automatico basico

- Crear OpenSpec `add-automatic-reminders`.
- Implementar `CreateAutomaticReminderUseCase`.
- Crear recordatorio al registrar ITV o seguro con fecha de vencimiento.
- Mostrar recordatorio basico en detalle o pantalla simple.

Resultado esperado: registrar ITV/seguro genera un recordatorio visible.

### 3 julio - Persistencia real e integracion

- Sustituir fakes por persistencia local/remota donde sea viable.
- Confirmar que vehiculos y mantenimientos sobreviven al reinicio si hay persistencia local.
- Confirmar conexion real con Supabase para al menos auth/perfil o datos principales.
- Mantener fakes solo donde sean necesarios para no bloquear el flujo.

Resultado esperado: backend/base de datos conectados al flujo principal o integracion parcial documentada.

### 4 julio - Calidad y estabilizacion

- Ejecutar build y tests.
- Corregir bugs del flujo principal.
- Completar tests unitarios clave.
- Revisar OpenSpec archives pendientes.
- Revisar que no haya secretos en Git.

Resultado esperado: MVP Android estable para demo interna.

### 5 julio - Cierre interno del MVP Android

- Ejecutar flujo completo en emulador o dispositivo.
- Actualizar `readme.md` con instrucciones reales de ejecucion.
- Actualizar `docs/toolchain/carbura_toolchain.md` con herramientas IA, proceso y evidencias de Entrega 2.
- Preparar capturas o video corto del flujo.
- Revisar diff y abrir PR draft si ya tiene sentido.

Resultado esperado: Entrega 2 funcionalmente cerrada antes del colchon.

## 12. Colchon del 6 al 10 de julio

### 6 julio - Bugs e integracion

- Corregir bugs detectados en demo interna.
- Simplificar scope si alguna integracion no es estable.
- Asegurar que Android sigue compilando limpio.

### 7 julio - Supabase y seguridad

- Revisar RLS y policies.
- Verificar variables locales y `local.properties.example`.
- Documentar pasos de Supabase pendientes o definitivos.

### 8 julio - Desktop opcional o pulido Android

- Si Android esta estable, probar Desktop.
- Si Desktop bloquea, no incluirlo.
- Si no se hace Desktop, pulir UI Android y estados vacios/error.

### 9 julio - Preparacion PR

- Actualizar README, documentacion y evidencia.
- Ejecutar checks disponibles.
- Revisar PR description.
- Confirmar trazabilidad tickets -> OpenSpec -> tests/codigo.

### 10 julio - Entrega

- Abrir o finalizar PR `feature-entrega2-AAC` -> `dev`.
- Enviar URL de PR en Typeform.
- Guardar notas de riesgos conocidos para Entrega Final.

## 13. Primer paso recomendado

El primer cambio OpenSpec deberia ser:

```text
bootstrap-kmp-project
```

Objetivo:

- crear el esqueleto KMP;
- definir estructura modular `app`, `core`, `feature` y `build-logic`;
- crear convention plugins minimos;
- preparar `core:designsystem`;
- dejar Android compilando;
- preparar base para tests y configuracion segura.

No conviene empezar por toda la UI ni por toda la configuracion de Supabase. Primero necesitamos una base KMP estable que compile.
