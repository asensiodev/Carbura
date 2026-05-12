# Carbura - User Stories MVP

Este documento deriva las historias de usuario principales desde el PRD (`openspec/prd.md`). El objetivo es mantener el MVP acotado y trazable antes de convertir estas historias en specs OpenSpec, tickets tecnicos y tests TDD.

## Criterio de priorizacion

| Prioridad | Significado |
|---|---|
| P0 | Imprescindible para que el MVP demuestre el valor principal. |
| P1 | Importante para completar la experiencia MVP, pero puede implementarse despues de P0. |
| P2 | MVP extendido o mejora si hay tiempo. |

## Epicas MVP

| Epica | Descripcion | Prioridad |
|---|---|---|
| E01 - Onboarding y garaje familiar | Acceso con Google y creacion del espacio familiar. | P0 |
| E02 - Gestion de vehiculos | Alta y consulta de vehiculos del garaje. | P0 |
| E03 - Mantenimientos e historial | Registro de eventos, costes, kilometros e historial. | P0 |
| E04 - Recordatorios | Recordatorios por fecha/km y avisos locales. | P0 |
| E05 - Offline-first y sincronizacion | Uso sin conexion y sincronizacion eventual. | P1 |
| E06 - Colaboracion familiar | Invitaciones para compartir el garaje. | P1 |
| E07 - Exportacion | Exportar historial de vehiculo. | P2 |

---

## P0 - MVP estricto

### US-01 - Crear cuenta y garaje familiar

**Como** usuario nuevo,
**quiero** iniciar sesion con Google y crear mi garaje familiar,
**para** empezar a registrar los vehiculos de mi familia.

**Trazabilidad:** UC-01, PRD objetivos 1 y 6.

**Criterios de aceptacion:**

- Dado un usuario sin sesion, cuando inicia sesion con Google correctamente, entonces accede a la app como usuario autenticado.
- Dado un usuario autenticado sin garaje, cuando entra por primera vez, entonces puede crear un garaje familiar.
- Dado un garaje familiar creado, cuando el usuario accede a la app, entonces ve el garaje como espacio activo.
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

**Datos minimos del MVP:** nombre, tipo, matricula opcional, kilometros actuales.

### US-03 - Ver listado y detalle de vehiculos

**Como** miembro de la familia,
**quiero** ver los vehiculos del garaje y abrir el detalle de cada uno,
**para** consultar rapidamente su informacion y estado de mantenimiento.

**Trazabilidad:** UC-02, UC-04, PRD objetivos 1 y 4.

**Criterios de aceptacion:**

- Dado un garaje con vehiculos, cuando el usuario abre la app, entonces ve una lista de vehiculos disponibles.
- Dado un vehiculo de la lista, cuando el usuario lo selecciona, entonces ve su detalle.
- Dado un vehiculo sin mantenimientos, cuando el usuario abre su detalle, entonces ve un estado vacio claro.
- Dado que no hay vehiculos, cuando el usuario abre el garaje, entonces la app muestra una accion principal para anadir el primer vehiculo.

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

**Tipos iniciales del MVP:** ITV, aceite, neumaticos, seguro, revision general, averia, personalizado.

### US-05 - Consultar historial de mantenimiento

**Como** miembro de la familia,
**quiero** consultar el historial de mantenimiento de un vehiculo,
**para** saber que se hizo, cuando, con cuantos kilometros y cuanto costo.

**Trazabilidad:** UC-04, PRD objetivo 4.

**Criterios de aceptacion:**

- Dado un vehiculo con mantenimientos, cuando el usuario abre su historial, entonces ve los registros ordenados por fecha descendente.
- Dado un registro de mantenimiento, cuando aparece en el historial, entonces muestra al menos tipo, fecha, kilometros y coste si existe.
- Dado un vehiculo sin historial, cuando el usuario abre la seccion, entonces ve un estado vacio con opcion de registrar mantenimiento.
- Dado varios registros con coste, cuando el usuario consulta el historial, entonces puede ver el coste acumulado del vehiculo.

### US-06 - Generar recordatorio automatico tras registrar ITV o seguro

**Como** propietario del vehiculo,
**quiero** que la app cree recordatorios automaticamente tras registrar una ITV o seguro,
**para** no olvidar el siguiente vencimiento.

**Trazabilidad:** UC-03, UC-05, PRD objetivos 2 y 3.

**Criterios de aceptacion:**

- Dado un registro de ITV, cuando se guarda con fecha de vencimiento o fecha de proxima ITV, entonces la app crea un recordatorio asociado.
- Dado un registro de seguro, cuando se guarda con fecha de vencimiento, entonces la app crea un recordatorio asociado.
- Dado un recordatorio automatico, cuando se crea, entonces usa 30 dias de antelacion por defecto.
- Dado un vehiculo con recordatorio creado, cuando el usuario abre la pantalla de recordatorios, entonces el recordatorio aparece visible.

### US-07 - Ver proximos recordatorios

**Como** miembro de la familia,
**quiero** ver los proximos recordatorios del garaje,
**para** saber que mantenimientos o vencimientos requieren atencion.

**Trazabilidad:** UC-05, PRD objetivos 2 y 3.

**Criterios de aceptacion:**

- Dado un garaje con recordatorios futuros, cuando el usuario abre la pantalla de recordatorios, entonces ve los recordatorios ordenados por proximidad.
- Dado un recordatorio, cuando se muestra, entonces incluye vehiculo, tipo, fecha objetivo y antelacion configurada.
- Dado que no hay recordatorios, cuando el usuario abre la pantalla, entonces ve un estado vacio comprensible.
- Dado un recordatorio vencido, cuando aparece en la lista, entonces se identifica visualmente como vencido.

### US-08 - Recibir notificacion local de un recordatorio

**Como** usuario,
**quiero** recibir una notificacion local antes de un vencimiento,
**para** actuar con tiempo aunque no abra la app a diario.

**Trazabilidad:** UC-05, PRD objetivos 2 y 3.

**Criterios de aceptacion:**

- Dado un recordatorio con antelacion configurada, cuando llega la fecha de aviso, entonces la app programa o muestra una notificacion local.
- Dado que el usuario pulsa la notificacion, cuando la app se abre, entonces navega al contexto del vehiculo o recordatorio.
- Dado que las notificaciones no tienen permiso en Android, cuando la app necesita avisar, entonces solicita o explica el permiso necesario.
- Dado que el dispositivo esta offline, cuando llega el momento del aviso, entonces la notificacion local puede mostrarse sin depender del backend.

---

## P1 - MVP completo

### US-09 - Actualizar odometro rapidamente

**Como** miembro de la familia,
**quiero** actualizar rapidamente los kilometros actuales de un vehiculo,
**para** mantener los recordatorios por kilometraje y el historial al dia.

**Trazabilidad:** UC-06, PRD scope MVP.

**Criterios de aceptacion:**

- Dado un vehiculo existente, cuando el usuario actualiza el odometro, entonces el nuevo valor queda guardado.
- Dado un valor inferior al kilometraje actual, cuando el usuario intenta guardarlo, entonces la app pide confirmacion o muestra una validacion.
- Dado que el usuario esta offline, cuando actualiza el odometro, entonces el cambio queda disponible localmente y pendiente de sincronizacion.

### US-10 - Sincronizar datos entre dispositivos

**Como** miembro de una familia,
**quiero** que los datos del garaje se sincronicen entre mis dispositivos,
**para** ver los mismos vehiculos, mantenimientos y recordatorios en Android y Desktop.

**Trazabilidad:** UC-09, PRD objetivos 5 y 6.

**Criterios de aceptacion:**

- Dado un cambio realizado en un dispositivo con conexion, cuando otro dispositivo sincroniza, entonces el cambio aparece en el segundo dispositivo.
- Dado un cambio realizado offline, cuando el dispositivo recupera conexion, entonces el cambio se sincroniza con el backend.
- Dado dos cambios sobre el mismo dato, cuando se sincronizan, entonces se aplica la estrategia last-write-wins definida en arquitectura.
- Dado un error de sincronizacion, cuando ocurre, entonces la app conserva los datos locales y reintenta posteriormente.

### US-11 - Invitar a un familiar al garaje

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

## P2 - MVP extendido

### US-12 - Exportar historial de un vehiculo

**Como** miembro de la familia,
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

## Siguiente paso

Convertir estas historias en specs OpenSpec por capacidad, empezando por `vehicles`, `maintenance-records` y `reminders`. Despues, derivar tickets tecnicos y tests TDD desde los criterios de aceptacion.
