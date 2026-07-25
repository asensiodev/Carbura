# Guía de entrega final de Carbura

Esta guía convierte el cierre de Carbura en una secuencia reproducible. No se debe generar ni publicar la versión definitiva hasta completar la aceptación manual sobre el mismo commit que se vaya a etiquetar.

## 1. Qué falta antes de entregar

- [ ] Ejecutar la matriz manual de Android, Desktop, sincronización, offline, seguridad y paquetes de esta guía.
- [x] Implementar y ejecutar al menos un test E2E automatizado del flujo principal, requerido por las instrucciones académicas.
- [ ] Corregir cualquier defecto encontrado y repetir los casos afectados.
- [x] Alinear Android y Desktop en la versión `1.0.0`.
- [ ] Grabar y revisar el vídeo de 2–3 minutos.
- [ ] Generar de nuevo la APK y el DMG desde el commit final.
- [ ] Instalar y validar exactamente esos dos artefactos, no una compilación anterior.
- [x] Crear y publicar la rama `finalproject-AAC`.
- [ ] Crear la Pull Request final y la release `v1.0-final-AAC`.
- [ ] Añadir al README los enlaces definitivos de PR, release y vídeo. Esta tarea queda asignada al autor de la entrega.
- [ ] Archivar los cambios OpenSpec que queden completados después de la aceptación manual y ejecutar su validación estricta. `harden-cross-platform-inputs` ya está archivado.

Windows/MSI, Developer ID y notarización macOS quedan fuera de la entrega si no se dispone del host o de las credenciales necesarias. Esta limitación debe declararse, no ocultarse.

## 2. Preparación de la aceptación manual

### Entorno necesario

- Un Android físico con Google Play Services y permiso para instalar APK.
- Un Mac distinto o un usuario limpio de macOS para comprobar el DMG como lo recibiría el profesor.
- Dos cuentas Google de prueba, A y B, sin datos personales relevantes.
- El proyecto Supabase con las ocho migraciones aplicadas.
- La misma configuración pública de Supabase y Google en Android y Desktop.
- Conexión que se pueda desactivar en Android y macOS.
- Acceso al panel de Supabase para verificar filas y, si es posible, probar RLS con dos JWT de usuario.

### Registro de evidencia

Para cada caso anotar:

| Campo | Valor |
|---|---|
| Fecha y hora | |
| Commit probado | |
| Android/modelo/versión | |
| Mac/modelo/versión | |
| Cuenta de prueba | A / B |
| Resultado | OK / Fallo / No aplicable |
| Evidencia | Nombre de captura o clip |
| Observaciones | |

No grabar JWT, claves, rutas con el nombre personal, correos completos ni contenido de `local.properties`.

## 3. Matriz de pruebas manuales

### Instalación y arranque

#### M-01 — Instalación Android limpia

- [ ] Desinstalar una instalación anterior si se quiere comprobar el primer arranque.
- [ ] Instalar la APK candidata con `adb install app/android/build/outputs/apk/debug/android-debug.apk`.
- [ ] Abrir Carbura, verificar que no se cierra y que muestra onboarding o restaura una sesión válida.
- [ ] Confirmar que el nombre, icono y versión corresponden a la candidata.

Resultado esperado: arranque limpio sin crash, ANR ni pantalla vacía.

#### M-02 — Instalación exacta del DMG

- [ ] Montar el DMG candidato y copiar Carbura a Aplicaciones.
- [ ] Abrir la copia instalada, no `:app:desktop:run` ni un `.app` anterior.
- [ ] Si Gatekeeper bloquea el bundle ad-hoc, registrar la limitación y abrirlo con Control-clic > Abrir cuando macOS lo permita.
- [ ] Verificar arranque, navegación y persistencia tras cerrar y volver a abrir.

Resultado esperado: el paquete contiene su runtime y arranca sin depender de un JDK instalado.

### Autenticación, familia y sesión

#### M-03 — Misma cuenta en Android y Desktop

- [ ] Iniciar sesión con la cuenta A en Android.
- [ ] Iniciar sesión con la cuenta A en Desktop mediante navegador y PKCE.
- [ ] Comprobar que ambos clientes muestran la misma identidad/familia y los mismos datos tras sincronizar.

Resultado esperado: una sola familia personal, sin duplicados ni datos de otra cuenta.

#### M-04 — Restauración segura y cierre de sesión independiente

- [ ] Cerrar Desktop y volver a abrirlo; comprobar que Keychain restaura la sesión.
- [ ] Desactivar la red y volver a abrir; comprobar que los datos locales siguen disponibles.
- [ ] Cerrar sesión en Desktop y confirmar que Android sigue autenticado.
- [ ] Comprobar que Desktop vuelve a onboarding y no muestra datos autenticados residuales.

Resultado esperado: restauración segura, funcionamiento local-first y cierre de sesión limitado al cliente.

#### M-05 — Cambio de cuenta y aislamiento visual

- [ ] Cerrar sesión en Desktop con A e iniciar con B.
- [ ] Confirmar que B no ve vehículos, mantenimientos ni recordatorios de A.
- [ ] Volver a A y verificar que sus datos reaparecen tras sincronizar.

Resultado esperado: no existe visibilidad cruzada entre familias.

### Vehículos y tombstones

#### M-06 — Alta Desktop → Android

- [ ] Crear en Desktop un vehículo con nombre, tipo, odómetro, ITV, seguro y próxima revisión.
- [ ] Probar primero un valor inválido y confirmar el error junto al campo.
- [ ] Guardar y aceptar las sugerencias de recordatorio.
- [ ] Sincronizar Android y comprobar vehículo y recordatorios sin duplicados.

Resultado esperado: propagación completa y una sola sugerencia por objetivo estable.

#### M-07 — Edición Android → Desktop

- [ ] Editar en Android nombre, tipo, matrícula y planificación.
- [ ] Rotar el dispositivo con cambios sin guardar y confirmar que el formulario conserva su estado.
- [ ] Guardar, sincronizar Desktop y comprobar todos los campos.

Resultado esperado: estado preservado durante rotación y actualización bidireccional correcta.

#### M-08 — Odómetro y confirmación de descenso

- [ ] Actualizar rápidamente el odómetro con un valor mayor.
- [ ] Intentar un valor menor y comprobar que aparece confirmación.
- [ ] Cancelar una vez y aceptar otra vez.
- [ ] Verificar el resultado en ambos clientes.

Resultado esperado: cancelar no modifica; aceptar modifica y sincroniza.

#### M-09 — Borrado Desktop → Android

- [ ] Borrar un vehículo de prueba en Desktop y confirmar la acción.
- [ ] Sincronizar Android y comprobar que desaparece junto a su contenido asociado según la semántica del producto.
- [ ] Reiniciar ambos clientes y comprobar que no reaparece.

Resultado esperado: tombstone convergente, sin resurrección tras reinicio o sync.

### Mantenimiento

#### M-10 — Crear, editar y borrar bidireccionalmente

- [ ] Crear un mantenimiento en Android dejando el odómetro opcional vacío.
- [ ] Incluir coste con dos decimales, taller y notas.
- [ ] Comprobar el registro en Desktop y la presentación localizada de fecha e importe.
- [ ] Editarlo en Desktop y verificar el cambio en Android.
- [ ] Borrarlo en Android y comprobar que desaparece en Desktop tras sync.

Resultado esperado: los valores opcionales se conservan correctamente y las tres mutaciones convergen.

#### M-11 — Mantenimiento futuro sin recordatorio

- [ ] Crear un mantenimiento con fecha futura.
- [ ] Elegir “Guardar sin este recordatorio”.
- [ ] Comprobar que existe el mantenimiento y no se crea recordatorio planificado.

Resultado esperado: solo un mantenimiento, ningún recordatorio asociado.

#### M-12 — Mantenimiento futuro con recordatorio

- [ ] Crear otro mantenimiento futuro.
- [ ] Elegir “Guardar y crear recordatorio”.
- [ ] Comprobar el mantenimiento y exactamente un recordatorio en ambos clientes.
- [ ] Editar la fecha futura y confirmar que el recordatorio se actualiza sin duplicarse.

Resultado esperado: recordatorio determinista único y sincronizado.

### Recordatorios y notificaciones

#### M-13 — CRUD bidireccional

- [ ] Crear en Desktop un recordatorio por fecha y kilometraje.
- [ ] Verificarlo en Android.
- [ ] Completarlo en Android y verificar el estado en Desktop.
- [ ] Crear otro en Android, borrarlo en Desktop y confirmar que no reaparece.

Resultado esperado: crear, completar y borrar convergen en ambas direcciones.

#### M-14 — Fecha opcional y validación

- [ ] Crear un borrador Android con fecha, quitarla y dejar solo kilometraje.
- [ ] Introducir valores negativos, desbordados o texto pegado y confirmar errores específicos.
- [ ] Corregir y guardar.

Resultado esperado: la fecha se puede limpiar y los valores inválidos nunca se persisten silenciosamente.

#### M-15 — Notificación nativa solo en Android

- [ ] Conceder permiso de notificaciones en Android.
- [ ] Crear un recordatorio de prueba que deba notificarse en la ventana temporal elegida.
- [ ] Verificar la notificación y que al tocarla abre Recordatorios.
- [ ] Confirmar que Desktop muestra el recordatorio pero no crea una notificación del sistema.

Resultado esperado: entrega local Android y ausencia deliberada de alertas nativas Desktop.

### Offline, reinicio y conflictos

#### M-16 — Mutación offline Desktop

- [ ] Desconectar la red en macOS.
- [ ] Crear o editar un vehículo y un mantenimiento.
- [ ] Cerrar y abrir Desktop; comprobar que siguen visibles.
- [ ] Reconectar, sincronizar y comprobarlos en Android.

Resultado esperado: persistencia local tras reinicio y envío posterior sin pérdida.

#### M-17 — Mutación offline Android

- [ ] Activar modo avión.
- [ ] Crear un recordatorio y editar un vehículo.
- [ ] Forzar cierre y abrir; comprobar que los cambios siguen visibles.
- [ ] Reconectar, sincronizar y comprobarlos en Desktop.

Resultado esperado: cambios pendientes persistentes y convergencia al recuperar red.

#### M-18 — Last-write-wins

- [ ] Partir del mismo vehículo sincronizado en ambos clientes.
- [ ] Poner ambos offline y editar el mismo campo con valores distintos.
- [ ] Reconectar primero el cambio más antiguo y después el más reciente.
- [ ] Sincronizar de nuevo ambos clientes.

Resultado esperado: ambos terminan mostrando la versión con `updated_at` más reciente y no quedan alternando valores.

### Modo local Desktop

Usar datos desechables y restaurar la configuración pública antes de continuar.

#### M-19 — Importar datos locales

- [ ] Crear datos en modo local Desktop.
- [ ] Iniciar sesión y elegir importar.
- [ ] Verificar que se asignan a la familia autenticada y aparecen en Android.

Resultado esperado: adopción explícita sin duplicados.

#### M-20 — Excluir datos locales

- [ ] Preparar de nuevo datos locales desechables.
- [ ] Iniciar sesión y elegir excluir.
- [ ] Confirmar que no se suben ni aparecen en Android.

Resultado esperado: exclusión efectiva y sin contaminación de la familia.

#### M-21 — Cancelar la decisión

- [ ] Preparar datos locales y comenzar el login/importación.
- [ ] Cancelar la decisión.
- [ ] Confirmar que no comienza sync autenticado y que los datos locales permanecen intactos.

Resultado esperado: cancelación sin mutaciones parciales.

### Seguridad y cuenta

#### M-22 — RLS con dos cuentas

- [ ] Obtener de forma temporal una sesión de prueba A y otra B sin registrar sus tokens.
- [ ] Con A, intentar leer y escribir usando el `family_id` de B mediante PostgREST.
- [ ] Intentar upsert, tombstone, reasignación de familia y modificación de perfil/familia ajenos.
- [ ] Repetir en sentido B → A.

Resultado esperado: lecturas vacías o denegadas y escrituras rechazadas por RLS. Nunca usar `service_role`, porque omitiría la frontera que se pretende probar.

#### M-23 — Eliminación permanente con cuenta desechable

- [ ] Ejecutar este caso al final con una cuenta/familia creada solo para la prueba.
- [ ] Cancelar el primer diálogo y verificar que nada cambia.
- [ ] Confirmar la eliminación permanente.
- [ ] Comprobar cierre local, limpieza de datos y rechazo de la sesión anterior.

Resultado esperado: eliminación irreversible solo tras confirmación y limpieza convergente.

### UI y accesibilidad mínima

#### M-24 — Android compacto, teclado y rotación

- [ ] Recorrer formularios de vehículo, mantenimiento y recordatorio con teclado abierto.
- [ ] Confirmar que Guardar sigue accesible y que el primer error se desplaza a la vista.
- [ ] Rotar durante cada formulario y comprobar que el borrador se conserva.
- [ ] Probar Atrás y toque exterior en confirmaciones; nunca deben guardar implícitamente.

Resultado esperado: acciones alcanzables, estado conservado y cierres seguros.

#### M-25 — Desktop estrecho y texto ampliado

- [ ] Reducir la ventana hasta su mínimo y recorrer Garaje, Mantenimiento, Recordatorios y Cuenta.
- [ ] Comprobar campos apilados, calendarios, scroll y acciones completas.
- [ ] Aumentar el tamaño de texto del sistema si el entorno lo permite.
- [ ] Guardar una mutación lenta y comprobar que campos y cierre quedan bloqueados.

Resultado esperado: sin clipping, acciones inaccesibles, diálogos apilados ni edición durante guardado.

## 4. Criterio de salida de la aceptación

La candidata se considera aceptada cuando:

- [ ] Todos los casos aplicables están en OK.
- [ ] Cada fallo tiene corrección y repetición documentada.
- [ ] No hay defectos críticos o altos abiertos.
- [ ] El commit aceptado está anotado y no cambia antes de generar artefactos.
- [ ] El test E2E automatizado y los gates completos están en verde.

## 5. Guion del vídeo de 2–3 minutos

### Preparación

- Resolución recomendada: 1920×1080, 16:9, 30 fps.
- Duración objetivo: 2:30; máximo recomendado: 3:00.
- Android y Desktop ya autenticados con una cuenta de demo.
- Datos iniciales mínimos y nombres legibles, sin correos personales.
- Notificaciones concedidas y una fecha de ejemplo preparada.
- Escritorio limpio, notificaciones privadas desactivadas y zoom legible.
- Grabar voz y pantalla en pistas separadas si la herramienta lo permite.

### Escaleta

| Tiempo | Imagen | Narración principal |
|---|---|---|
| 00:00–00:15 | Título y ambos clientes | Problema: centralizar vehículos, mantenimientos y recordatorios familiares. |
| 00:15–00:35 | Android y Desktop con la misma cuenta | Kotlin Multiplatform, SQLDelight local-first, Supabase Auth/RLS y sync familiar. |
| 00:35–01:00 | Crear vehículo en Desktop y verlo en Android | CRUD bidireccional y sugerencias de ITV, seguro y revisión sin duplicados. |
| 01:00–01:30 | Crear mantenimiento futuro | Coste, taller, historial y decisión explícita de crear o no recordatorio. |
| 01:30–01:50 | Recordatorio en ambos clientes y notificación Android | Desktop sincroniza; Android programa la alerta local. |
| 01:50–02:10 | Desconectar red y mostrar un cambio local | Persistencia offline, reinicio y convergencia posterior. |
| 02:10–02:30 | Cuenta Desktop y arquitectura/CI | PKCE, Keychain, cierre/eliminación, tests y pipeline. |
| 02:30–02:45 | Pantalla final | Repositorio, release y limitaciones: sin iOS, MSI ni firma/notarización de producción. |

### Texto de cierre sugerido

> Carbura entrega un flujo completo Android y Desktop para gestionar un garaje familiar de forma local-first. El código, las especificaciones, los tests y los instalables reproducibles están disponibles en el repositorio y en la release final.

### Revisión del vídeo

- [ ] Dura entre 2 y 3 minutos.
- [ ] Se entiende sin leer el README.
- [ ] Muestra valor funcional, no solo código.
- [ ] No revela secretos ni datos personales.
- [ ] El texto y el cursor son legibles.
- [ ] Audio sin ruido y volumen uniforme.
- [ ] Subtítulos revisados.
- [ ] El enlace permite acceso al profesor sin solicitar permisos.

## 6. Preparar la revisión final

### Congelar la candidata

```bash
git status --short
git rev-parse HEAD
git pull --ff-only
```

El árbol debe estar limpio. Registrar el SHA resultante en la evidencia manual.

Android y Desktop declaran `1.0.0`. Si se publica una APK debug para evaluación, identificarla explícitamente como debug; no llamarla APK de producción.

### Gate automatizado

```bash
./gradlew qualityCheck test assembleDebug :app:desktop:jar --stacktrace
./gradlew connectedDebugAndroidTest --max-workers=1
openspec validate --all --strict
git diff --check
```

El E2E está integrado en `MainActivityE2ETest` y se puede ejecutar de forma aislada con:

```bash
./gradlew :app:android:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.carbura.MainActivityE2ETest \
  --max-workers=1
```

## 7. Generar APK Android

La configuración actual no incluye firma release propia. Para una entrega académica reproducible se puede publicar la APK debug, indicando claramente esta condición.

```bash
./gradlew :app:android:clean :app:android:assembleDebug
./gradlew :app:android:signingReport
```

Salida canónica:

```text
app/android/build/outputs/apk/debug/android-debug.apk
```

Instalación del artefacto exacto:

```bash
adb uninstall com.asensiodev.carbura
adb install app/android/build/outputs/apk/debug/android-debug.apk
```

Comprobar login Google después de cualquier cambio de firma. La huella SHA de la clave usada debe estar registrada en el cliente Android de Google.

Nombre recomendado tras verificarlo:

```text
Carbura-Android-1.0.0-debug.apk
```

Renombrar únicamente después de instalar y aceptar exactamente el APK generado.

## 8. Generar DMG macOS

Se necesita un JDK completo 17 con `jpackage`.

```bash
java -version
jpackage --version
./gradlew :app:desktop:clean :app:desktop:packageDmg
```

Salida canónica:

```text
app/desktop/build/compose/binaries/main/dmg/Carbura-1.0.0.dmg
```

Verificación técnica:

```bash
hdiutil verify app/desktop/build/compose/binaries/main/dmg/Carbura-1.0.0.dmg
codesign --verify --deep --strict --verbose=2 /Applications/Carbura.app
spctl --assess --type execute --verbose=4 /Applications/Carbura.app
```

La firma actual es ad-hoc. `codesign` puede validar la integridad mientras `spctl` rechaza la distribución descargada por no disponer de Developer ID/notarización. Registrar esta salida como limitación conocida e incluir instrucciones de apertura para el profesor.

Después de instalar el DMG final repetir como mínimo M-02, M-03, M-04, M-06, M-10, M-13 y M-16.

## 9. Checksums y publicación

Calcular checksums después de la última validación, sin volver a compilar:

```bash
shasum -a 256 Carbura-Android-1.0.0-debug.apk
shasum -a 256 Carbura-1.0.0.dmg
```

Crear en GitHub una release con:

- Tag: `v1.0-final-AAC`.
- Título: `Carbura 1.0 — Entrega final AAC`.
- Commit/tag: exactamente el SHA aceptado.
- Assets: APK, DMG y un archivo de checksums.
- Notas: funcionalidades, instalación, configuración necesaria y limitaciones conocidas.
- Enlace al vídeo.

No subir `local.properties`, keystores, tokens, sesiones, bases de datos personales ni capturas con datos sensibles.

## 10. Rama, PR y formulario final

Las instrucciones requieren una rama final identificable:

```bash
git switch -c finalproject-AAC
git push -u origin finalproject-AAC
```

La PR final hacia `main` debe incluir:

- Resumen de producto y arquitectura.
- Historias/tickets cerrados.
- Comandos y resultados de tests.
- Matriz manual resumida.
- Enlace a release y vídeo.
- Limitaciones conocidas.

Antes de enviar el formulario comprobar:

- [ ] README con secciones `0–7` completas.
- [ ] `prompts.md` actualizado.
- [ ] URLs reales de PR 1, PR 2 y PR final.
- [x] Rama `finalproject-AAC` accesible.
- [ ] CI de la PR en verde.
- [ ] Release y vídeo accesibles sin permisos especiales.
- [ ] APK y DMG descargados desde GitHub e instalados una última vez.
- [ ] OpenSpec completado y archivado.
- [ ] Formulario enviado con la URL solicitada por la convocatoria.

## 11. Limitaciones que deben declararse

- Android se distribuye como APK debug mientras no se configure una firma release propia.
- El DMG usa firma ad-hoc y no está notarizado.
- MSI y Windows Credential Manager no están validados sin un host Windows.
- Desktop no entrega notificaciones nativas; las programa Android tras sincronizar.
- iOS y Linux quedan fuera del alcance.
- El coste acumulado por vehículo no está implementado; sí se conservan y muestran costes individuales.
- La sincronización se ejecuta mientras la aplicación está activa, no mediante un servicio remoto con la app cerrada.

## 12. Enlaces que completará el autor

Estos valores dependen de servicios externos y deben rellenarse manualmente después de publicar cada recurso. No inventar URLs ni marcar la tarea como terminada antes de comprobarlas en una sesión privada del navegador.

| Recurso | Enlace pendiente | Dónde actualizarlo |
|---|---|---|
| PR final `finalproject-AAC` -> `main` | `<URL_PR_FINAL>` | Sección 7 de `readme.md` |
| GitHub Release `v1.0-final-AAC` | `<URL_RELEASE_FINAL>` | Introducción o instalación de `readme.md` |
| Vídeo de 2–3 minutos | `<URL_VIDEO_DEMO>` | Descripción de la PR, release y `readme.md` |
| Rama final | `https://github.com/asensiodev/Carbura/tree/finalproject-AAC` | Formulario académico |

Checklist del autor:

- [ ] Abrir cada enlace en una ventana privada.
- [ ] Confirmar que no solicita permisos adicionales al profesor.
- [ ] Sustituir todos los placeholders.
- [ ] Verificar que APK, DMG y checksums se descargan desde la release.
- [ ] Enviar en el formulario la URL exacta solicitada por la convocatoria.
