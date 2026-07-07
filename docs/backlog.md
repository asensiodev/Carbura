# Carbura - Backlog de Tickets

Este documento detalla los tickets de trabajo derivados de las historias de usuario del MVP. Sigue la estructura recomendada en el material de AI4Devs para tickets Agile: titulo, descripcion, criterios de aceptacion, prioridad, estimacion, area responsable, etiquetas, referencias e historial.

Los tickets se implementaran mediante SDD con OpenSpec. Antes de ejecutar cada bloque de trabajo se creara un cambio en `openspec/changes/` con `proposal.md`, `tasks.md` y spec delta cuando aplique. La implementacion seguira TDD: Red -> Green -> Refactor.

## Scope MVP dia 10

- Incluido: login Google real, garaje local persistente, historial de mantenimiento local persistente, recordatorios MVP locales, y UI Android con edge-to-edge.
- Recordatorios MVP: crear, listar pendientes y marcar completados por familia/vehiculo, con fecha y/o kilometraje objetivo.
- Diferido: notificaciones locales, recordatorios recurrentes, sync remoto de recordatorios, invitaciones familiares, y Desktop.

## Orden de implementacion recomendado

| Orden | Ticket | Area | Historias | Prioridad | Estimacion |
|---|---|---|---|---|---|
| 1 | T-01 - Esquema local/remoto del MVP | Datos | US-01, US-02, US-04, US-06 | Must | 8 SP |
| 2 | T-02 - Auth y garaje familiar | Auth / backend gestionado | US-01 | Must | 5 SP |
| 3 | T-03 - Crear vehiculo offline-first | Dominio / repositorio | US-02 | Must | 5 SP |
| 4 | T-04 - Registro e historial de mantenimiento | Dominio / repositorio | US-04, US-05 | Must | 8 SP |
| 5 | T-05 - Recordatorio automatico | Dominio | US-06 | Must | 5 SP |
| 6 | T-06 - Preparacion de sincronizacion | Sincronizacion | US-02, US-04 | Must | 8 SP |
| 7 | T-07 - Formulario alta de vehiculo | Frontend | US-02 | Must | 5 SP |
| 8 | T-08 - Formulario mantenimiento e historial | Frontend | US-04, US-05 | Must | 8 SP |
| 9 | T-09 - Lista de recordatorios | Frontend | US-07 | Should | 5 SP |
| 10 | T-10 - Notificaciones locales | Plataforma | US-08 | Should | 5 SP |

## T-01 - Esquema local/remoto del MVP

**Tipo:** tarea tecnica / datos.

**Descripcion:** definir el esquema inicial local con SQLDelight y remoto con Supabase PostgreSQL para soportar familias, perfiles, vehiculos, tipos de mantenimiento, registros y recordatorios.

**Proposito:** habilitar una base persistente, relacional y sincronizable para el flujo E2E del MVP.

**Historias relacionadas:** US-01, US-02, US-04, US-06.

**Prioridad:** Must-Have.

**Estimacion:** 8 story points.

**Responsable:** datos / backend.

**Etiquetas:** `data`, `sqldelight`, `supabase`, `security`, `mvp`.

**Alcance:**

- Crear tablas `families`, `user_profiles`, `vehicles`, `maintenance_types`, `maintenance_records` y `reminders`.
- Incluir `created_at`, `updated_at`, `deleted_at` y campos necesarios para sync.
- Definir relaciones, indices y constraints basicas.
- Preparar politicas RLS por `family_id` en Supabase.

**Fuera de alcance:** migraciones historicas complejas, auditoria avanzada, multi-garaje por usuario.

**Criterios de aceptacion:**

- Dado un usuario autenticado, cuando crea un garaje, entonces se persiste una `family` asociable a su perfil.
- Dado un vehiculo, cuando se guarda, entonces queda asociado a una familia.
- Dado un mantenimiento, cuando se guarda, entonces queda asociado a un vehiculo existente.
- Dado un usuario autenticado, cuando consulta datos remotos, entonces solo accede a su `family_id`.

**Tests TDD previstos:**

- Test de insercion y lectura de vehiculo local.
- Test de relacion vehiculo-mantenimiento.
- Test de consulta de recordatorios por vehiculo.
- Test SQL/RLS manual para aislamiento por familia.

**Referencias:** `readme.md` secciones 3 y 4, `docs/user-stories.md`, `openspec/prd.md`.

**Historial:** creado para Entrega 1.

## T-02 - Auth y garaje familiar

**Tipo:** feature / auth.

**Descripcion:** implementar el flujo inicial de autenticacion con Google mediante Supabase Auth y creacion/carga del garaje familiar. En Android, Credential Manager con Google ID sera la opcion principal, con fallback controlado a Google Sign-In/OAuth si no esta disponible.

**Proposito:** permitir que el usuario entre en la app y tenga un espacio de datos aislado antes de registrar vehiculos.

**Historias relacionadas:** US-01.

**Prioridad:** Must-Have.

**Estimacion:** 5 story points.

**Responsable:** auth / backend gestionado / shared.

**Etiquetas:** `auth`, `supabase`, `onboarding`, `security`.

**Alcance:**

- Configurar login Google en Supabase Auth.
- Implementar login Android con Credential Manager y Google ID.
- Definir fallback a Google Sign-In/OAuth para dispositivos o entornos no compatibles.
- Crear o cargar `UserProfile` tras login.
- Crear `Family` si el usuario no tiene garaje.
- Exponer estado de sesion a la UI.

**Fuera de alcance:** login por email/password, multiples familias por usuario, roles avanzados.

**Criterios de aceptacion:**

- Dado un usuario sin sesion, cuando inicia sesion con Google, entonces accede autenticado.
- Dado un dispositivo compatible, cuando el usuario inicia sesion, entonces la app usa Credential Manager como flujo principal.
- Dado que Credential Manager no devuelve credencial valida o no esta disponible, cuando el usuario intenta iniciar sesion, entonces la app ofrece un fallback controlado sin bloquear el onboarding.
- Dado un usuario autenticado sin garaje, cuando completa onboarding, entonces se crea su garaje familiar.
- Dado un usuario autenticado con garaje, cuando abre la app, entonces se carga su garaje activo.

**Tests TDD previstos:**

- Test de creacion de perfil si no existe.
- Test de seleccion de flujo Credential Manager disponible.
- Test de fallback cuando no hay credencial disponible.
- Test de carga de garaje existente.
- Test de error de autenticacion propagado como estado de UI.

**Referencias:** US-01, contrato `signInWithGoogleAndLoadProfile` en `readme.md`.

**Historial:** creado para Entrega 1.

## T-03 - Crear vehiculo offline-first

**Tipo:** feature / dominio / repositorio.

**Descripcion:** implementar el caso de uso y repositorio para crear vehiculos en local, preparado para sincronizacion posterior.

**Proposito:** cubrir el primer paso funcional del garaje familiar: registrar un vehiculo y conservarlo aunque no haya conexion.

**Historias relacionadas:** US-02.

**Prioridad:** Must-Have.

**Estimacion:** 5 story points.

**Responsable:** dominio / datos.

**Etiquetas:** `domain`, `vehicle`, `offline-first`, `repository`.

**Alcance:**

- Definir entidad de dominio `Vehicle`.
- Crear `CreateVehicleUseCase` con validaciones.
- Añadir contrato `VehicleRepository`.
- Persistir vehiculo localmente con estado pendiente de sync.

**Fuera de alcance:** edicion de vehiculos, imagenes, eliminacion avanzada.

**Criterios de aceptacion:**

- Dado un garaje activo, cuando se crea un vehiculo valido, entonces queda guardado localmente.
- Dado kilometraje negativo, cuando se intenta crear, entonces se devuelve error de validacion.
- Dado modo offline, cuando se crea el vehiculo, entonces queda pendiente de sincronizacion.

**Tests TDD previstos:**

- Test de creacion valida.
- Test de error sin nombre.
- Test de error con kilometraje negativo.
- Test de marca `pending_sync` tras creacion offline.

**Referencias:** US-02, T-01.

**Historial:** creado para Entrega 1.

## T-04 - Registro e historial de mantenimiento

**Tipo:** feature / dominio / repositorio.

**Descripcion:** implementar el registro de mantenimientos o averias y la consulta de historial ordenado por vehiculo.

**Proposito:** aportar el valor principal de Carbura: conservar el historial fiable de cada vehiculo.

**Historias relacionadas:** US-04, US-05.

**Prioridad:** Must-Have.

**Estimacion:** 8 story points.

**Responsable:** dominio / datos.

**Etiquetas:** `maintenance`, `history`, `domain`, `offline-first`.

**Alcance:**

- Definir `MaintenanceRecord`.
- Crear `CreateMaintenanceRecordUseCase`.
- Validar vehiculo, tipo, fecha, kilometros y coste opcional.
- Persistir registros localmente.
- Consultar historial por vehiculo en orden descendente.

**Fuera de alcance:** adjuntos, OCR, recomendaciones automaticas.

**Criterios de aceptacion:**

- Dado un vehiculo existente, cuando se registra un mantenimiento valido, entonces queda guardado.
- Dado varios mantenimientos, cuando se consulta el historial, entonces aparecen ordenados por fecha descendente.
- Dado modo offline, cuando se registra un mantenimiento, entonces queda pendiente de sincronizacion.

**Tests TDD previstos:**

- Test de creacion valida de mantenimiento.
- Test de error si falta tipo.
- Test de error si los kilometros son invalidos.
- Test de historial ordenado por fecha descendente.

**Referencias:** US-04, US-05, T-01.

**Historial:** creado para Entrega 1.

## T-05 - Recordatorio automatico

**Tipo:** feature / dominio.

**Descripcion:** crear recordatorios automaticamente al registrar ITV o seguro con fecha de vencimiento.

**Proposito:** transformar el historial en prevencion, reduciendo olvidos de vencimientos importantes.

**Historias relacionadas:** US-06.

**Prioridad:** Must-Have.

**Estimacion:** 5 story points.

**Responsable:** dominio.

**Etiquetas:** `reminders`, `domain`, `maintenance`.

**Alcance:**

- Definir entidad `Reminder`.
- Crear logica de generacion desde registros ITV/seguro.
- Asociar recordatorio a vehiculo, tipo y registro origen.
- Usar 30 dias de antelacion por defecto.

**Fuera de alcance:** repeticion avanzada, reglas por kilometraje complejas, notificacion local.

**Criterios de aceptacion:**

- Dado un registro ITV con vencimiento, cuando se guarda, entonces se crea un recordatorio asociado.
- Dado un registro de seguro con vencimiento, cuando se guarda, entonces se crea un recordatorio asociado.
- Dado un recordatorio automatico, cuando se crea, entonces usa 30 dias de antelacion por defecto.

**Tests TDD previstos:**

- Test de recordatorio tras ITV.
- Test de recordatorio tras seguro.
- Test de no crear recordatorio si no hay vencimiento.
- Test de antelacion por defecto.

**Referencias:** US-06, T-04.

**Historial:** creado para Entrega 1.

## T-06 - Preparacion de sincronizacion

**Tipo:** tarea tecnica / sincronizacion.

**Descripcion:** preparar las entidades y repositorios para sincronizacion eventual entre local y Supabase con estrategia `last-write-wins`.

**Proposito:** mantener una experiencia offline-first sin bloquear el MVP con resolucion avanzada de conflictos.

**Historias relacionadas:** US-02, US-04.

**Prioridad:** Must-Have.

**Estimacion:** 8 story points.

**Responsable:** sincronizacion / datos.

**Etiquetas:** `sync`, `offline-first`, `supabase`, `repository`.

**Alcance:**

- Guardar timestamps y estado pendiente de sync.
- Preparar contrato `SyncManager`.
- Definir mapeos local/remoto para entidades del MVP.
- Aplicar `last-write-wins` usando `updated_at`.

**Fuera de alcance:** merge avanzado, resolucion manual de conflictos, sync en tiempo real.

**Criterios de aceptacion:**

- Dado un cambio offline, cuando se guarda, entonces queda marcado como pendiente.
- Dado un cambio pendiente, cuando hay conexion, entonces puede enviarse a Supabase.
- Dado conflicto simple, cuando se comparan timestamps, entonces gana el cambio con `updated_at` mas reciente.

**Tests TDD previstos:**

- Test de cambio marcado como pendiente.
- Test de seleccion de cambio mas reciente.
- Test de conservacion local ante error remoto.

**Referencias:** arquitectura offline-first en `readme.md`, contrato `syncGarageData`.

**Historial:** creado para Entrega 1.

## T-07 - Formulario alta de vehiculo

**Tipo:** feature / frontend.

**Descripcion:** crear la pantalla o dialogo para dar de alta un vehiculo desde la interfaz.

**Proposito:** permitir al usuario ejecutar visualmente el flujo de alta de vehiculo.

**Historias relacionadas:** US-02.

**Prioridad:** Must-Have.

**Estimacion:** 5 story points.

**Responsable:** frontend / shared presentation.

**Etiquetas:** `frontend`, `compose`, `vehicle`, `validation`.

**Alcance:**

- Crear estado de formulario.
- Mostrar campos nombre, tipo, matricula opcional y kilometros.
- Mostrar errores de validacion.
- Conectar con `CreateVehicleUseCase`.
- Refrescar lista del garaje tras guardar.

**Fuera de alcance:** edicion avanzada, imagenes, multiples garajes.

**Criterios de aceptacion:**

- Dado un formulario valido, cuando el usuario guarda, entonces se crea el vehiculo.
- Dado un formulario incompleto, cuando el usuario guarda, entonces se muestran errores.
- Dado guardado correcto, cuando vuelve al garaje, entonces aparece el vehiculo.

**Tests TDD previstos:**

- Test de validacion sin nombre.
- Test de validacion de kilometros negativos.
- Test de estado loading/success/error.

**Referencias:** US-02, T-03.

**Historial:** creado para Entrega 1.

## T-08 - Formulario mantenimiento e historial

**Tipo:** feature / frontend.

**Descripcion:** crear la interfaz para registrar mantenimiento y consultar historial por vehiculo.

**Proposito:** completar el flujo E2E de registro y consulta de historial.

**Historias relacionadas:** US-04, US-05.

**Prioridad:** Must-Have.

**Estimacion:** 8 story points.

**Responsable:** frontend / shared presentation.

**Etiquetas:** `frontend`, `compose`, `maintenance`, `history`.

**Alcance:**

- Crear formulario de mantenimiento.
- Mostrar tipo, fecha, kilometros, coste, taller y notas.
- Mostrar historial ordenado.
- Mostrar estado vacio si no hay registros.
- Conectar con casos de uso de mantenimiento.

**Fuera de alcance:** adjuntos, filtros avanzados, exportacion.

**Criterios de aceptacion:**

- Dado un vehiculo, cuando se registra mantenimiento valido, entonces aparece en historial.
- Dado un vehiculo sin historial, cuando se abre la pantalla, entonces aparece estado vacio.
- Dado coste informado, cuando se muestra el registro, entonces el coste aparece visible.

**Tests TDD previstos:**

- Test de validacion de formulario.
- Test de envio correcto al caso de uso.
- Test de estado vacio.
- Test de orden visual por fecha descendente.

**Referencias:** US-04, US-05, T-04.

**Historial:** creado para Entrega 1.

## T-09 - Lista de recordatorios

**Tipo:** feature / frontend.

**Descripcion:** crear pantalla de proximos recordatorios del garaje.

**Proposito:** permitir al usuario ver vencimientos proximos y vencidos en un lugar centralizado.

**Historias relacionadas:** US-07.

**Prioridad:** Should-Have.

**Estimacion:** 5 story points.

**Responsable:** frontend / shared presentation.

**Etiquetas:** `frontend`, `reminders`, `compose`.

**Alcance:**

- Listar recordatorios ordenados por proximidad.
- Mostrar vehiculo, tipo, fecha objetivo y estado.
- Diferenciar vencidos.
- Mostrar estado vacio.

**Fuera de alcance:** edicion de reglas, notificaciones, calendario avanzado.

**Criterios de aceptacion:**

- Dado recordatorios futuros, cuando se abre la pantalla, entonces aparecen ordenados por proximidad.
- Dado recordatorio vencido, cuando aparece, entonces se identifica visualmente.
- Dado que no hay recordatorios, cuando se abre la pantalla, entonces aparece estado vacio.

**Tests TDD previstos:**

- Test de orden por fecha.
- Test de estado vencido.
- Test de estado vacio.

**Referencias:** US-07, T-05.

**Historial:** creado para Entrega 1.

## T-10 - Notificaciones locales

**Tipo:** feature / plataforma.

**Descripcion:** implementar aviso local antes del vencimiento de un recordatorio.

**Proposito:** aportar valor aunque el usuario no abra la app a diario y sin depender del backend en el momento del aviso.

**Historias relacionadas:** US-08.

**Prioridad:** Should-Have.

**Estimacion:** 5 story points.

**Responsable:** plataforma / Android / Desktop.

**Etiquetas:** `notifications`, `android`, `desktop`, `reminders`.

**Alcance:**

- Programar notificacion local segun fecha de aviso.
- Gestionar permiso de notificaciones en Android.
- Navegar al contexto del recordatorio al abrir desde notificacion cuando sea viable.

**Fuera de alcance:** push notifications remotas, reglas complejas de repeticion, integracion calendario.

**Criterios de aceptacion:**

- Dado un recordatorio con antelacion, cuando llega la fecha de aviso, entonces se muestra notificacion local.
- Dado Android sin permiso, cuando se necesita avisar, entonces la app solicita o explica el permiso.
- Dado dispositivo offline, cuando llega el aviso, entonces no depende del backend.

**Tests TDD previstos:**

- Test de calculo de fecha de aviso.
- Test de solicitud de permiso requerida.
- Test manual/instrumentado de notificacion local.

**Referencias:** US-08, T-09.

**Historial:** creado para Entrega 1.
