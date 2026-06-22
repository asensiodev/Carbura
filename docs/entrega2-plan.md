# Carbura - Plan Entrega 2

Este documento define como empezar y ejecutar la Entrega 2 de Carbura. La meta es llegar al 10 de julio con un primer MVP ejecutable: backend, frontend y base de datos conectados, con el flujo principal casi completo.

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
- README y prompts actualizados con decisiones y avances.
- PR oficial `feature-entrega2-AAC` -> `main`.

### Opcional

- App Desktop ejecutable desde la misma base KMP.
- Notificaciones locales completas.
- Sincronizacion avanzada entre dispositivos.
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
| Arquitectura | Kotlin Multiplatform | Compartir dominio y datos entre Android/Desktop. |
| UI principal | Compose for Android | Target prioritario de Entrega 2. |
| UI opcional | Compose for Desktop | Solo si Android queda estable. |
| Backend | Supabase | Auth, PostgreSQL y RLS sin servidor propio. |
| Persistencia local | SQLDelight si no bloquea | Base local multiplataforma y testeable. |
| Red | Ktor Client o Supabase Kotlin | Acceso a Supabase desde shared. |
| Tests | kotlin.test + dobles/fakes | TDD en dominio/use cases/repositorios. |
| CI | GitHub Actions minimo | Ejecutar checks/tests cuando existan. |

Decision importante: si SQLDelight o Supabase bloquean demasiado el avance, se puede usar un repositorio local fake/in-memory temporal para cerrar primero el flujo vertical. La condicion es mantener interfaces limpias para sustituirlo por implementacion real sin romper la arquitectura.

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
- actualizar documentacion y prompts.

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
feature-entrega2-AAC -> main
```

Reglas:

- No trabajar directamente en `main`.
- No usar `dev` para Entrega 2; `dev` queda como base historica usada solo para visualizar la PR de Entrega 1.
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
| 1 | `bootstrap-kmp-project` | T-11 parcial | Crear esqueleto KMP, Gradle y estructura base. |
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

### Paso 1 - Preparar rama y entorno

- Confirmar rama `feature-entrega2-AAC`.
- Confirmar Android Studio instalado y actualizado.
- Confirmar JDK compatible.
- Confirmar que Gradle funciona cuando exista proyecto.
- Mantener `local.properties` fuera de Git.

### Paso 2 - Bootstrap KMP

- Crear proyecto KMP minimo.
- Mantener modulos esperados:
  - `shared` para dominio/datos/presentacion compartida.
  - `androidApp` para Android.
  - `desktopApp` opcional si el template lo facilita.
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
GOOGLE_CLIENT_ID=xxxx
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
- `prompts.md` actualizado con prompts usados en implementacion.

## 9. Criterios de listo para Entrega 2

La entrega se considera lista si cumple esto:

- La app Android compila.
- La app Android se puede abrir en emulador o dispositivo.
- Existe un flujo navegable de garaje.
- Se puede crear un vehiculo.
- Se puede registrar un mantenimiento o ITV.
- Se puede consultar historial del vehiculo.
- Se genera un recordatorio automatico basico para ITV o seguro.
- Hay base de datos/backend conectado o, como minimo, persistencia local real y Supabase parcialmente integrado.
- Hay tests unitarios clave en dominio/use cases.
- README incluye instrucciones actualizadas para ejecutar.
- `prompts.md` incluye prompts reales de Entrega 2.
- Existe PR `feature-entrega2-AAC` -> `main`.

## 10. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigacion |
|---|---|---|
| KMP setup consume demasiado tiempo | Alto | Usar template oficial y validar build antes de features. |
| SQLDelight bloquea | Medio/alto | Mantener repositorios e iniciar con fake/local simple temporal. |
| Supabase Auth Google se complica | Alto | Implementar auth/onboarding minimo y documentar configuracion pendiente si hace falta. |
| Desktop retrasa Android | Alto | Desktop es opcional; no bloquear Entrega 2. |
| Demasiado scope de sync | Alto | Usar `pending_sync` y `updated_at`; sync avanzada queda para final. |
| UI consume demasiado tiempo | Medio | Priorizar pantallas funcionales y estados claros, no pulido visual final. |

## 11. Checklist semanal sugerido

### Semana 1

- Rama `feature-entrega2-AAC` creada.
- OpenSpec `bootstrap-kmp-project` creado y aplicado.
- Proyecto KMP compila en Android.
- Estructura `shared` definida.
- `local.properties.example` creado.

### Semana 2

- Supabase creado.
- Tablas principales definidas.
- Entidades y use cases iniciales con tests.
- Primer flujo vertical de vehiculo funcionando con repo fake/local.

### Semana 3

- Registro de mantenimiento funcionando.
- Historial por vehiculo funcionando.
- Recordatorio automatico basico funcionando.
- Persistencia local/remota conectada parcialmente.

### Semana final antes del 10 de julio

- Build limpio.
- Tests clave pasan.
- README actualizado.
- `prompts.md` actualizado.
- PR abierta contra `main`.
- Evidencia minima: capturas o video corto del flujo Android.

## 12. Primer paso recomendado

El primer cambio OpenSpec deberia ser:

```text
bootstrap-kmp-project
```

Objetivo:

- crear el esqueleto KMP;
- definir estructura de modulos;
- dejar Android compilando;
- preparar base para tests y configuracion segura.

No conviene empezar por toda la UI ni por toda la configuracion de Supabase. Primero necesitamos una base KMP estable que compile.
