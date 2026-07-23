# Modelo de amenazas de autenticacion Desktop

Este documento define los limites de confianza y controles obligatorios del login y la sincronizacion de Carbura Desktop. Complementa `desktop-auth-sync-setup.md` y no contiene secretos.

## Activos protegidos

- Refresh tokens, access tokens, ID tokens y codigos OAuth.
- Verificador PKCE y estado transitorio del intento de login.
- Identidad Supabase del usuario y su `family_id` autorizado.
- Datos locales de vehiculos, mantenimientos y recordatorios.
- Decision explicita sobre importacion de datos `local-family`.

## Limites de confianza

### Navegador del sistema

Google y Supabase se abren solo en el navegador predeterminado. Carbura no recoge passwords ni renderiza login en un WebView. El navegador se considera externo y solo devuelve un code de autorizacion al callback local.

### Callback loopback

El listener temporal enlaza exclusivamente `127.0.0.1:43821`, se abre antes del navegador, acepta un solo intento activo y se cierra en cualquier resultado. PKCE impide intercambiar un code interceptado sin el verificador conservado en memoria. Peticiones invalidas, duplicadas, mayores de 8 KiB o fuera del timeout se rechazan sin registrar parametros.

### Vault del sistema operativo

Keychain en macOS y Credential Manager en Windows son la unica persistencia de sesion. No existe fallback a fichero, SQLDelight, Preferences o variables de entorno. Las entradas se separan por aplicacion, proyecto Supabase, entorno y cuenta. Una build release falla de forma cerrada si el vault no esta disponible.

### Base local

SQLDelight contiene datos funcionales y decisiones de importacion, nunca credenciales. Cada lectura o mutacion debe validar la familia activa. Los datos `local-family` no se reasignan, suben ni borran sin consentimiento ligado al snapshot exacto presentado.

### Supabase

RLS y RPC son la frontera real de autorizacion. El cliente puede incluir la anon/publishable key, pero nunca `service_role`, Client Secret de Google ni password de base de datos. El cliente no puede asignarse otro `family_id`; altas o cambios de membresia pasan por operaciones de servidor que derivan el actor de `auth.uid()`.

### Logs y diagnosticos

Los logs pueden incluir categorias estables y estados, pero nunca URLs OAuth completas, codes, verifier PKCE, tokens, cookies, cabeceras Authorization, secretos OAuth ni respuestas completas de Supabase.

## Amenazas y controles

- Intercepcion local del callback: PKCE S256, bind previo, loopback exacto, listener one-shot y code intercambiado una sola vez.
- Suplantacion o replay: un solo intento activo, timeout de cinco minutos, parametros estrictos y limpieza terminal del verifier.
- Robo de sesion en disco: vault nativo como unica persistencia y limpieza al cerrar sesion.
- Persistencia de sesion revocada: refresh y validacion antes de mostrar contenido; credenciales invalidas se eliminan sin borrar datos.
- Escalada entre familias: RLS endurecida, campos de identidad/membresia inmutables para el cliente y pruebas hostiles con dos familias.
- Subida local no consentida: sync bloqueado hasta resolver el snapshot local de forma explicita e idempotente.
- Cambio de cuenta con cache previa: todas las operaciones locales y jobs se limitan por familia activa y generacion.
- Colision de IDs: preflight y remapeo transaccional con preservacion de relaciones antes de pull o push.
- Filtracion en artefactos: inspeccion de paquetes y rechazo de secretos privilegiados antes de release.

## Riesgos residuales

- Malware con control de la sesion del sistema operativo puede observar memoria o automatizar el navegador; queda fuera del aislamiento que una aplicacion de usuario puede garantizar.
- Un proceso local puede ocupar el puerto y provocar denegacion de servicio. Carbura falla antes de abrir el navegador y no usa interfaces externas.
- SQLDelight no se cifra en este cambio. La proteccion de datos funcionales en reposo depende del cifrado de disco y permisos de la cuenta del sistema.
- La implementacion nativa del vault debe validarse en paquetes firmados de macOS y Windows; compilar en otro sistema no sustituye esa prueba.
