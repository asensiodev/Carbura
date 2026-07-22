# Configuracion segura de autenticacion y sincronizacion Desktop

Este documento recoge los pasos externos necesarios para habilitar el login de Google y la sincronizacion de Carbura Desktop. No contiene secretos reales. No copies credenciales privadas en el repositorio, en incidencias, en logs ni en capturas de pantalla.

La implementacion prevista usa OAuth Authorization Code con PKCE S256, el navegador del sistema y un callback temporal limitado a `127.0.0.1`. La URL planificada para la primera version es:

```text
http://127.0.0.1:43821/auth/callback
```

La URL configurada en Supabase y la utilizada por la aplicacion deben coincidir exactamente. Si cambia el puerto o la ruta durante la implementacion, actualiza ambos sitios antes de probar.

## 1. Requisitos previos

- Acceso de administrador al proyecto de Supabase.
- Acceso al proyecto de Google Cloud usado por Android.
- Proveedor Google ya habilitado o permiso para habilitarlo.
- Migraciones de `supabase/migrations/` aplicadas.
- RLS activo en las tablas familiares.
- Una cuenta Google de prueba y, preferiblemente, una segunda cuenta para verificar aislamiento.

## 2. Google Cloud Console

1. Abre `APIs & Services > OAuth consent screen`.
2. Comprueba que el nombre, dominio de soporte y politica de privacidad corresponden a Carbura.
3. Si la aplicacion sigue en modo Testing, registra las cuentas que probaran Android y Desktop.
4. Abre `APIs & Services > Credentials`.
5. Selecciona el cliente OAuth de tipo `Web application` usado por el proveedor Google de Supabase.
6. En `Authorized redirect URIs`, confirma la URL de callback de Supabase:

```text
https://<PROJECT_REF>.supabase.co/auth/v1/callback
```

7. No anadas `127.0.0.1` en Google Cloud para este flujo. Google devuelve el resultado a Supabase; Supabase redirige despues a Carbura Desktop.
8. No incluyas el Client Secret de Google en Desktop, `local.properties`, variables de CI destinadas al cliente ni artefactos de distribucion. El secreto, si Google lo exige, se configura unicamente en el proveedor de Supabase.

## 3. Proveedor Google en Supabase

1. Abre `Authentication > Providers > Google`.
2. Activa el proveedor Google.
3. Comprueba que el Client ID corresponde al cliente OAuth web de Google Cloud.
4. Configura el Client Secret solamente en Supabase Dashboard.
5. Guarda los cambios.
6. No uses el Client ID Android como Client ID del proveedor Supabase.

## 4. Redirect Desktop en Supabase

1. Abre `Authentication > URL Configuration`.
2. En `Redirect URLs`, anade exactamente:

```text
http://127.0.0.1:43821/auth/callback
```

3. No uses `http://0.0.0.0`, una IP de red local, una IP publica ni un comodin de host.
4. No permitas rutas globales como `http://127.0.0.1/**` si puede registrarse la ruta exacta.
5. Conserva las URLs Android/web existentes que sigan en uso.
6. Para entornos separados, configura proyectos Supabase separados o una lista explicita por entorno. No mezcles callbacks de desarrollo y produccion sin revisarlos.

El callback usa HTTP porque `127.0.0.1` es loopback y el trafico no sale del equipo. La comunicacion entre navegador, Google y Supabase continua usando HTTPS.

## 5. Configuracion publica local

`local.properties` debe seguir fuera de Git. Usa valores publicos del cliente:

```properties
SUPABASE_URL=https://<PROJECT_REF>.supabase.co
SUPABASE_ANON_KEY=<ANON_OR_PUBLISHABLE_KEY>
# Solo lo necesita Android para validar el ID token de Credential Manager:
GOOGLE_CLIENT_ID=<WEB_CLIENT_ID>.apps.googleusercontent.com
```

Reglas:

- `SUPABASE_ANON_KEY` o la publishable key puede distribuirse en el cliente, pero RLS debe limitar todas las operaciones.
- Desktop no debe empaquetar ni requerir `GOOGLE_CLIENT_ID` para el flujo OAuth alojado por Supabase; Android sigue usandolo como audience del ID token.
- No configures `service_role` en ninguna aplicacion cliente.
- No configures passwords de base de datos en la aplicacion.
- No configures Client Secret de Google en Desktop.
- No imprimas estos valores junto a sesiones o cabeceras de autorizacion en logs.

La implementacion Desktop debe cargar solamente la configuracion publica necesaria. Antes de publicar, inspecciona el paquete final y no solo el arbol de fuentes.

## 6. Perfil y familia

1. Confirma que existe la RPC `ensure_user_profile`.
2. Confirma que solo usuarios autenticados pueden ejecutarla.
3. Inicia sesion con la cuenta usada en Android.
4. Comprueba que `user_profiles.user_id` coincide con `auth.users.id`.
5. Comprueba que el perfil conserva el mismo `family_id`; Desktop no debe crear otra familia para el mismo usuario.
6. No permitas que el cliente elija libremente un `family_id` durante login o sync.

No es necesario implementar invitaciones o multiples familias para compartir datos entre Android y Desktop con la misma cuenta.

## 7. Verificacion de RLS

La migracion inicial actual no es suficiente para publicar este flujo: una policy amplia de `user_profiles` puede permitir que un cliente modificado cambie su propio `family_id`. Antes de habilitar Desktop debe aplicarse la migracion de hardening incluida en las tareas del cambio OpenSpec.

Antes de publicar Desktop, verifica como minimo:

- RLS esta activo en `families`, `user_profiles`, `vehicles`, `maintenance_records` y `reminders`.
- Las policies se basan en `auth.uid()` y el perfil o membresia del usuario.
- Un cliente autenticado no puede modificar directamente `user_profiles.user_id` ni `user_profiles.family_id`.
- Los cambios de membresia se realizan solo mediante una RPC restringida que deriva el actor de `auth.uid()`.
- Un miembro sin rol de propietario no puede actualizar o borrar la familia ni administrar perfiles ajenos.
- Cambiar `family_id` en una peticion cliente no concede acceso.
- Un usuario no puede leer registros de otra familia.
- Un usuario no puede insertar, actualizar, borrar ni restaurar tombstones de otra familia.
- Las RPC con `SECURITY DEFINER`, si existen, validan `auth.uid()` internamente y tienen un `search_path` seguro.
- El rol `anon` no puede acceder a datos familiares.

Realiza una prueba negativa con dos cuentas y dos familias. Intenta tambien reasignar el perfil propio a la familia ajena y actualizar o borrar familia/perfiles. Una prueba correcta con una sola cuenta no demuestra aislamiento.

## 8. Almacenamiento seguro de sesion

No hay configuracion de Supabase para este punto, pero debe verificarse en cada sistema:

### macOS

- El refresh token se almacena en Keychain bajo un servicio e identificador propios de Carbura.
- La entrada se separa por proyecto Supabase, entorno y cuenta, no se sincroniza entre dispositivos cuando la API lo permite y solo es accesible por la aplicacion firmada en la sesion del usuario.
- El paquete esta firmado y, para distribucion, notarizado.
- Cerrar sesion elimina la entrada de Keychain.

### Windows

- El refresh token se almacena en Windows Credential Manager.
- La credencial es de ambito del usuario actual y se separa por proyecto Supabase, entorno y cuenta.
- El ejecutable o MSI esta firmado.
- Cerrar sesion elimina la credencial.

En ambos sistemas:

- No debe aparecer ningun token en la base SQLDelight, archivos de datos, Java Preferences, argumentos del proceso, variables de entorno, logs o informes de errores.
- La persistencia por defecto del SDK Supabase debe estar desactivada o sustituida: el vault del sistema operativo es la unica persistencia de sesion.
- La rotacion del refresh token actualiza el vault de forma atomica y elimina entradas sustituidas.
- Una build release debe rechazar persistencia insegura si el almacen seguro no esta disponible.

## 9. Prueba del callback loopback

1. Comprueba que Carbura enlaza el puerto antes de abrir el navegador.
2. Comprueba que escucha solo en `127.0.0.1`, no en `0.0.0.0`, `::`, Wi-Fi o Ethernet.
3. Comprueba que solo se permite un intento OAuth activo y que expira a los cinco minutos.
4. Comprueba que el listener se cierra tras un callback, error OAuth, cancelacion o timeout.
5. Envia callbacks con metodo, host o ruta incorrectos y verifica que se rechazan.
6. Envia parametros `code` duplicados, `error` duplicados o `code` y `error` juntos y verifica que se rechazan.
7. Envia una peticion o cabeceras mayores de 8 KiB y verifica que se rechazan.
8. Repite un callback ya consumido y verifica que se rechaza.
9. Ocupa el puerto antes del login y verifica que Carbura no abre el navegador ni busca una interfaz externa.
10. Comprueba que ningun log contiene code, verifier, state, nonce, token o URL completa con parametros.

Supabase Auth valida el `state` y `nonce` de su intercambio con Google. Carbura valida que el code recibido pertenece al unico intento PKCE vivo; no debe simular una validacion de parametros que Supabase no devuelve al callback local.

## 10. Prueba de importacion local

Prepara Desktop con un vehiculo local antes de iniciar sesion y verifica las tres decisiones:

### Importar y combinar

- El usuario confirma antes de cualquier subida.
- Se mantienen IDs cuando no hay colision; si la hay, se remapean los IDs locales y se conservan todas las relaciones.
- Los registros pasan a la familia autenticada una sola vez.
- La aprobacion solo cubre el snapshot exacto mostrado; nuevos registros o versiones locales requieren otra decision.
- Si falla la red, permanecen pendientes para reintento.
- Android recibe los registros despues de sincronizar.
- Si un ID local colisiona con uno remoto, se remapea el dato local y todas sus relaciones antes del push.

### Usar datos de la cuenta

- Los registros `local-family` no se suben ni se borran.
- Desktop descarga los datos existentes de Android.
- Cuenta informa de que aun existen datos solo locales.
- Un ID remoto igual a uno local no sustituye ni destruye el registro excluido.

### Cancelar

- No se modifica ningun registro.
- No se ejecuta sync autenticado.
- El usuario puede volver al modo local o reintentar login.

## 11. Prueba cruzada Android y Desktop

1. Inicia sesion en Android y Desktop con la misma cuenta Google.
2. Confirma que ambos resuelven el mismo `user_id` y `family_id`.
3. Crea un vehiculo en Android y sincroniza.
4. Sincroniza Desktop y confirma que aparece.
5. Registra mantenimiento y recordatorios desde Desktop.
6. Sincroniza Android y confirma que aparecen bajo el mismo vehiculo.
7. Prueba cambios offline, reintento, tombstones y conflicto last-write-wins.
8. Cierra y abre Desktop para verificar restauracion y refresh de sesion.
9. Inicia sesion con una segunda cuenta y confirma que no puede ver ni mutar la cache de la primera.
10. Cierra sesion y confirma que desaparecen las credenciales Desktop, no se elimina la base local y Android permanece autenticado.

## 12. Checklist antes de produccion

- [ ] OAuth consent screen revisada y publicada o con testers correctos.
- [ ] Google callback de Supabase configurado.
- [ ] Redirect Desktop exacto configurado en Supabase.
- [ ] No hay comodines de host innecesarios.
- [ ] RLS y RPC verificadas con pruebas positivas y negativas.
- [ ] Migracion de hardening aplicada; el cliente no puede reasignar su perfil ni administrar familias/perfiles sin autorizacion.
- [ ] No existe `service_role` ni Client Secret en artefactos cliente.
- [ ] Tokens almacenados solo en Keychain/Credential Manager.
- [ ] Logs y crash reports revisados para evitar secretos.
- [ ] Importacion local probada en las tres ramas.
- [ ] Colisiones de IDs y cambio de cuenta probados sin perdida ni acceso cruzado.
- [ ] Sync Android/Desktop probado con la misma cuenta.
- [ ] MSI firmado y aplicacion macOS firmada/notarizada.
- [ ] Dependencias de OAuth y credential storage revisadas y fijadas.
