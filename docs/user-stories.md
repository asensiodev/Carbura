# Carbura - Historias de usuario del MVP

Este documento deriva las historias de usuario principales desde el PRD (`openspec/prd.md`). El objetivo es mantener el MVP acotado y trazable antes de convertir estas historias en specs OpenSpec, tickets tecnicos y tests TDD.

## Criterio de priorizacion

| Prioridad | Significado |
|---|---|
| Must-Have | Imprescindible para el flujo E2E prioritario de la entrega. |
| Should-Have | Importante, pero opcional si el tiempo obliga a recortar alcance. |
| Could-Have | Mejora de MVP extendido o post-MVP. |

## Epicas MVP

| Epica | Descripcion | Prioridad |
|---|---|---|
| E01 - Onboarding y espacio personal | Acceso con Google y creacion automatica del espacio personal. | Must-Have |
| E02 - Gestion de vehiculos | Alta y consulta de vehiculos del garaje. | Must-Have |
| E03 - Mantenimientos e historial | Registro de eventos, costes, kilometros e historial. | Must-Have |
| E04 - Recordatorios | Recordatorios por fecha/km y avisos locales. | Must-Have |
| E05 - Offline-first y sincronizacion | Uso sin conexion y sincronizacion eventual. | Should-Have |
| E06 - Colaboracion familiar | Invitaciones para compartir el garaje. | Could-Have |
| E07 - Exportacion | Exportar historial de vehiculo. | Could-Have |

---

## Flujo E2E prioritario de la entrega

El flujo E2E prioritario que crea valor completo para la entrega es:

```text
Usuario inicia sesion
  -> dispone automaticamente de su espacio personal
  -> anade un vehiculo
  -> registra una ITV o mantenimiento
  -> consulta el historial del vehiculo
  -> crea un recordatorio o acepta una sugerencia proactiva del vehiculo
```

El flujo esta implementado en Android y Desktop con persistencia local y sincronizacion v0. Android entrega las notificaciones locales; Desktop conserva y sincroniza los recordatorios sin programar avisos nativos. iOS, invitaciones y exportacion quedan fuera de la entrega.

## Must-Have - Entrega E2E

### US-01 - Iniciar sesion y disponer de un espacio personal

**Como** usuario nuevo,
**quiero** iniciar sesion con Google y disponer de un espacio personal,
**para** empezar a registrar mis vehiculos.

**Trazabilidad:** UC-01, PRD objetivo 1.

**Criterios de aceptacion:**

- Dado un usuario sin sesion, cuando inicia sesion con Google correctamente, entonces accede a la app como usuario autenticado.
- Dado un usuario autenticado sin perfil, cuando entra por primera vez, entonces la app crea automaticamente su perfil y espacio personal.
- Dado un espacio personal existente, cuando el usuario accede a la app, entonces lo recupera como espacio activo.
- Dado un error de autenticacion, cuando el usuario intenta iniciar sesion, entonces la app muestra un error comprensible y permite reintentar.

### US-02 - Anadir vehiculo al garaje

**Como** usuario autenticado,
**quiero** anadir un vehiculo a mi garaje,
**para** poder registrar su mantenimiento y consultar su estado.

**Trazabilidad:** UC-02, PRD objetivo 1.

**Criterios de aceptacion:**

- Dado un garaje activo, cuando el usuario introduce los datos minimos de un vehiculo, entonces el vehiculo queda guardado en el garaje.
- Dado un vehiculo guardado, cuando el usuario vuelve a la pantalla principal, entonces el vehiculo aparece en la lista del garaje.
- Dado un formulario incompleto, cuando el usuario intenta guardar, entonces la app indica los campos obligatorios.
- Dado que el usuario esta offline, cuando guarda un vehiculo, entonces queda disponible localmente y pendiente de sincronizacion.
- Dado Android o Desktop, cuando crea, edita o elimina un vehiculo, entonces el cambio converge en el otro cliente tras sincronizar.

**Datos minimos del MVP:** nombre, tipo, matricula opcional, kilometros actuales.

### US-04 - Registrar mantenimiento o averia

**Como** usuario autenticado,
**quiero** registrar un mantenimiento o averia de un vehiculo,
**para** mantener un historial fiable de lo que se le ha hecho.

**Trazabilidad:** UC-03, PRD objetivos 1 y 4.

**Criterios de aceptacion:**

- Dado un vehiculo existente, cuando el usuario registra un mantenimiento con tipo, fecha y kilometros, entonces queda guardado en el historial del vehiculo.
- Dado un mantenimiento con coste informado, cuando se guarda, entonces el coste queda asociado al registro.
- Dado un formulario incompleto, cuando el usuario intenta guardar, entonces la app muestra validaciones claras.
- Dado que el usuario esta offline, cuando registra un mantenimiento, entonces queda disponible localmente y pendiente de sincronizacion.
- Dado Android o Desktop, cuando crea, edita o elimina un mantenimiento, entonces el historial converge en ambos clientes.

**Tipos iniciales del MVP:** ITV, aceite, neumaticos, seguro, revision general, averia, personalizado.

### US-05 - Consultar historial de mantenimiento

**Estado actual:** parcial. El historial y los costes individuales estan disponibles; el coste acumulado por vehiculo queda fuera del MVP entregado.

**Como** usuario de la cuenta,
**quiero** consultar el historial de mantenimiento de un vehiculo,
**para** saber que se hizo, cuando, con cuantos kilometros y cuanto costo.

**Trazabilidad:** UC-04, PRD objetivo 4.

**Criterios de aceptacion:**

- Dado un vehiculo con mantenimientos, cuando el usuario abre su historial, entonces ve los registros ordenados por fecha descendente.
- Dado un registro de mantenimiento, cuando aparece en el historial, entonces muestra al menos tipo, fecha, kilometros y coste si existe.
- Dado un vehiculo sin historial, cuando el usuario abre la seccion, entonces ve un estado vacio con opcion de registrar mantenimiento.
- Pendiente: presentar el coste acumulado del vehiculo cuando existen varios registros con coste.

### US-06 - Generar recordatorios desde un mantenimiento

**Estado actual:** implementada mediante dos recorridos. Una proxima fecha de ITV o seguro genera un recordatorio determinista; registrar un mantenimiento en una fecha futura ofrece la decision explicita de guardarlo con o sin recordatorio.

**Como** propietario del vehiculo,
**quiero** convertir las fechas futuras de un mantenimiento en recordatorios,
**para** no olvidar el siguiente vencimiento.

**Trazabilidad:** UC-03, UC-05, PRD objetivos 2 y 3.

**Criterios de aceptacion:**

- Dado un registro de ITV o seguro con proxima fecha, cuando se guarda, entonces la app crea un recordatorio determinista asociado.
- Dado un mantenimiento registrado con fecha futura, cuando se guarda, entonces la app permite elegir entre conservar solo el registro o crear tambien un recordatorio.
- Dado que el usuario elige guardar sin recordatorio, cuando finaliza el registro, entonces no se crea ningun aviso opcional.
- Dado un recordatorio automatico o confirmado, cuando se guarda, entonces usa un ID estable y no se duplica al reintentar.
- Dado un vehiculo con recordatorio creado, cuando el usuario abre la pantalla de recordatorios, entonces el recordatorio aparece visible.

---

## Should-Have - Implementados total o parcialmente

### US-07 - Ver proximos recordatorios

**Estado actual:** parcial. La lista, el estado vacio y la convergencia Android/Desktop estan implementados; mostrar la antelacion y diferenciar visualmente los vencidos queda como refinamiento.

**Como** usuario de la cuenta,
**quiero** ver los proximos recordatorios del garaje,
**para** saber que mantenimientos o vencimientos requieren atencion.

**Trazabilidad:** UC-05, PRD objetivos 2 y 3.

**Criterios de aceptacion:**

- Dado un garaje con recordatorios futuros, cuando el usuario abre la pantalla de recordatorios, entonces ve los recordatorios ordenados por proximidad.
- Dado un recordatorio, cuando se muestra, entonces incluye vehiculo, titulo y fecha y/o kilometraje objetivo.
- Dado que no hay recordatorios, cuando el usuario abre la pantalla, entonces ve un estado vacio comprensible.
- Dado un recordatorio creado, completado o eliminado en Desktop, cuando Android sincroniza, entonces refleja el mismo estado.

### US-08 - Recibir notificacion local de un recordatorio

**Como** usuario,
**quiero** recibir una notificacion local de un vencimiento,
**para** actuar con tiempo aunque no abra la app a diario.

**Trazabilidad:** UC-05, PRD objetivos 2 y 3.

**Criterios de aceptacion:**

- Dado un recordatorio con fecha, cuando llega un punto de aviso definido por su politica local, entonces Android programa o muestra una notificacion.
- Dado que el usuario pulsa la notificacion, cuando la app se abre, entonces navega al contexto del vehiculo o recordatorio.
- Dado que las notificaciones no tienen permiso en Android, cuando la app necesita avisar, entonces solicita o explica el permiso necesario.
- Dado que el dispositivo esta offline, cuando llega el momento del aviso, entonces la notificacion local puede mostrarse sin depender del backend.
- Dado un recordatorio sincronizado desde Desktop, cuando Android lo descarga, entonces entra en el outbox de notificaciones y Android programa el aviso.
- Dado Desktop, cuando muestra un recordatorio, entonces informa de que las alertas nativas se entregan solo desde Android.

---

## Could-Have - Backlog MVP extendido / Entrega final

### US-03 - Ver listado y detalle de vehiculos

> **Nota de alcance:** la version minima de esta historia (lista basica del garaje y acceso al detalle) queda absorbida por el flujo E2E Must-Have y se implementa dentro de los tickets T-07 y T-08, ya que sin lista de vehiculos no se puede completar el flujo principal. Lo que permanece como Could-Have es el refinamiento: estado resumido por vehiculo, proximos avisos en la tarjeta y detalle enriquecido.

**Estado actual:** la lista, el detalle, la edicion y el borrado logico estan integrados en Android y Desktop; permanecen como evolucion los refinamientos visuales adicionales.

**Como** usuario de la cuenta,
**quiero** ver los vehiculos del garaje y abrir el detalle de cada uno,
**para** consultar rapidamente su informacion y estado de mantenimiento.

**Trazabilidad:** UC-02, UC-04, PRD objetivos 1 y 4.

**Criterios de aceptacion:**

- Dado un garaje con vehiculos, cuando el usuario abre la app, entonces ve una lista de vehiculos disponibles.
- Dado un vehiculo de la lista, cuando el usuario lo selecciona, entonces ve su detalle.
- Dado un vehiculo sin mantenimientos, cuando el usuario abre su detalle, entonces ve un estado vacio claro.
- Dado que no hay vehiculos, cuando el usuario abre el garaje, entonces la app muestra una accion principal para anadir el primer vehiculo.

### US-09 - Actualizar odometro rapidamente

**Estado actual:** implementada en Android y Desktop, incluida la confirmacion cuando el valor desciende y su persistencia local-first.

**Como** usuario de la cuenta,
**quiero** actualizar rapidamente los kilometros actuales de un vehiculo,
**para** mantener los recordatorios por kilometraje y el historial al dia.

**Trazabilidad:** UC-06, PRD scope MVP.

**Criterios de aceptacion:**

- Dado un vehiculo existente, cuando el usuario actualiza el odometro, entonces el nuevo valor queda guardado.
- Dado un valor inferior al kilometraje actual, cuando el usuario intenta guardarlo, entonces la app pide confirmacion o muestra una validacion.
- Dado que el usuario esta offline, cuando actualiza el odometro, entonces el cambio queda disponible localmente y pendiente de sincronizacion.

### US-10 - Sincronizar datos entre dispositivos

**Estado actual:** sync v0 implementada entre Android y Desktop. Usa full pull, push de pendientes, tombstones, confirmacion condicionada por version y `last-write-wins` mientras cada app esta activa.

**Como** usuario de la misma cuenta en varios dispositivos,
**quiero** que los datos del garaje se sincronicen entre mis dispositivos,
**para** ver los mismos vehiculos, mantenimientos y recordatorios en Android y Desktop.

**Trazabilidad:** UC-09, PRD objetivo 5.

**Criterios de aceptacion:**

- Dado un cambio realizado en un dispositivo con conexion, cuando otro dispositivo sincroniza, entonces el cambio aparece en el segundo dispositivo.
- Dado un cambio realizado offline, cuando el dispositivo recupera conexion y la aplicacion esta activa o el usuario solicita sincronizar, entonces el cambio se sincroniza con el backend.
- Dado dos cambios sobre el mismo dato, cuando se sincronizan, entonces se aplica la estrategia last-write-wins definida en arquitectura.
- Dado un error de sincronizacion, cuando ocurre, entonces la app conserva los datos locales y reintenta posteriormente.

### US-13 - Obtener sugerencias proactivas desde el vehiculo

**Como** propietario del vehiculo,
**quiero** convertir la proxima ITV, seguro o revision por kilometraje en recordatorios,
**para** anticiparme sin introducir los mismos datos dos veces.

**Estado actual:** implementada en Android y Desktop con confirmacion explicita e IDs estables.

**Criterios de aceptacion:**

- Dado un objetivo futuro, cuando el usuario acepta la sugerencia, entonces se crea el recordatorio correspondiente.
- Dado que el usuario rechaza la sugerencia, cuando guarda, entonces el vehiculo se conserva sin crear el aviso.
- Dado un reintento o una edicion equivalente, cuando se reconcilia, entonces no se crean duplicados.

### US-14 - Usar y proteger la cuenta Desktop

**Como** usuario Desktop,
**quiero** elegir entre modo local y cuenta sincronizada,
**para** controlar mis datos sin exponer credenciales ni subir registros sin consentimiento.

**Estado actual:** implementada para OAuth PKCE, Keychain/Credential Manager, importacion/exclusion de `local-family`, cierre local y solicitud de eliminacion permanente con limpieza local convergente.

**Criterios de aceptacion:**

- Sin configuracion Supabase, Desktop permite usar Garaje, Mantenimiento, Recordatorios y Cuenta en local.
- Antes del primer sync, los datos heredados solo se importan con consentimiento ligado a un snapshot.
- Cerrar sesion elimina la credencial Desktop sin borrar la cache familiar ni cerrar Android.
- Eliminar la cuenta exige confirmacion irreversible, limpia la cache familiar y converge a modo local aunque la respuesta remota sea incierta.

### US-11 - Invitar a un familiar al garaje

**Estado actual:** evolucion futura; no implementada ni incluida en la version `1.0.0`.

**Como** propietario del garaje,
**quiero** generar un codigo de invitacion,
**para** que un familiar pueda unirse al mismo garaje desde su dispositivo.

**Trazabilidad:** UC-07, PRD objetivo 6.

**Criterios de aceptacion:**

- Dado un garaje existente, cuando el propietario genera una invitacion, entonces la app muestra un codigo de 6 caracteres.
- Dado un familiar autenticado, cuando introduce un codigo valido, entonces queda asociado al garaje familiar.
- Dado un codigo invalido o caducado, cuando el familiar intenta unirse, entonces la app muestra un error claro.
- Dado un familiar unido al garaje, cuando accede a la app, entonces ve los mismos vehiculos y recordatorios.

---

### US-12 - Exportar historial de un vehiculo

**Estado actual:** evolucion futura; no implementada ni incluida en la version `1.0.0`.

**Como** usuario,
**quiero** exportar el historial de un vehiculo a PDF o CSV,
**para** compartirlo, archivarlo o usarlo al vender el vehiculo.

**Trazabilidad:** UC-08, PRD scope MVP.

**Criterios de aceptacion:**

- Dado un vehiculo con historial, cuando el usuario solicita exportar, entonces puede elegir PDF o CSV.
- Dado una exportacion generada, cuando se abre el archivo, entonces contiene los eventos con fecha, kilometros, tipo y coste.
- Dado un vehiculo sin historial, cuando el usuario intenta exportar, entonces la app informa de que no hay datos exportables.
- Dado un error al generar el archivo, cuando ocurre, entonces la app muestra un mensaje claro sin perder datos.

---

## Historias seleccionadas para documentacion final

Para la entrega academica, las 3 historias principales recomendadas son:

| Historia | Motivo |
|---|---|
| US-02 - Anadir vehiculo al garaje | Representa el core de gestion del dominio. |
| US-04 - Registrar mantenimiento o averia | Demuestra el valor principal del producto. |
| US-06 - Generar recordatorio automatico tras registrar ITV o seguro | Conecta historial con prevencion, que es el principal diferenciador del MVP. |

## Backlog inicial de tickets

Los tickets se derivaron de las historias Must-Have y Should-Have. Los cambios sustanciales se documentaron mediante OpenSpec y se cubrieron con tests cuando el comportamiento permitia una comprobacion estable. El detalle completo vive en `docs/backlog.md`.

| Ticket | Area | Historias | Prioridad | Estimacion | Criterio principal de aceptacion |
|---|---|---|---|---|---|
| T-01 - Esquema local/remoto | Datos | US-01, US-02, US-04, US-06 | Must | 8 SP | Datos quedan relacionados por familia, vehiculo, mantenimiento y recordatorio. |
| T-02 - Auth y garaje familiar | Auth / onboarding | US-01 | Must | Usuario autenticado obtiene o recupera automaticamente su espacio personal. |
| T-03 - Crear vehiculo offline-first | Dominio / datos | US-02 | Must | 5 SP | Vehiculo valido queda guardado localmente y pendiente de sync si no hay conexion. |
| T-04 - Registro e historial | Dominio / datos | US-04, US-05 | Must | 8 SP | Historial muestra mantenimientos ordenados por fecha descendente. |
| T-05 - Recordatorio automatico | Dominio | US-06 | Must | 5 SP | Las fechas futuras generan u ofrecen recordatorios deterministas sin duplicados. |
| T-06 - Preparacion de sync | Sincronizacion | US-02, US-04, US-07 | Must | 8 SP | Cambios offline conservan estado pendiente y timestamps de sincronizacion. |
| T-07 - Formulario alta vehiculo | Frontend | US-02 | Must | 5 SP | Usuario puede introducir datos minimos y ver validaciones. |
| T-08 - Formulario mantenimiento e historial | Frontend | US-04, US-05 | Must | 8 SP | Usuario puede registrar mantenimiento y consultar historial. |
| T-09 - Lista de recordatorios | Frontend | US-07 | Should | 5 SP | Recordatorios se muestran ordenados por proximidad con estado vacio. |
| T-10 - Notificaciones locales | Plataforma | US-08 | Should | 5 SP | Aviso local se muestra sin depender del backend cuando llega la fecha. |
| T-11 - CI, empaquetado y evidencia de despliegue | Infraestructura | Transversal | Must (final) | 5 SP | Pipeline CI con tests en verde, secretos fuera del repo y artefactos instalables entregados. |
| T-12 - Test E2E del flujo principal | Calidad | US-02, US-04, US-05, US-06 | Must (final) | 5 SP | `MainActivityE2ETest` recorre el flujo dentro del proceso con autenticacion, sync remoto y notificaciones sustituidos en sus fronteras. |

## Siguiente paso

Las historias Must-Have, la sincronizacion Android/Desktop, US-06, los recordatorios proactivos y la cuenta Desktop estan implementados. El coste acumulado de US-05 queda fuera del MVP; iOS y la validacion de paquetes Windows/Linux tampoco forman parte del alcance comprobado.
