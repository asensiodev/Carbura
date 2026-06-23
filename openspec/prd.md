# PRD - Carbura

> Product Requirements Document
> Autor: Angel Asensio Cuevasanta
> Proyecto: Carbura - TFM AI4Devs
> Version: 1.0 - Mayo 2026

---

## 1. Vision del producto

**Carbura** es una aplicacion multiplataforma (Android + Desktop) para gestionar el mantenimiento de los vehiculos de una familia. Permite registrar revisiones, ITVs, seguros, averias y cambios de aceite o neumaticos, recibir recordatorios antes de que venzan fechas clave, y consultar el historial completo y los costes de cada vehiculo desde cualquier dispositivo.

**Slogan:** *Tu garaje, siempre a punto.*

---

## 2. El problema

Gestionar el mantenimiento de uno o varios vehiculos familiares es una tarea recurrente que hoy se lleva de cabeza o directamente no se lleva:

- Se olvida cuando caduca la ITV hasta que ya ha vencido.
- No se recuerda cuando se hizo el ultimo cambio de aceite ni si ya toca hacerlo.
- Se pierde el historial de mantenimientos y lo que costaron (util al vender el coche).
- Cuando son varios vehiculos en casa, el caos se multiplica.
- No hay un lugar unico donde toda la familia pueda ver y registrar los eventos de todos los coches.

Las apps existentes en el mercado son demasiado complejas (enfocadas en flotas, telemetria OBD2 o registro de combustible), requieren cuenta en la nube con datos dispersos, o tienen UX recargada poco adecuada para un uso familiar simple.

---

## 3. Usuarios

### Usuario principal
**Propietario del vehiculo** - persona con uno o varios vehiculos que quiere tener controlado el mantenimiento sin esfuerzo. Tecnicamente competente (usa smartphone y ordenador con soltura), pero no quiere aprender una herramienta compleja.

**Ejemplo real:** Angel - tiene dos coches y una moto. Se le olvidan las ITVs, no recuerda cuando hizo el ultimo cambio de aceite ni cuanto le costo, y quiere recibir un aviso con tiempo suficiente para no pillarse los dedos.

### Usuario secundario
**Familiar / pareja** - comparte el uso de los vehiculos y tambien registra eventos o consulta recordatorios. Accede desde su propio movil Android al mismo garaje compartido.

**Ejemplo real:** la pareja de Angel - quiere poder ver los recordatorios y anadir mantenimientos sin necesidad de coordinarse manualmente.

---

## 4. Objetivos del MVP

1. Permitir registrar todos los mantenimientos de todos los vehiculos de la familia en un solo lugar.
2. Generar recordatorios automaticos antes de que caduquen fechas clave (especialmente ITV).
3. Notificar al usuario con antelacion suficiente (1 mes antes por defecto para ITV).
4. Mantener un historial completo con fechas, kilometros y costes por vehiculo.
5. Funcionar offline-first: la app funciona siempre, sincroniza cuando hay conexion.
6. Permitir acceso compartido familiar: varios miembros ven y editan el mismo garaje.

---

## 5. Scope del MVP

### Dentro del MVP

- Gestion de multiples vehiculos (coche, moto, furgoneta) bajo un garaje familiar.
- Registro de mantenimientos: ITV, cambio de aceite, cambio de neumaticos, seguro, revision general, averias y tipos personalizados.
- Recordatorios por fecha y/o por kilometros.
- **Notificaciones locales** con antelacion configurable (por defecto: 30 dias antes para ITV y seguro).
- Actualizacion rapida del odometro.
- Historial completo por vehiculo con costes.
- Sincronizacion en la nube entre dispositivos de la misma familia.
- Autenticacion con cuenta de Google.
- Sistema de invitacion para unirse al garaje familiar (codigo de 6 caracteres).
- Exportacion del historial de un vehiculo a PDF/CSV.
- App Android + Desktop (macOS y Windows via Compose Multiplatform).

### Fuera del MVP

- iOS (queda preparado en la arquitectura KMP, se implementa en version futura).
- Registro de combustible y coste por km.
- Integracion con OBD2 o telemetria del vehiculo.
- Notificaciones push remotas (el MVP usa solo notificaciones locales).
- OCR de facturas para autocompletar campos (roadmap IA v2).
- Recomendaciones de mantenimiento basadas en historial (roadmap IA v2).
- Multi-familia (cada workspace es independiente y no transferible).
- Roles y permisos avanzados dentro de la familia.
- Modulo de talleres con ficha completa.

---

## 6. Casos de uso principales

| ID | Nombre | Actor |
|---|---|---|
| UC-01 | Registro e inicio de sesion con Google | Usuario nuevo |
| UC-02 | Alta de vehiculo | Usuario autenticado |
| UC-03 | Registrar mantenimiento o averia | Usuario autenticado |
| UC-04 | Ver historial de un vehiculo | Cualquier miembro de la familia |
| UC-05 | Ver y gestionar recordatorios | Cualquier miembro de la familia |
| UC-06 | Actualizar odometro rapido | Cualquier miembro de la familia |
| UC-07 | Invitar a un familiar al garaje | Propietario del garaje |
| UC-08 | Exportar historial de un vehiculo | Cualquier miembro |
| UC-09 | Sincronizacion entre dispositivos | Sistema (automatico) |

---

## 7. Requisitos no funcionales

- **Offline-first**: todas las operaciones de lectura y escritura funcionan sin conexion. La sincronizacion es eventual y transparente.
- **Privacidad**: los datos no se comparten con terceros. Sin anuncios. Sin analiticas de uso.
- **Rendimiento**: la app debe arrancar en menos de 2 segundos en condiciones normales.
- **Multiplataforma**: Android (API 26+) y Desktop (macOS + Windows) desde la misma base de codigo KMP.
- **Escalabilidad del backend**: arquitectura Supabase con Row Level Security por `family_id`, preparada para multiples familias independientes desde el dia 1.
- **Seguridad**: autenticacion OAuth 2.0 via Google. En Android se prioriza Credential Manager con Google ID y fallback controlado a Google Sign-In/OAuth cuando el dispositivo no lo soporte. Tokens JWT gestionados por Supabase Auth. Secrets nunca en el repositorio.

---

## 8. Stack tecnologico

| Capa | Tecnologia |
|---|---|
| UI Android | Compose for Android |
| UI Desktop | Compose for Desktop opcional, reutilizando design system/componentes compartidos cuando aporte valor |
| UI iOS futura | SwiftUI o Compose Multiplatform a evaluar; arquitectura preparada, fuera del MVP |
| Logica compartida | Kotlin Multiplatform (commonMain) para dominio, casos de uso, contratos, modelos, validaciones y UiState |
| Modularizacion | Modulos Gradle con convention plugins en `build-logic` |
| Design system | `core:designsystem` con tema, tokens y componentes Compose reutilizables |
| Base de datos local | SQLDelight |
| HTTP Client | Ktor Client (KMP) |
| Autenticacion cliente | Contrato KMP comun; Android adapter con Credential Manager + Google ID y fallback Google Sign-In/OAuth; Desktop OAuth opcional; iOS futuro con adapter propio |
| Inyeccion de dependencias | Koin (KMP) |
| Serializacion | kotlinx.serialization |
| Backend / Auth | Supabase (PostgreSQL + Auth + Storage) |
| Almacenamiento de adjuntos | Supabase Storage |
| Sincronizacion | Custom timestamp-based (last-write-wins) |

---

## 9. Arquitectura

Clean Architecture modular. Se comparte en `commonMain` todo lo que sea dominio, contratos, modelos, estado y logica testeable. Las integraciones nativas viven detras de contratos comunes y se implementan por plataforma:

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
SyncManager (commonMain) - gestiona conflictos last-write-wins
```

Patron general para dependencias de plataforma: auth, permisos, notificaciones, secure storage, deep links y APIs del sistema se definen como contratos en KMP y se resuelven con adapters `androidMain`, `desktopMain` o `iosMain` futuro.

---

## 10. Modelo de datos (resumen)

| Entidad | Descripcion |
|---|---|
| `Family` | Workspace familiar. Cada familia tiene sus propios datos aislados. |
| `User` | Miembro de la familia. Vinculado a cuenta de Google via Supabase Auth. |
| `Vehicle` | Vehiculo del garaje (coche, moto, furgoneta). Pertenece a una `Family`. |
| `MaintenanceType` | Tipo de mantenimiento (ITV, aceite, neumaticos, seguro...). Globales + personalizados. |
| `MaintenanceRecord` | Evento de mantenimiento registrado (fecha, km, coste, taller, notas, adjuntos). |
| `Reminder` | Recordatorio futuro por fecha y/o km. Genera notificacion local cuando se acerca. |

---

## 11. Metodologia de desarrollo

- **SDD (Specification-Driven Development)** con OpenSpec: las specs son la fuente de verdad. Todo cambio pasa por `/openspec-proposal` -> `/openspec-apply` -> `/openspec-archive`.
- **TDD (Test-Driven Development)** dentro de cada tarea: Red -> Green -> Refactor.
- **DDD ligero (Domain-Driven Design)** para modelar el dominio principal sin sobrediseñar el MVP: entidades, use cases, repositorios como contratos y value objects solo cuando aporten claridad.
- **Agente de IA**: OpenCode como copiloto en todas las fases (analisis, diseno, implementacion, tests, documentacion).
- **Principios de diseño**: SOLID y CUPID aplicados de forma pragmatica durante el refactor, priorizando testabilidad, claridad de dominio y simplicidad.
- **BDD fuera de alcance**: los criterios de aceptacion se mantendran en OpenSpec sin incorporar una metodologia o herramienta BDD adicional.
- **Convenciones de commits**: Conventional Commits (`feat`, `fix`, `test`, `refactor`, `docs`, `chore`).

---

## 12. Roadmap y plazos

| Fecha | Hito |
|---|---|
| **12 junio 2026** | Entrega de documentacion (PRD, user stories, tickets, readme.md, prompts.md) |
| **10 julio 2026** | Codigo funcional: MVP completo con todas las features del scope |
| **29 julio 2026** | Entrega final refinada: UX pulida, tests, documentacion completa |

### Fases de desarrollo

```text
Mayo 2026        -> Documentacion: PRD, specs, user stories, tickets, readme.md
Junio 2026       -> Implementacion core: auth, vehiculos, mantenimientos, historial
Julio (1-10)     -> Recordatorios, notificaciones, sync, exportacion, Desktop
Julio (10-29)    -> Refinado UX, tests, cobertura, documentacion final
```

---

## 13. Criterios de exito del MVP

- Un usuario puede crear su garaje, anadir vehiculos y registrar mantenimientos en menos de 3 minutos desde cero.
- Los recordatorios de ITV se generan automaticamente al registrar una ITV y notifican 30 dias antes de la proxima.
- Dos dispositivos de la misma familia muestran los mismos datos tras sincronizacion.
- El historial exportado contiene todos los eventos con fecha, km y coste.
- La app funciona completamente offline y sincroniza al recuperar conexion.

---

*Documento vivo - sujeto a revision iterativa durante el desarrollo.*
*Vive en: `openspec/prd.md` del repositorio [asensiodev/Carbura](https://github.com/asensiodev/Carbura)*
