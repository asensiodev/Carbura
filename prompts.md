# Carbura - Registro de Prompts

> Este documento recoge los prompts principales utilizados o preparados durante el proyecto para justificar el uso de asistentes de codigo en las distintas fases del ciclo de vida. Se mantiene como documento vivo: los prompts marcados como "plantilla" se sustituiran o completaran con prompts reales conforme avance la implementacion.

## Indice

1. [Descripcion general del producto](#1-descripcion-general-del-producto)
2. [Arquitectura del sistema](#2-arquitectura-del-sistema)
3. [Modelo de datos](#3-modelo-de-datos)
4. [Especificacion de la API](#4-especificacion-de-la-api)
5. [Historias de usuario](#5-historias-de-usuario)
6. [Tickets de trabajo](#6-tickets-de-trabajo)
7. [Pull requests](#7-pull-requests)

---

## 1. Descripcion general del producto

### Prompt 1 - PRD inicial

**Estado:** usado / base documental.

```text
Actua como product manager senior. Ayudame a definir un PRD para Carbura, una aplicacion multiplataforma Android y Desktop para gestionar el mantenimiento de vehiculos familiares. El documento debe incluir vision del producto, problema, usuarios, objetivos del MVP, alcance, fuera de alcance, casos de uso principales, requisitos no funcionales, stack tecnologico, arquitectura de alto nivel, modelo de datos resumido, metodologia de desarrollo y criterios de exito del MVP.
```

### Prompt 2 - Revision de alcance MVP

**Estado:** usado parcialmente.

```text
Revisa el PRD de Carbura desde el punto de vista de un MVP realista para un TFM. Identifica riesgos de alcance, funcionalidades que deberian ser P0/P1/P2, dependencias entre funcionalidades y que partes podrian posponerse sin romper el valor principal del producto.
```

### Prompt 3 - Sintesis para README

**Estado:** usado parcialmente.

```text
A partir del PRD de Carbura, genera una descripcion clara para el README del proyecto. Debe explicar el problema, la solucion, el publico objetivo, las funcionalidades principales del MVP y el valor diferencial en un lenguaje entendible para evaluadores tecnicos y no tecnicos.
```

---

## 2. Arquitectura del sistema

### 2.1. Diagrama de arquitectura

#### Prompt 1 - Arquitectura inicial

**Estado:** usado parcialmente.

```text
Actua como arquitecto software senior especializado en Kotlin Multiplatform. Propon una arquitectura para Carbura usando Compose Multiplatform, SQLDelight, Ktor, Koin y Supabase. Debe ser offline-first, seguir Clean Architecture y separar claramente presentation, domain, data, local datasource, remote datasource y sincronizacion.
```

#### Prompt 2 - Diagrama Mermaid

**Estado:** usado parcialmente.

```text
Genera un diagrama Mermaid de alto nivel para la arquitectura de Carbura. Debe mostrar Android App, Desktop App, shared commonMain, SQLDelight local database, Ktor client, Supabase Auth, Supabase PostgreSQL, Supabase Storage y SyncManager. Incluye una breve explicacion de cada flujo.
```

#### Prompt 3 - Revision de tradeoffs

**Estado:** usado parcialmente.

```text
Revisa esta arquitectura de Carbura y analiza tradeoffs. Evalua especialmente offline-first, sincronizacion last-write-wins, uso de Supabase, KMP commonMain, tests, seguridad y complejidad para un MVP. Indica riesgos y mitigaciones.
```

### 2.2. Descripcion de componentes principales

#### Prompt 1 - Componentes por capa

**Estado:** usado parcialmente.

```text
Describe los componentes principales de Carbura por capas: UI Compose, ViewModels/UiState, Use Cases, entidades de dominio, Repositories, LocalDataSource SQLDelight, RemoteDataSource Supabase/Ktor y SyncManager. Explica responsabilidades, dependencias permitidas y que codigo deberia vivir en commonMain.
```

#### Prompt 2 - Responsabilidades del SyncManager

**Estado:** usado parcialmente.

```text
Define las responsabilidades del SyncManager de Carbura para un MVP offline-first. Incluye sincronizacion de vehiculos, mantenimientos y recordatorios, deteccion de cambios locales, subida/bajada desde Supabase, estrategia last-write-wins y tratamiento de errores de red.
```

#### Prompt 3 - Seguridad por componente

**Estado:** usado parcialmente.

```text
Analiza los componentes de Carbura desde seguridad. Explica como deben manejarse autenticacion Google, tokens Supabase, Row Level Security por family_id, almacenamiento local, secretos de desarrollo y permisos de notificaciones.
```

### 2.3. Descripcion de alto nivel del proyecto y estructura de ficheros

#### Prompt 1 - Estructura KMP

**Estado:** usado parcialmente.

```text
Propon una estructura de carpetas para Carbura como proyecto Kotlin Multiplatform con Android y Desktop. Debe incluir modulos o carpetas para shared/commonMain, commonTest, androidApp, desktopApp, SQLDelight, documentacion, OpenSpec y configuracion local segura.
```

#### Prompt 2 - Convenciones de paquetes

**Estado:** usado parcialmente.

```text
Define convenciones de paquetes y nombres para Carbura siguiendo Clean Architecture en Kotlin Multiplatform. Incluye ejemplos para features de vehicles, maintenance-records, reminders y sync.
```

#### Prompt 3 - Revision de estructura

**Estado:** plantilla.

```text
Revisa la estructura actual del repositorio Carbura y detecta problemas de organizacion, mezcla de responsabilidades, ficheros que no deberian versionarse o convenciones inconsistentes. Propon cambios minimos.
```

### 2.4. Infraestructura y despliegue

#### Prompt 1 - Infraestructura Supabase

**Estado:** plantilla.

```text
Disena la infraestructura minima de Supabase para Carbura MVP. Incluye Auth con Google, PostgreSQL, Storage para adjuntos futuros, RLS por family_id, variables de entorno necesarias y separacion entre configuracion local y repositorio.
```

#### Prompt 2 - Setup local

**Estado:** plantilla.

```text
Genera instrucciones de setup local para Carbura. Deben cubrir Android Studio, JDK, Gradle, Kotlin Multiplatform, configuracion de Supabase, local.properties, ejecucion de tests, ejecucion Android y ejecucion Desktop.
```

#### Prompt 3 - Checklist de despliegue/demo

**Estado:** plantilla.

```text
Prepara un checklist de despliegue y demo para Carbura MVP. Debe incluir verificacion de Supabase, autenticacion, datos de prueba, sincronizacion entre dispositivos, notificaciones locales, exportacion y comandos de build.
```

### 2.5. Seguridad

#### Prompt 1 - Modelo de seguridad

**Estado:** plantilla.

```text
Define el modelo de seguridad de Carbura. Incluye autenticacion con Google, autorizacion por familia, Row Level Security en Supabase, aislamiento por family_id, tratamiento de codigos de invitacion y gestion segura de secrets.
```

#### Prompt 2 - Politicas RLS

**Estado:** plantilla.

```text
Propon politicas Row Level Security para las tablas de Carbura: families, users, vehicles, maintenance_types, maintenance_records y reminders. Cada usuario solo debe acceder a datos de su family_id. Incluye ejemplos SQL y riesgos a validar.
```

#### Prompt 3 - Revision de secretos

**Estado:** usado parcialmente.

```text
Revisa el repositorio antes de hacer commit y busca patrones de secretos o credenciales: api keys, tokens, Supabase keys, Google client secrets, private keys, passwords o ficheros de entorno. Indica que archivos son seguros para versionar y cuales deben quedar fuera.
```

### 2.6. Tests

#### Prompt 1 - Estrategia TDD

**Estado:** usado parcialmente.

```text
Define una estrategia de tests TDD para Carbura. A partir de las specs OpenSpec y criterios de aceptacion, indica que tests unitarios, de integracion y de UI deberian escribirse para vehicles, maintenance-records, reminders, sync y auth.
```

#### Prompt 2 - Tests para una spec

**Estado:** plantilla.

```text
A partir de esta spec OpenSpec de Carbura, genera primero los tests que deberian fallar siguiendo TDD Red. No implementes codigo productivo todavia. Cubre casos felices, validaciones, errores y comportamiento offline cuando aplique.
```

#### Prompt 3 - Refactor seguro

**Estado:** plantilla.

```text
Con los tests ya en verde, revisa esta implementacion de Carbura y propon refactors minimos que mejoren claridad, separacion de responsabilidades y mantenibilidad sin cambiar comportamiento. Despues reejecuta los tests relevantes.
```

---

## 3. Modelo de Datos

### Prompt 1 - Modelo conceptual

**Estado:** usado parcialmente.

```text
A partir del PRD de Carbura, disena el modelo de datos conceptual del MVP. Incluye entidades, atributos principales, relaciones y restricciones para Family, User, Vehicle, MaintenanceType, MaintenanceRecord y Reminder.
```

### Prompt 2 - Diagrama entidad-relacion

**Estado:** usado parcialmente.

```text
Genera un diagrama Mermaid ER para el modelo de datos de Carbura. Incluye claves primarias, claves foraneas, cardinalidades, campos obligatorios, timestamps de sincronizacion y family_id para aislamiento multiusuario.
```

### Prompt 3 - SQLDelight y Supabase

**Estado:** usado parcialmente.

```text
Convierte el modelo de datos de Carbura en una propuesta compatible con SQLDelight local y Supabase PostgreSQL remoto. Explica diferencias necesarias, campos de sincronizacion, indices recomendados y restricciones de integridad.
```

---

## 4. Especificacion de la API

### Prompt 1 - API via Supabase

**Estado:** usado parcialmente.

```text
Describe como Carbura consume Supabase como backend. Aunque no haya una API REST propia, documenta los principales contratos de datos y operaciones remotas para vehiculos, mantenimientos, recordatorios y familias. Limita la documentacion a los 3 endpoints/operaciones mas relevantes para el MVP.
```

### Prompt 2 - OpenAPI si hay backend propio

**Estado:** plantilla condicional.

```text
Si Carbura necesitara una API propia ademas de Supabase, genera una especificacion OpenAPI minima con maximo 3 endpoints principales. Debe cubrir autenticacion delegada, sincronizacion de datos del garaje y exportacion de historial.
```

### Prompt 3 - Contratos de sincronizacion

**Estado:** usado parcialmente.

```text
Define los contratos de sincronizacion entre cliente KMP y Supabase para Carbura. Incluye payloads esperados, campos de control, timestamps, deleted_at si aplica, resolucion last-write-wins y errores recuperables.
```

---

## 5. Historias de Usuario

### Prompt 1 - Generacion desde PRD

**Estado:** usado.

```text
A partir del PRD de Carbura, crea las user stories mas importantes para un MVP. Priorizalas como P0, P1 y P2, manten el alcance realista, incluye trazabilidad con los casos de uso del PRD y define criterios de aceptacion claros para cada historia.
```

### Prompt 2 - Seleccion academica

**Estado:** usado.

```text
De las user stories de Carbura, selecciona las 3 mas representativas para documentarlas en la entrega academica. Deben cubrir dominio principal, valor de producto y un caso diferencial del MVP. Justifica brevemente por que se elige cada una.
```

### Prompt 3 - Refinamiento de criterios de aceptacion

**Estado:** plantilla.

```text
Revisa estas user stories de Carbura y mejora sus criterios de aceptacion para que sean verificables con tests. Usa formato Dado/Cuando/Entonces, evita ambiguedades y separa comportamiento funcional, validaciones, errores y offline-first.
```

---

## 6. Tickets de Trabajo

### Prompt 1 - Tickets desde user stories

**Estado:** usado parcialmente.

```text
Convierte las historias US-02, US-04 y US-06 de Carbura en tickets tecnicos accionables. Necesito un ticket de frontend, uno de backend/datos y uno de base de datos. Cada ticket debe incluir contexto, objetivo, alcance, fuera de alcance, tareas, criterios de aceptacion, tests TDD y riesgos.
```

### Prompt 2 - OpenSpec proposal

**Estado:** plantilla.

```text
Usando OpenSpec, crea una propuesta de cambio para implementar la capacidad indicada de Carbura. Lee `openspec/project.md`, `openspec/prd.md` y `docs/user-stories.md`. Genera proposal.md, design.md si hace falta, spec delta y tasks.md. Las tareas deben seguir TDD: primero tests que fallan, luego codigo minimo, luego refactor.
```

### Prompt 3 - Desglose TDD por ticket

**Estado:** plantilla.

```text
Para este ticket de Carbura, desglosa el trabajo en pasos TDD. Primero lista los tests que deben fallar, despues el codigo minimo para hacerlos pasar, despues refactors permitidos y finalmente comandos de verificacion.
```

---

## 7. Pull Requests

### Prompt 1 - Resumen de PR

**Estado:** plantilla.

```text
Genera la descripcion de una de las 3 Pull Requests oficiales de Carbura. Indica si corresponde a Entrega 1, Entrega 2 o Entrega final. Incluye resumen, historias/tickets relacionados, decisiones tecnicas, tests ejecutados, evidencia visual o despliegue si aplica, riesgos y checklist de revision.
```

### Prompt 2 - Revision previa al PR

**Estado:** plantilla.

```text
Revisa los cambios antes de abrir PR con mentalidad de code reviewer senior. Prioriza bugs, regresiones, riesgos de seguridad, inconsistencias con OpenSpec, falta de tests y desviaciones del MVP. Devuelve hallazgos ordenados por severidad con referencias a archivo y linea.
```

### Prompt 3 - Documentacion posterior al merge

**Estado:** plantilla.

```text
Tras cerrar este PR, actualiza la documentacion de Carbura. Refleja la user story implementada, tests relevantes, decisiones de arquitectura, cambios en specs OpenSpec y cualquier instruccion nueva de setup o uso.
```
