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

Windows/MSI queda fuera del alcance validado de esta entrega porque no se dispone de un PC Windows. Developer ID y notarización macOS también quedan fuera por no disponer de las credenciales necesarias. Estas limitaciones deben declararse, no ocultarse.

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

- [x] Desinstalar una instalación anterior si se quiere comprobar el primer arranque.
- [x] Instalar la APK candidata con `adb install app/android/build/outputs/apk/debug/android-debug.apk`.
- [x] Abrir Carbura, verificar que no se cierra y que muestra onboarding o restaura una sesión válida.
- [x] Confirmar que el nombre, icono y versión corresponden a la candidata.

Resultado esperado: arranque limpio sin crash, ANR ni pantalla vacía.

#### M-02 — Instalación exacta del DMG

- [x] Montar el DMG candidato y copiar Carbura a Aplicaciones.
- [x] Abrir la copia instalada, no `:app:desktop:run` ni un `.app` anterior.
- [x] Si Gatekeeper bloquea el bundle ad-hoc, registrar la limitación y abrirlo con Control-clic > Abrir cuando macOS lo permita.
- [x] Verificar arranque, navegación y persistencia tras cerrar y volver a abrir.

Resultado esperado: el paquete contiene su runtime y arranca sin depender de un JDK instalado.

Resultado: OK tras corregir y repetir la instalación. Se verificaron tamaño y posición Retina, navegación, títulos funcionales, identidad sin comillas de serialización, paneles de Cuenta alineados, persistencia, firma ad-hoc íntegra y limitación esperada de Gatekeeper.

### Autenticación, familia y sesión

#### M-03 — Misma cuenta en Android y Desktop

- [x] Iniciar sesión con la cuenta A en Android.
- [x] Iniciar sesión con la cuenta A en Desktop mediante navegador y PKCE.
- [x] Comprobar que ambos clientes muestran la misma identidad/familia y los mismos datos tras sincronizar.

Resultado esperado: una sola familia personal, sin duplicados ni datos de otra cuenta.

#### M-04 — Restauración segura y cierre de sesión independiente

- [x] Cerrar Desktop y volver a abrirlo; comprobar que Keychain restaura la sesión.
- [x] Desactivar la red y volver a abrir; comprobar que los datos locales siguen disponibles.
- [x] Cerrar sesión en Desktop y confirmar que Android sigue autenticado.
- [x] Comprobar que Desktop vuelve a onboarding y no muestra datos autenticados residuales.

Resultado esperado: restauración segura, funcionamiento local-first y cierre de sesión limitado al cliente.

Resultado: OK tras corregir y repetir el arranque offline. La sesión restaurada abre el ámbito exacto de la cuenta, conserva la sección activa durante el reintento, presenta errores localizados y el cierre de Desktop no afecta a Android.

#### M-05 — Cambio de cuenta y aislamiento visual

- [x] Cerrar sesión en Desktop con A e iniciar con B.
- [x] Confirmar que B no ve vehículos, mantenimientos ni recordatorios de A.
- [x] Volver a A y verificar que sus datos reaparecen tras sincronizar.

Resultado esperado: no existe visibilidad cruzada entre cuentas.

Resultado: OK. B se mostró sin datos de A y, al volver a A, sus vehículos, mantenimientos y recordatorios reaparecieron tras sincronizar sin datos cruzados.

### Vehículos y tombstones

#### M-06 — Alta Desktop → Android

- [x] Crear en Desktop un vehículo con nombre, tipo, odómetro, ITV, seguro y próxima revisión.
- [x] Probar primero un valor inválido y confirmar el error junto al campo.
- [x] Guardar y aceptar las sugerencias de recordatorio.
- [x] Sincronizar Android y comprobar vehículo y recordatorios sin duplicados.

Resultado esperado: propagación completa y una sola sugerencia por objetivo estable.

Resultado: OK tras sustituir el selector de fecha Desktop incompatible. El valor negativo mostró validación inline; el vehículo, su planificación y los recordatorios aceptados llegaron una sola vez a Android sin notificación inmediata.

#### M-07 — Edición Android → Desktop

- [x] Editar en Android nombre, tipo, matrícula y planificación.
- [x] Rotar el dispositivo con cambios sin guardar y confirmar que el formulario conserva su estado.
- [x] Guardar, sincronizar Desktop y comprobar todos los campos.

Resultado esperado: estado preservado durante rotación y actualización bidireccional correcta.

Resultado: OK. Android conservó los cambios sin guardar al rotar y Desktop recibió nombre, tipo, matrícula y planificación actualizados.

#### M-08 — Odómetro y confirmación de descenso

- [x] Actualizar rápidamente el odómetro con un valor mayor.
- [x] Intentar un valor menor y comprobar que aparece confirmación.
- [x] Cancelar una vez y aceptar otra vez.
- [x] Verificar el resultado en ambos clientes.

Resultado esperado: cancelar no modifica; aceptar modifica y sincroniza.

Resultado: OK. La cancelación conservó el valor mayor y la confirmación posterior propagó el descenso aceptado a ambos clientes.

#### M-09 — Borrado Desktop → Android

- [x] Borrar un vehículo de prueba en Desktop y confirmar la acción.
- [x] Sincronizar Android y comprobar que desaparece junto a su contenido asociado según la semántica del producto.
- [x] Reiniciar ambos clientes y comprobar que no reaparece.

Resultado esperado: tombstone convergente, sin resurrección tras reinicio o sync.

Resultado: OK. El vehículo y sus recordatorios asociados desaparecieron en Android y no reaparecieron en ninguno de los clientes tras reinicio y nueva sincronización.

### Mantenimiento

#### M-10 — Crear, editar y borrar bidireccionalmente

- [x] Crear un mantenimiento en Android dejando el odómetro opcional vacío.
- [x] Incluir coste con dos decimales, taller y notas.
- [x] Comprobar el registro en Desktop y la presentación localizada de fecha e importe.
- [x] Editarlo en Desktop y verificar el cambio en Android.
- [x] Borrarlo en Android y comprobar que desaparece en Desktop tras sync.

Resultado esperado: los valores opcionales se conservan correctamente y las tres mutaciones convergen.

Resultado: OK. El odómetro opcional permaneció vacío, fecha e importe se mostraron localizados y alta, edición y borrado convergieron entre Android y Desktop.

#### M-11 — Mantenimiento futuro sin recordatorio

- [x] Crear un mantenimiento con fecha futura.
- [x] Elegir “Guardar sin este recordatorio”.
- [x] Comprobar que existe el mantenimiento y no se crea recordatorio planificado.

Resultado esperado: solo un mantenimiento, ningún recordatorio asociado.

Resultado: OK. El mantenimiento futuro quedó disponible en ambos clientes y no se creó ningún recordatorio asociado.

#### M-12 — Mantenimiento futuro con recordatorio

- [x] Crear otro mantenimiento futuro.
- [x] Elegir “Guardar y crear recordatorio”.
- [x] Comprobar el mantenimiento y exactamente un recordatorio en ambos clientes.
- [x] Editar la fecha futura y confirmar que el recordatorio se actualiza sin duplicarse.

Resultado esperado: recordatorio determinista único y sincronizado.

Resultado: OK. Ambos clientes mostraron un único recordatorio asociado; al cambiar la fecha desde Desktop se actualizó el existente sin duplicarse ni emitir una notificación inmediata.

### Recordatorios y notificaciones

#### M-13 — CRUD bidireccional

- [x] Crear en Desktop un recordatorio por fecha y kilometraje.
- [x] Verificarlo en Android.
- [x] Completarlo en Android y verificar el estado en Desktop.
- [x] Crear otro en Android, borrarlo en Desktop y confirmar que no reaparece.

Resultado esperado: crear, completar y borrar convergen en ambas direcciones.

Resultado: OK. Alta, completado y borrado convergieron entre clientes y el recordatorio eliminado no reapareció tras reiniciar.

#### M-14 — Fecha opcional y validación

- [x] Crear un borrador Android con fecha, quitarla y dejar solo kilometraje.
- [x] Introducir valores negativos, desbordados o texto pegado y confirmar errores específicos.
- [x] Corregir y guardar.

Resultado esperado: la fecha se puede limpiar y los valores inválidos nunca se persisten silenciosamente.

Resultado: OK. La fecha se eliminó del borrador, los valores negativo, desbordado y alfanumérico fueron rechazados y solo se guardó el kilometraje válido corregido.

#### M-15 — Notificación nativa solo en Android

- [x] Conceder permiso de notificaciones en Android.
- [x] Crear un recordatorio de prueba que deba notificarse en la ventana temporal elegida.
- [x] Verificar la notificación y que al tocarla abre Recordatorios.
- [x] Confirmar que Desktop muestra el recordatorio pero no crea una notificación del sistema.

Resultado esperado: entrega local Android y ausencia deliberada de alertas nativas Desktop.

Resultado: OK. Android entregó la notificación de un recordatorio vencido en la fecha actual y abrió Recordatorios al tocarla; Desktop sincronizó el registro sin crear una alerta nativa.

### Offline, reinicio y conflictos

#### M-16 — Mutación offline Desktop

- [x] Desconectar la red en macOS.
- [x] Crear o editar un vehículo y un mantenimiento.
- [x] Cerrar y abrir Desktop; comprobar que siguen visibles.
- [x] Reconectar, sincronizar y comprobarlos en Android.

Resultado esperado: persistencia local tras reinicio y envío posterior sin pérdida.

Resultado: OK. El vehículo y mantenimiento creados en Desktop permanecieron tras reiniciar sin red y llegaron una sola vez a Android al recuperar la conexión.

#### M-17 — Mutación offline Android

- [x] Activar modo avión.
- [x] Crear un recordatorio y editar un vehículo.
- [x] Forzar cierre y abrir; comprobar que los cambios siguen visibles.
- [x] Reconectar, sincronizar y comprobarlos en Desktop.

Resultado esperado: cambios pendientes persistentes y convergencia al recuperar red.

Resultado: OK. Android conservó el recordatorio y la edición del vehículo tras cierre forzado sin red; ambos cambios llegaron una sola vez a Desktop al reconectar.

#### M-18 — Last-write-wins

- [x] Partir del mismo vehículo sincronizado en ambos clientes.
- [x] Poner ambos offline y editar el mismo campo con valores distintos.
- [x] Reconectar primero el cambio más antiguo y después el más reciente.
- [x] Sincronizar de nuevo ambos clientes.

Resultado esperado: ambos terminan mostrando la versión con `updated_at` más reciente y no quedan alternando valores.

Resultado: OK. Ambos clientes convergieron de forma estable en `M18 Reciente`, la mutación con `updated_at` posterior, y sincronizaciones adicionales no alternaron el valor.

### Modo local Desktop

Usar datos desechables y restaurar la configuración pública antes de continuar.

#### M-19 — Importar datos locales

- [x] Crear datos en modo local Desktop.
- [x] Iniciar sesión y elegir importar.
- [x] Verificar que se asignan a la familia autenticada y aparecen en Android.

Resultado esperado: adopción explícita sin duplicados.

Resultado: OK. El resumen local reflejó el vehículo y mantenimiento preparados; la importación los asignó a A y llegaron una sola vez a Android sin duplicarse tras nuevas sincronizaciones.

#### M-20 — Excluir datos locales

- [x] Preparar de nuevo datos locales desechables.
- [x] Iniciar sesión y elegir excluir.
- [x] Confirmar que no se suben ni aparecen en Android.

Resultado esperado: exclusión efectiva y sin contaminación de la familia.

Resultado: OK. Desktop mantuvo los registros locales separados y lo comunicó en Cuenta; A y Android permanecieron sin el vehículo excluido tras sincronizaciones repetidas.

#### M-21 — Cancelar la decisión

- [x] Preparar datos locales y comenzar el login/importación.
- [x] Cancelar la decisión.
- [x] Confirmar que no comienza sync autenticado y que los datos locales permanecen intactos.

Resultado esperado: cancelación sin mutaciones parciales.

Resultado: OK. Cancelar devolvió Desktop al modo local, conservó intactos los datos separados y no inició sincronización ni subida a la cuenta A.

### Seguridad y cuenta

#### M-22 — RLS con dos cuentas

- [ ] Obtener de forma temporal una sesión de prueba A y otra B sin registrar sus tokens.
- [ ] Con A, intentar leer y escribir usando el `family_id` de B mediante PostgREST.
- [ ] Intentar upsert, tombstone, reasignación de familia y modificación de perfil/familia ajenos.
- [ ] Repetir en sentido B → A.

Resultado esperado: lecturas vacías o denegadas y escrituras rechazadas por RLS. Nunca usar `service_role`, porque omitiría la frontera que se pretende probar.

Estado de entrega: no ejecutada por decisión de cierre. Las políticas y sus pruebas automatizadas permanecen en el repositorio, pero no se aporta evidencia manual hostil con sesiones A/B.

#### M-23 — Eliminación permanente con cuenta desechable

- [ ] Ejecutar este caso al final con una cuenta/familia creada solo para la prueba.
- [ ] Cancelar el primer diálogo y verificar que nada cambia.
- [ ] Confirmar la eliminación permanente.
- [ ] Comprobar cierre local, limpieza de datos y rechazo de la sesión anterior.

Resultado esperado: eliminación irreversible solo tras confirmación y limpieza convergente.

Estado de entrega: no ejecutada por decisión de cierre para evitar una operación destructiva adicional. Se conserva cobertura automatizada de cancelación, confirmación única y limpieza convergente.

### UI y accesibilidad mínima

#### M-24 — Android compacto, teclado y rotación

- [ ] Recorrer formularios de vehículo, mantenimiento y recordatorio con teclado abierto.
- [ ] Confirmar que Guardar sigue accesible y que el primer error se desplaza a la vista.
- [ ] Rotar durante cada formulario y comprobar que el borrador se conserva.
- [ ] Probar Atrás y toque exterior en confirmaciones; nunca deben guardar implícitamente.

Resultado esperado: acciones alcanzables, estado conservado y cierres seguros.

Estado de entrega: no ejecutada como recorrido completo. M-07 verificó rotación con borrador y M-14 verificó errores de entrada, pero no se reclama cobertura manual integral de los tres formularios.

#### M-25 — Desktop estrecho y texto ampliado

- [ ] Reducir la ventana hasta su mínimo y recorrer Garaje, Mantenimiento, Recordatorios y Cuenta.
- [ ] Comprobar campos apilados, calendarios, scroll y acciones completas.
- [ ] Aumentar el tamaño de texto del sistema si el entorno lo permite.
- [ ] Guardar una mutación lenta y comprobar que campos y cierre quedan bloqueados.

Resultado esperado: sin clipping, acciones inaccesibles, diálogos apilados ni edición durante guardado.

Estado de entrega: no ejecutada como recorrido completo. M-02 verificó tamaño mínimo y M-06 validó el calendario instalado, pero no se aporta prueba manual de texto ampliado del sistema.

## 4. Criterio de salida de la aceptación

La candidata se considera aceptada para la entrega académica con el siguiente alcance:

- [x] M-01 a M-21 están en OK.
- [x] Cada fallo observado tiene corrección y repetición documentada.
- [x] No hay defectos críticos o altos observados abiertos.
- [x] M-22 a M-25 quedan declaradas como validación manual no ejecutada, sin presentarlas como superadas.
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
| 00:00–00:15 | Título y ambos clientes | Problema: centralizar vehículos, mantenimientos y recordatorios en una sola aplicación. |
| 00:15–00:35 | Android y Desktop con la misma cuenta | Kotlin Multiplatform, SQLDelight local-first, Supabase Auth/RLS y aislamiento por cuenta. |
| 00:35–01:00 | Crear vehículo en Desktop y verlo en Android | CRUD bidireccional y sugerencias de ITV, seguro y revisión sin duplicados. |
| 01:00–01:30 | Crear mantenimiento futuro | Coste, taller, historial y decisión explícita de crear o no recordatorio. |
| 01:30–01:50 | Recordatorio en ambos clientes y notificación Android | Desktop sincroniza; Android programa la alerta local. |
| 01:50–02:10 | Desconectar red y mostrar un cambio local | Persistencia offline, reinicio y convergencia posterior. |
| 02:10–02:30 | Cuenta Desktop y arquitectura/CI | PKCE, Keychain, cierre/eliminación, tests y pipeline. |
| 02:30–02:45 | Pantalla final | Repositorio, release y limitaciones: Windows/MSI no validado por falta de PC Windows, sin iOS ni firma/notarización de producción. |

### Texto de cierre sugerido

> Carbura entrega un flujo completo Android y Desktop para gestionar vehículos, mantenimientos y recordatorios de forma local-first. El código, las especificaciones, los tests y los instalables reproducibles están disponibles en el repositorio y en la release final.

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

La PR final desde `finalproject-AAC` hacia `dev` debe incluir:

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
- MSI, ejecución Windows y Windows Credential Manager quedan fuera del alcance validado por no disponer de un PC Windows.
- Desktop no entrega notificaciones nativas; las programa Android tras sincronizar.
- iOS y Linux quedan fuera del alcance.
- El coste acumulado por vehículo no está implementado; sí se conservan y muestran costes individuales.
- La sincronización se ejecuta mientras la aplicación está activa, no mediante un servicio remoto con la app cerrada.

## 12. Enlaces que completará el autor

Estos valores dependen de servicios externos y deben rellenarse manualmente después de publicar cada recurso. No inventar URLs ni marcar la tarea como terminada antes de comprobarlas en una sesión privada del navegador.

| Recurso | Enlace pendiente | Dónde actualizarlo |
|---|---|---|
| PR final `finalproject-AAC` -> `dev` | `<URL_PR_FINAL>` | Sección 7 de `readme.md` |
| GitHub Release `v1.0-final-AAC` | `<URL_RELEASE_FINAL>` | Introducción o instalación de `readme.md` |
| Vídeo de 2–3 minutos | `<URL_VIDEO_DEMO>` | Descripción de la PR, release y `readme.md` |
| Rama final | `https://github.com/asensiodev/Carbura/tree/finalproject-AAC` | Formulario académico |

Checklist del autor:

- [ ] Abrir cada enlace en una ventana privada.
- [ ] Confirmar que no solicita permisos adicionales al profesor.
- [ ] Sustituir todos los placeholders.
- [ ] Verificar que APK, DMG y checksums se descargan desde la release.
- [ ] Enviar en el formulario la URL exacta solicitada por la convocatoria.
