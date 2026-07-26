# Carbura - Backlog de Tickets

Este documento detalla los tickets de trabajo derivados de las historias de usuario del MVP. Sigue la estructura recomendada en el material de AI4Devs para tickets Agile: titulo, descripcion, criterios de aceptacion, prioridad, estimacion, area responsable, etiquetas, referencias e historial.

Los tickets se trabajaron mediante SDD con OpenSpec. Los cambios sustanciales se documentaron con propuesta, tareas y delta de especificacion cuando aplicaba; TDD se uso de forma pragmatica para comportamientos estables y regresiones reproducibles.

## Alcance actual de la entrega final

- Incluido en Android y Desktop: autenticacion Google/Supabase, espacio personal, garaje persistente, historial, recordatorios, solicitud de eliminacion de cuenta y sync v0 bidireccional.
- Recordatorios MVP: crear, listar pendientes, marcar completados y borrar por familia/vehiculo, con fecha mediante date picker y/o kilometraje objetivo.
- Mantenimiento MVP: crear y editar con date picker, listar el historial persistente por vehiculo y borrar registros con confirmacion.
- Garaje MVP: crear, editar, listar y borrar vehiculos, actualizar rapidamente el odometro y gestionar objetivos opcionales de ITV, seguro y revision por kilometraje.
- Sync v0: subida/bajada de vehiculos, mantenimientos y recordatorios; tombstones; `pending_sync`; `last-write-wins`; sync inicial al restaurar sesion; accion manual desde Usuario.
- Incorporado despues de Entrega 2: edicion de vehiculos, odometro rapido, sugerencias proactivas de recordatorios desde el vehiculo y CI con calidad, tests y APK debug.
- Implementado despues de Entrega 2: Desktop local y autenticado, OAuth PKCE, vault nativo, importacion consentida, recordatorios desde mantenimiento, hardening RLS y confirmaciones de sync condicionadas por version.
- Fuera del MVP entregado: coste acumulado, invitaciones, exportacion PDF/CSV, iOS y validacion de paquetes para Windows/Linux.
- Fuente de sincronizacion: `openspec/specs/sync-v0/spec.md` y `readme.md` secciones 2.1, 2.4 y 2.6.

## Estado actual

- Android MVP local-first + sync v0: completado para la demo de Entrega 2 y ampliado con edicion y recordatorios proactivos.
- Android y Desktop son clientes funcionales. Android es la unica plataforma con notificaciones nativas; iOS queda fuera de alcance.
- OpenSpec archivados relevantes: `add-sync-v0`, `add-reminders-mvp-edge-to-edge`, `add-user-family-mvp`, `add-date-pickers-delete-mvp`, `add-local-reminder-notifications`, `harden-sync-offline`.
- Cambios recientes archivados: `add-vehicle-editing-odometer`, `add-proactive-vehicle-reminders` y `edit-maintenance-records`.

## Sync v0 implementado

- Fuente de alcance: `openspec/specs/sync-v0/spec.md`.
- Es funcional end-to-end, no solo preparatorio: sube cambios locales pendientes a Supabase y baja datos remotos de la familia.
- Alcance v0: vehiculos, mantenimientos y recordatorios; ejecucion manual, tras login/restauracion y durante el uso de app; resolucion simple por `updated_at` con `last-write-wins`.
- Fuera de v0: realtime, sincronizacion periodica o en background con el proceso de la aplicacion cerrado, merge manual de conflictos, colas complejas, adjuntos y notificaciones remotas.
- La implementacion debe vivir en KMP/shared (`core:domain`/`core:data`) para ser reutilizable por Android y Desktop.

## Orden de implementacion recomendado

| Orden | Ticket | Area | Historias | Prioridad | Estimacion |
|---|---|---|---|---|---|
| 1 | T-01 - Esquema local/remoto del MVP | Datos | US-01, US-02, US-04, US-06 | Must | 8 SP |
| 2 | T-02 - Auth y garaje familiar | Auth / backend gestionado | US-01 | Must | 5 SP |
| 3 | T-03 - Crear vehiculo offline-first | Dominio / repositorio | US-02 | Must | 5 SP |
| 4 | T-04 - Registro e historial de mantenimiento | Dominio / repositorio | US-04, US-05 | Must | 8 SP |
| 5 | T-05 - Recordatorio automatico | Dominio | US-06 | Must | 5 SP |
| 6 | T-06 - Preparacion de sincronizacion | Sincronizacion | US-02, US-04, US-07 | Must | 8 SP |
| 7 | T-07 - Formulario alta de vehiculo | Frontend | US-02 | Must | 5 SP |
| 8 | T-08 - Formulario mantenimiento e historial | Frontend | US-04, US-05 | Must | 8 SP |
| 9 | T-09 - Lista de recordatorios | Frontend | US-07 | Should | 5 SP |
| 10 | T-10 - Notificaciones locales | Plataforma | US-08 | Should | 5 SP |
| 11 | T-11 - CI, empaquetado y evidencia de despliegue | Infraestructura | Transversal | Must | 5 SP |
| 12 | T-12 - Test E2E del flujo principal | Calidad / tests | US-02, US-04, US-05, US-06 | Must | 5 SP |

> T-11 y T-12 cubren artefactos obligatorios de la entrega final. T-11 se cerró con CI y los paquetes Android/macOS entregados por el canal académico; T-12 se cerró con un E2E Android de aplicación ejecutado en emulador.

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

- Dado un usuario autenticado sin perfil, cuando accede por primera vez, entonces se persisten automaticamente la `Family` tecnica y el `UserProfile` asociados.
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

**Descripcion:** implementar autenticacion Google/Supabase y creacion o carga automatica del espacio personal. Android usa Credential Manager con Google ID y Desktop Authorization Code con PKCE S256, navegador del sistema y vault nativo.

**Proposito:** permitir que el usuario entre en la app y tenga un espacio de datos aislado antes de registrar vehiculos.

**Historias relacionadas:** US-01.

**Prioridad:** Must-Have.

**Estimacion:** 5 story points.

**Responsable:** auth / backend gestionado / shared.

**Etiquetas:** `auth`, `supabase`, `onboarding`, `security`.

**Alcance:**

- Configurar login Google en Supabase Auth.
- Implementar login Android con Credential Manager y Google ID.
- Permitir reintentar errores Android y restaurar/refrescar de forma segura la sesion Desktop.
- Mantener tokens Desktop solo en Keychain o Windows Credential Manager.
- Crear o cargar `UserProfile` tras login.
- Crear la `Family` tecnica si el usuario no tiene espacio personal.
- Exponer estado de sesion a la UI.

**Fuera de alcance:** login por email/password, multiples familias por usuario, roles avanzados.

**Criterios de aceptacion:**

- Dado un usuario sin sesion, cuando inicia sesion con Google, entonces accede autenticado.
- Dado un dispositivo compatible, cuando el usuario inicia sesion, entonces la app usa Credential Manager como flujo principal.
- Dado que Credential Manager no devuelve una credencial valida, cuando el usuario intenta iniciar sesion, entonces la app muestra un error comprensible y permite reintentar.
- Dado un usuario autenticado sin perfil, cuando entra por primera vez, entonces se crean automaticamente su perfil y espacio personal.
- Dado un usuario autenticado con espacio personal, cuando abre la app, entonces se carga como espacio activo.

**Tests TDD previstos:**

- Test de creacion de perfil si no existe.
- Test de seleccion de flujo Credential Manager disponible.
- Test de error y reintento cuando no hay credencial disponible.
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

**Descripcion:** implementar el registro, edicion y borrado de mantenimientos o averias y la consulta de historial ordenado por vehiculo.

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
- Editar registros activos conservando sus relaciones y recordatorios asociados.
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

**Descripcion:** crear recordatorios deterministas desde la proxima fecha de una ITV o seguro y ofrecer un recordatorio opcional al guardar un mantenimiento con fecha futura.

**Proposito:** transformar el historial en prevencion, reduciendo olvidos de vencimientos importantes.

**Historias relacionadas:** US-06.

**Prioridad:** Must-Have.

**Estimacion:** 5 story points.

**Responsable:** dominio.

**Etiquetas:** `reminders`, `domain`, `maintenance`.

**Alcance:**

- Definir entidad `Reminder`.
- Crear logica de generacion desde la proxima fecha de registros ITV/seguro.
- Ofrecer la decision de guardar con o sin recordatorio para mantenimientos registrados con fecha futura.
- Asociar recordatorio a vehiculo, tipo y registro origen.

**Fuera de alcance:** repeticion avanzada, reglas por kilometraje complejas, notificacion local.

**Criterios de aceptacion:**

- Dado un registro ITV con vencimiento, cuando se guarda, entonces se crea un recordatorio asociado.
- Dado un registro de seguro con vencimiento, cuando se guarda, entonces se crea un recordatorio asociado.
- Dado un mantenimiento con fecha futura, cuando se guarda, entonces el usuario puede conservar solo el registro o crear tambien el recordatorio.

**Tests TDD previstos:**

- Test de recordatorio tras ITV.
- Test de recordatorio tras seguro.
- Test de no crear recordatorio si no hay vencimiento.
- Test de ID estable y ausencia de duplicados.

**Referencias:** US-06, T-04.

**Historial:** creado para Entrega 1.

## T-06 - Preparacion de sincronizacion

**Tipo:** tarea tecnica / sincronizacion.

**Descripcion:** preparar las entidades y repositorios para sincronizacion eventual entre local y Supabase con estrategia `last-write-wins`.

**Proposito:** mantener una experiencia offline-first sin bloquear el MVP con resolucion avanzada de conflictos.

**Historias relacionadas:** US-02, US-04, US-07.

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

**Estado actual:** parcial. El listado ordenado y el estado vacio estan disponibles; la diferenciacion visual de vencidos queda como refinamiento.

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
- Mostrar estado vacio.

**Fuera de alcance actual:** diferenciacion visual de vencidos, edicion de reglas, notificaciones y calendario avanzado.

**Criterios de aceptacion:**

- Dado recordatorios futuros, cuando se abre la pantalla, entonces aparecen ordenados por proximidad.
- Dado que no hay recordatorios, cuando se abre la pantalla, entonces aparece estado vacio.

**Tests TDD previstos:**

- Test de orden por fecha.
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

**Responsable:** plataforma / Android.

**Etiquetas:** `notifications`, `android`, `reminders`.

**Alcance:**

- Programar notificacion local segun fecha de aviso.
- Gestionar permiso de notificaciones en Android.
- Navegar al contexto del recordatorio al abrir desde notificacion cuando sea viable.

**Fuera de alcance:** push notifications remotas, reglas complejas de repeticion, integracion calendario.

**Criterios de aceptacion:**

- Dado un recordatorio con antelacion, cuando llega la fecha de aviso, entonces se muestra notificacion local.
- Dado Android sin permiso, cuando se necesita avisar, entonces la app solicita o explica el permiso.
- Dado dispositivo offline, cuando llega el aviso, entonces no depende del backend.
- Dado un recordatorio creado en Desktop, cuando Android sincroniza, entonces Android lo incorpora al outbox y programa el aviso.

**Tests TDD previstos:**

- Test de calculo de fecha de aviso.
- Test de solicitud de permiso requerida.
- Test manual/instrumentado de notificacion local.

**Referencias:** US-08, T-09.

**Historial:** creado para Entrega 1.

## T-11 - CI, empaquetado y evidencia de despliegue

**Tipo:** tarea tecnica / infraestructura.

**Descripcion:** configurar un pipeline de CI con GitHub Actions y preparar el empaquetado manual de la entrega final. Al ser Carbura una app KMP nativa sin URL publica, la evidencia se basa en artefactos instalables, el backend Supabase desplegado y un video demo del flujo principal.

**Proposito:** disponer de verificacion continua, mantener la configuracion sensible fuera del repositorio y preparar un sistema instalable que pueda probarse directamente.

**Historias relacionadas:** transversal (soporta todas las Must-Have).

**Prioridad:** Must-Have (entrega final).

**Estimacion:** 5 story points.

**Responsable:** infraestructura.

**Etiquetas:** `ci`, `packaging`, `github-actions`, `secrets`, `delivery`.

**Alcance:**

- Workflow de GitHub Actions que compila y ejecuta `./gradlew test` en cada push y PR.
- Quality gate actual con `./gradlew qualityCheck test assembleDebug --stacktrace`.
- Ejecucion de CI sin secretos de produccion y exclusion de la configuracion local sensible del repositorio.
- Release académica con APK Android y DMG macOS generados e instalados en sus sistemas objetivo. MSI queda fuera del alcance validado por no disponer de un PC Windows.
- Evidencia de despliegue: backend Supabase activo, instrucciones de instalacion y video demo de 2-3 minutos del flujo E2E entregado mediante el canal academico externo.

**Fuera de alcance:** publicacion en Google Play, firma de produccion, despliegue continuo a stores, infraestructura propia de servidor.

**Criterios de aceptacion:**

- Dado un push a una rama con PR, cuando se ejecuta el pipeline, entonces compila el proyecto y ejecuta la suite de tests.
- Dado un fallo de tests, cuando se ejecuta el pipeline, entonces la PR queda marcada en rojo.
- Dada la version final, cuando se prepara el paquete academico, entonces incluye la APK Android y el DMG comprobados en sus sistemas objetivo.
- Dado el repositorio publico, cuando se inspecciona, entonces no contiene secretos ni credenciales.

**Tests TDD previstos:**

- Verificacion del pipeline en verde sobre un cambio trivial.
- Verificacion de fallo del pipeline ante un test roto (prueba controlada).
- Checklist manual: instalacion de la APK en Android y del DMG en macOS; MSI/Credential Manager queda registrado como no validado por falta de PC Windows.

**Referencias:** instrucciones del proyecto final (artefactos de infra y despliegue), `readme.md` seccion 2.4.

**Historial:** creado tras auditoria pre-Entrega 1.

## Cierre tecnico de Desktop y seguridad

- **Desktop Compose:** Garaje, Mantenimiento, Recordatorios y Cuenta son interactivos en modo local y autenticado.
- **OAuth y vault:** callback exacto `127.0.0.1`, PKCE S256, Keychain/Credential Manager y ausencia de fallback en texto plano.
- **Consentimiento local:** importar, excluir o cancelar datos `local-family` antes del primer sync.
- **Sync seguro:** aislamiento por familia, tombstones, LWW y acknowledgement por `updatedAt` para no perder mutaciones concurrentes.
- **Cuenta:** cierre de sesion local y solicitud de eliminacion permanente single-flight con limpieza local convergente.
- **Backend:** ocho migraciones, RPC de eliminacion y hardening de familias/perfiles.
- **Release entregada:** APK debug, DMG y vídeo enviados mediante el canal académico externo; los instalables finales se generaron y verificaron en Android y macOS. La aceptación manual cubrió el flujo multidispositivo principal y la cobertura RLS hostil quedó automatizada; la eliminación con cuenta desechable y los recorridos completos de accesibilidad no se ejecutaron. MSI/Windows y firma Developer ID/notarización quedan fuera del alcance validado por falta de host y credenciales.

## T-12 - Test E2E del flujo principal

**Tipo:** tarea tecnica / calidad.

**Descripcion:** implementar un test automatizado dentro del proceso que recorra el flujo principal del MVP: sesion restaurada de test -> alta de vehiculo -> registro de mantenimiento ITV futuro -> aceptacion explicita del recordatorio -> consulta de historial y recordatorios.

**Proposito:** cumplir el requisito de la entrega final ("al menos un test E2E del flujo principal") y proteger el flujo de valor completo contra regresiones.

**Historias relacionadas:** US-02, US-04, US-05, US-06. La sesion restaurada es una frontera determinista y no cubre los criterios de autenticacion de US-01.

**Prioridad:** Must-Have (entrega final).

**Estimacion:** 5 story points.

**Responsable:** calidad / frontend / shared.

**Etiquetas:** `e2e`, `compose-ui-test`, `testing`, `quality`.

**Alcance:**

- Test instrumentado de Compose UI en Android (o test de UI Desktop si resulta mas estable en CI) que recorre el flujo E2E completo.
- Doble o bypass del login Google para entorno de test (sesion fake o usuario de test).
- Datos sobre base local SQLDelight en memoria o limpia por ejecucion.
- Asercion final: el recordatorio aceptado para el mantenimiento ITV futuro es visible en la pantalla de recordatorios.
- Integracion del test E2E en el pipeline de CI cuando sea viable (o documentado como paso de verificacion local si el emulador en CI resulta inestable).

**Fuera de alcance:** suites E2E exhaustivas por pantalla, tests E2E de sincronizacion multi-dispositivo, tests de notificaciones locales.

**Criterios de aceptacion:**

- Dado un entorno limpio, cuando se ejecuta el test E2E, entonces completa el flujo alta de vehiculo -> mantenimiento -> historial -> recordatorio sin intervencion manual.
- Dado el registro de una ITV futura y la aceptacion del recordatorio, cuando finaliza el flujo, entonces el test verifica que aparece en la UI.
- Dado un fallo en cualquier paso del flujo, cuando se ejecuta el test, entonces falla con un mensaje diagnosticable.

**Tests TDD previstos:**

- El propio test E2E (se escribe al final, cuando el flujo core esta implementado; los pasos intermedios ya estan cubiertos por tests unitarios y de integracion de T-01 a T-08).

**Referencias:** instrucciones del proyecto final (suite de tests), flujo E2E prioritario en `docs/user-stories.md`, `readme.md` seccion 2.6.

**Historial:** creado tras auditoria pre-Entrega 1.

**Resultado final:** implementado en `MainActivityE2ETest`. El test lanza la actividad real con una sesión restaurada determinista, crea un vehículo desde UI, registra un mantenimiento ITV futuro, confirma el recordatorio y verifica el historial y el recordatorio renderizados. Navegación, ViewModels, casos de uso, repositorios y SQLDelight son los de producción; Google, sync remoto y entrega de notificaciones se sustituyen en el límite externo.
