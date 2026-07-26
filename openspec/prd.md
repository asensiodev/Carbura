# PRD - Carbura

> Documento de requisitos de producto
> Autor: Angel Asensio Cuevasanta
> Proyecto: Carbura - TFM AI4Devs
> Version: 1.0 - Mayo 2026

---

## 1. Vision del producto

**Carbura** tiene como vision gestionar el mantenimiento de los vehiculos de una familia. La version `1.0.0` entrega un espacio personal por cuenta, sin miembros ni invitaciones; la colaboracion familiar permanece como evolucion futura. La entrega materializa clientes Android y Desktop macOS sobre una base Kotlin Multiplatform. El codigo Desktop mantiene compatibilidad y empaquetado configurado para Windows, pero MSI, ejecucion y Credential Manager quedan fuera del alcance comprobado por no disponer de un PC Windows; iOS queda fuera del alcance. Permite registrar revisiones, ITVs, seguros, averias y cambios de aceite o neumaticos, sincronizar recordatorios y consultar el historial y los costes individuales de cada vehiculo. Android es la unica plataforma que entrega notificaciones nativas.

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

### Usuario secundario futuro
**Familiar / pareja** - comparte el uso de los vehiculos y tambien registra eventos o consulta recordatorios. Accede desde su propio movil Android al mismo garaje compartido.

**Ejemplo real:** la pareja de Angel - quiere poder ver los recordatorios y anadir mantenimientos sin necesidad de coordinarse manualmente.

---

## 4. Objetivos del MVP

1. Permitir registrar todos los mantenimientos de todos los vehiculos de la familia en un solo lugar. *(core)*
2. Generar recordatorios automaticos antes de que caduquen fechas clave (especialmente ITV). *(core)*
3. Notificar al usuario con politicas locales adecuadas al tipo de recordatorio. *(extendido)*
4. Mantener un historial completo con fechas, kilometros y costes por vehiculo. *(core)*
5. Funcionar offline-first: la app funciona siempre en local; la sincronizacion entre dispositivos es objetivo extendido. *(core local / extendido sync)*
6. Permitir acceso compartido familiar: varios miembros ven y editan el mismo garaje. *(evolucion futura, no incluida en 1.0.0)*

---

## 5. Alcance del MVP

El scope se divide en dos niveles alineados con `docs/user-stories.md`: el **MVP entregado** y la **evolucion futura**. Algunas capacidades inicialmente extendidas, como Desktop y la sincronizacion, se incorporaron finalmente a la version `1.0.0`; invitaciones y exportacion permanecen pendientes.

### MVP core (Must-Have)

- Gestion de multiples vehiculos (coche, moto, furgoneta) bajo un garaje familiar.
- Registro de mantenimientos: ITV, cambio de aceite, cambio de neumaticos, seguro, revision general, averias y tipos personalizados.
- Historial por vehiculo con costes individuales; el agregado de costes queda pendiente.
- Recordatorios manuales y sugerencias proactivas a partir de los objetivos de ITV, seguro y kilometraje del vehículo. Un mantenimiento futuro permite guardar solo el registro o crear además un recordatorio determinista.
- Autenticacion con cuenta de Google.
- Persistencia local offline-first (la app funciona sin conexion en el dispositivo).
- App Android como plataforma principal de la demo E2E.

### MVP extendido (Should-Have / Could-Have)

- Pantalla de proximos recordatorios y **notificaciones locales Android**. Los avisos generados para ITV usan hitos de 60, 30 y 7 dias; los de seguro, 45, 37 y 7 dias; los recordatorios manuales avisan en su fecha objetivo. *(Implementado)*
- App Desktop macOS desde la misma base KMP. El target Windows está implementado/configurado, pero no forma parte del alcance validado de la entrega por falta de un PC Windows.
- Recordatorios por kilometros y actualizacion rapida del odometro. *(Implementado en Android y Desktop)*
- Sincronizacion en la nube entre instalaciones autenticadas de la misma cuenta. *(Implementado como sync v0)*
- Sistema de invitacion para unirse al garaje familiar. *(Evolucion futura)*
- Exportacion del historial de un vehiculo a PDF/CSV. *(Evolucion futura)*

### Fuera del MVP

- iOS (fuera del alcance de la entrega).
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
| UC-04 | Ver historial de un vehiculo | Usuario de la cuenta |
| UC-05 | Ver y gestionar recordatorios | Usuario de la cuenta |
| UC-06 | Actualizar odometro rapido | Usuario de la cuenta |
| UC-07 | Invitar a un familiar al garaje | Propietario del garaje (futuro, fuera de 1.0.0) |
| UC-08 | Exportar historial de un vehiculo | Usuario de la cuenta (futuro) |
| UC-09 | Sincronizacion entre dispositivos | Sistema (automatico) |

---

## 7. Requisitos no funcionales

- **Local-first**: las mutaciones de vehiculos, mantenimientos y recordatorios se guardan primero en SQLDelight y quedan pendientes ante fallos remotos. El arranque autenticado resuelve sesion y familia e intenta una sincronizacion inicial antes de mostrar el contenido principal.
- **Privacidad**: los datos no se comparten con terceros. Sin anuncios. Sin analiticas de uso.
- **Rendimiento**: se mantiene como objetivo un arranque inferior a 2 segundos en condiciones normales; no se registro una medicion formal en esta entrega.
- **Multiplataforma**: Android (API 26+) y Desktop macOS son entregables ejecutables validados; la base KMP comparte dominio, datos y presentacion. Windows permanece como target implementado no validado y iOS no forma parte del alcance.
- **Escalabilidad del backend**: arquitectura Supabase con Row Level Security por `family_id`, preparada para multiples familias independientes desde el dia 1.
- **Seguridad**: autenticacion con Google ID mediante Credential Manager y Supabase Auth. El flujo actual permite reintentar ante error; un fallback OAuth alternativo queda como mejora. Tokens JWT gestionados por Supabase Auth y secretos fuera del repositorio.

---

## 8. Stack tecnologico

| Capa | Tecnologia |
|---|---|
| UI Android | Compose for Android |
| UI Desktop | Compose Desktop, reutilizando logica y componentes compartidos cuando aporta valor |
| UI iOS posible | SwiftUI o Compose Multiplatform a evaluar en una fase posterior |
| Logica compartida | Kotlin Multiplatform (commonMain) para dominio, casos de uso, contratos, modelos, validaciones y UiState |
| Modularizacion | Modulos Gradle con convention plugins en `build-logic` |
| Design system | `core:designsystem` con tema y tokens Android; evolucion multiplataforma futura |
| Base de datos local | SQLDelight |
| HTTP Client | Ktor Client (KMP) |
| Autenticacion cliente | Android con Credential Manager + Google ID; Desktop con OAuth PKCE y vault nativo |
| Inyeccion de dependencias | Koin (KMP) |
| Serializacion | kotlinx.serialization |
| Backend / Auth | Supabase Auth, PostgreSQL, PostgREST y RLS |
| Almacenamiento de adjuntos | Supabase Storage, previsto pero no integrado |
| Sincronizacion | Full pull por familia de entidades, push de pendientes, tombstones y `last-write-wins` |

---

## 9. Arquitectura

Clean Architecture modular. Se comparte en `commonMain` todo lo que sea dominio, contratos, modelos, estado y logica testeable. Las integraciones nativas viven detras de contratos comunes y se implementan por plataforma:

```text
Presentation (Compose Android; Compose Desktop)
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

Patron general para dependencias de plataforma: auth, permisos, notificaciones, secure storage y APIs del sistema se definen como contratos cuando comparten semantica. Android y Desktop aportan adaptadores productivos; las notificaciones nativas son exclusivas de Android.

---

## 10. Modelo de datos (resumen)

| Entidad | Descripcion |
|---|---|
| `Family` | Espacio tecnico aislado de una cuenta en 1.0.0; preparado para evolucionar hacia un workspace familiar. |
| `UserProfile` | Perfil vinculado a una cuenta de Google mediante Supabase Auth. La gestion de varios miembros es futura. |
| `Vehicle` | Vehiculo del garaje (coche, moto, furgoneta). Pertenece a una `Family`. |
| `MaintenanceType` | Tipo de mantenimiento (ITV, aceite, neumaticos, seguro...). Globales + personalizados. |
| `MaintenanceRecord` | Evento de mantenimiento registrado (fecha, km, coste, moneda, taller, notas y proxima fecha opcional en el modelo). |
| `Reminder` | Recordatorio futuro por fecha y/o km. Los que tienen fecha pueden generar notificaciones locales en Android. |

---

## 11. Metodologia de desarrollo

- **SDD (Specification-Driven Development)** con OpenSpec: las specs son la fuente de verdad. Los cambios se exploran, proponen, aplican y archivan con `/opsx-explore`, `/opsx-propose`, `/opsx-apply` y `/opsx-archive`.
- **TDD (Test-Driven Development)** aplicado de forma pragmatica a comportamientos estables y automatizables: Red -> Green -> Refactor.
- **DDD ligero (Domain-Driven Design)** para modelar el dominio principal sin sobrediseñar el MVP: entidades, use cases, repositorios como contratos y value objects solo cuando aporten claridad.
- **Agente de IA**: OpenCode como copiloto en todas las fases (analisis, diseno, implementacion, tests, documentacion).
- **Principios de diseño**: SOLID y CUPID aplicados de forma pragmatica durante el refactor, priorizando testabilidad, claridad de dominio y simplicidad.
- **BDD fuera de alcance**: los criterios de aceptacion se mantendran en OpenSpec sin incorporar una metodologia o herramienta BDD adicional.
- **Convenciones de commits**: Conventional Commits (`feat`, `fix`, `test`, `refactor`, `docs`, `chore`).

---

## 12. Hoja de ruta y plazos

| Fecha | Hito |
|---|---|
| **12 junio 2026** | Entrega de documentacion (PRD, user stories, tickets, readme.md y toolchain de IA/proceso) |
| **10 julio 2026** | Codigo funcional: MVP Android con auth, vehiculos, mantenimientos, historial, recordatorios, notificaciones locales y sync con backend/base de datos conectados |
| **29 julio 2026** | Entrega final refinada: Android/Desktop, UX pulida, tests, CI, evidencia de despliegue y documentacion completa |

### Fases de desarrollo

```text
Mayo 2026        -> Documentacion: PRD, specs, user stories, tickets, readme.md
Junio 2026       -> Implementacion core: auth, vehiculos, mantenimientos, historial
Julio (1-10)     -> Recordatorios, notificaciones locales, sync v0, UX polish, cierre del flujo E2E Android
Julio (10-29)    -> Desktop, CI, E2E, empaquetado, evidencias y refinado UX
                    Exportacion e invitaciones permanecen como evolucion futura
```

### Nota sobre dedicacion

Las instrucciones del curso estiman una dedicacion orientativa de ~30 horas. Este proyecto asume explicitamente una **dedicacion mayor** por decision del autor, dado el stack elegido (KMP + Compose Multiplatform + Supabase). Aun asi, la priorizacion core/extendido del scope (seccion 5) se mantiene como mecanismo de control: si el tiempo real disponible se reduce, se recorta el MVP extendido sin comprometer el flujo E2E core.

---

## 13. Criterios de exito del MVP

### Criterios core (obligatorios para la entrega final)

- Un usuario puede iniciar sesion, disponer de su espacio personal, anadir vehiculos y registrar mantenimientos. El objetivo de completar el recorrido inicial en menos de 3 minutos no se midio formalmente.
- El usuario puede crear recordatorios manuales y aceptar sugerencias proactivas derivadas de los objetivos del vehiculo sin duplicados.
- El historial muestra los eventos con fecha, km y coste individual, ordenados por fecha descendente.
- Las mutaciones principales permanecen disponibles localmente y convergen con Supabase mediante sync v0 cuando la app esta activa.
- La suite de calidad, tests y compilacion Android pasa en CI; la integracion Android E2E dentro del proceso se implemento con fronteras externas deterministas.

### Criterios extendidos (si se implementa el MVP extendido)

- Los recordatorios con fecha generan notificaciones locales Android segun su politica de aviso.
- Dos instalaciones autenticadas con la misma cuenta muestran los mismos datos tras sincronizacion.
- Pendiente: el historial exportado a PDF/CSV contendra los eventos con fecha, km y coste.

---

*Documento vivo - sujeto a revision iterativa durante el desarrollo.*
*Vive en: `openspec/prd.md` del repositorio [asensiodev/Carbura](https://github.com/asensiodev/Carbura)*
