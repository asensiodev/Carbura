# Uso de IA, toolchain y procedimiento de trabajo

## 1. Proposito del documento

Este archivo conserva el nombre `prompts.md` requerido por la plantilla de entrega. Conforme al criterio acordado en tutoria, no recopila transcripciones de prompts: documenta como se ha integrado la inteligencia artificial en el desarrollo de Carbura, incluidas las herramientas, el reparto de responsabilidades, la seleccion de modelos, los procedimientos, los controles de calidad y la supervision humana.

La IA se ha utilizado como apoyo durante el ciclo de vida del producto, no como autoridad final. Las decisiones de producto, arquitectura, alcance y aceptacion de cambios permanecen bajo responsabilidad humana.

La toolchain tecnica completa del proyecto se describe en [`docs/toolchain/carbura_toolchain.md`](docs/toolchain/carbura_toolchain.md). Este documento se centra especificamente en su componente de IA.

## 2. Toolchain de IA

| Herramienta | Uso principal en Carbura | Resultado esperado |
|---|---|---|
| **OpenCode** | Agente principal conectado al repositorio | Exploracion del codigo, edicion controlada, ejecucion de comandos, tests y verificacion |
| **OpenSpec** | Flujo de Specification-Driven Development | Propuestas, diseno, especificaciones, tareas y archivo trazable de cambios |
| **Gemini** | Generacion y exploracion de UI | Alternativas visuales, composicion de pantallas, estados y revision de experiencia de usuario |
| **Perplexity** | Investigacion externa | Descubrimiento de fuentes, contraste de alternativas y recopilacion inicial de referencias |
| **ChatGPT / modelos OpenAI** | Desarrollo y documentacion mediante OpenCode | Implementacion, arquitectura, depuracion, revision de codigo y redaccion tecnica |
| **Android Studio y emulador** | Validacion fuera del modelo | Compilacion, inspeccion visual, pruebas instrumentadas y comprobacion del comportamiento real |
| **Gradle, Git y CI** | Verificacion reproducible | Calidad estatica, tests, builds y trazabilidad de los cambios aceptados |

Las herramientas tienen funciones complementarias. Una propuesta visual generada con Gemini, una referencia localizada con Perplexity o una implementacion producida con ChatGPT no se incorpora directamente al producto: primero se contrasta con el repositorio, las especificaciones, la documentacion oficial aplicable y las pruebas ejecutables.

## 3. Estrategia de seleccion de modelos

La seleccion del modelo depende de la complejidad, ambiguedad y riesgo de la tarea, no solo de su longitud.

### Modelos de baja latencia

Para tareas pequenas, mecanicas y bien delimitadas se utilizan modelos generalistas rapidos, sin razonamiento extendido. Algunos ejemplos son:

- Localizar archivos o referencias concretas.
- Corregir formato o errores tipograficos.
- Realizar cambios repetitivos de bajo riesgo.
- Ejecutar comprobaciones conocidas.
- Resumir resultados ya verificados.

El objetivo es reducir coste y tiempo cuando no se necesita una deliberacion profunda.

### Modelos con razonamiento extendido

Para tareas complejas, ambiguas o de mayor impacto se utilizan modelos con capacidades de razonamiento extendido. Algunos ejemplos son:

- Diseno de arquitectura y limites entre modulos.
- Concurrencia estructurada y cancelacion de corrutinas.
- Sincronizacion local-first y resolucion de conflictos.
- Cambios de persistencia, migraciones o efectos secundarios duraderos.
- Diagnostico de fallos que atraviesan varias capas.
- Revision de propuestas OpenSpec y evaluacion de riesgos.

Este enrutado evita usar modelos costosos para operaciones triviales y evita delegar decisiones complejas a modelos sin capacidad suficiente para analizar sus consecuencias.

No se fija en este documento una version concreta de cada modelo. Las versiones disponibles pueden evolucionar durante el proyecto; lo relevante para la trazabilidad es la familia de herramienta, el tipo de tarea, el procedimiento seguido y la evidencia verificable producida.

## 4. OpenCode como entorno de trabajo asistido

OpenCode se utiliza desde el directorio del repositorio y trabaja sobre los mismos archivos que Android Studio, VS Code, Git y Gradle. Su funcion no se limita a generar texto: puede inspeccionar el proyecto, editar archivos, ejecutar comandos y verificar el resultado.

El procedimiento habitual es:

1. Examinar el estado real del repositorio antes de proponer cambios.
2. Leer el PRD, las especificaciones y el codigo relacionado.
3. Identificar incertidumbres, riesgos y criterios de aceptacion.
4. Seleccionar un procedimiento directo o un cambio OpenSpec segun el alcance.
5. Implementar el cambio minimo que satisface el comportamiento acordado.
6. Anadir o adaptar tests que cubran el comportamiento y las regresiones relevantes.
7. Ejecutar formato, analisis estatico, tests y builds.
8. Revisar el diff completo y mantener separados los cambios no relacionados.
9. Solicitar revision humana antes de cerrar, archivar, hacer commit o publicar.

OpenCode tambien permite dividir investigaciones complejas en agentes especializados. Por ejemplo, un agente de exploracion puede localizar contratos y dependencias mientras el agente principal conserva la responsabilidad sobre la decision y la integracion final. La delegacion no elimina la verificacion posterior.

## 5. Skills y procedimientos OpenSpec

Carbura configura en `.opencode/skills/` cuatro skills vinculados al ciclo de vida de OpenSpec:

| Skill | Procedimiento |
|---|---|
| `openspec-explore` | Investigar una idea, problema o ambiguedad sin modificar todavia el contrato funcional |
| `openspec-propose` | Crear proposal, design, delta specs y tareas para un cambio nuevo |
| `openspec-apply-change` | Implementar de forma incremental las tareas de un cambio aprobado |
| `openspec-archive-change` | Validar, integrar las especificaciones aceptadas y archivar un cambio completado |

Estos skills se exponen tambien mediante los comandos `/opsx-explore`, `/opsx-propose`, `/opsx-apply` y `/opsx-archive`.

El flujo aplicado a cambios sustanciales es:

```text
Necesidad o incidencia
  -> exploracion del producto y del codigo
  -> propuesta OpenSpec
  -> revision humana
  -> tests y criterios de aceptacion
  -> implementacion incremental
  -> refactor
  -> quality gates y validacion manual
  -> revision humana final
  -> archivo OpenSpec
  -> commit y pull request
```

No todas las tareas requieren una propuesta. Una correccion pequena, local y sin cambio contractual puede implementarse directamente, pero conserva los mismos requisitos de revision y verificacion.

## 6. Metodologia asistida por IA

### Specification-Driven Development

OpenSpec define que comportamiento se espera y por que se necesita. Los artefactos de `openspec/changes/` registran el contexto, las decisiones, los riesgos y las tareas. Tras la aceptacion, las especificaciones consolidadas de `openspec/specs/` actuan como fuente de verdad funcional.

La IA ayuda a redactar y contrastar estos artefactos, pero una especificacion no se considera aprobada por el mero hecho de haber sido generada.

### Test-Driven Development pragmatico

Los criterios de aceptacion se traducen en tests cuando aportan una comprobacion estable. El ciclo utilizado es:

```text
Red -> Green -> Refactor -> Verificacion completa
```

En correcciones de regresiones se intenta reproducir primero el fallo. En cambios de UI se combinan tests instrumentados con comprobacion manual, porque una asercion semantica no sustituye la inspeccion visual y de interaccion.

### DDD ligero y diseno pragmatico

La IA debe respetar los limites existentes entre presentacion, dominio, datos e integraciones de plataforma. Se aplican DDD ligero, SOLID y CUPID cuando mejoran claridad y testabilidad, evitando abstracciones preventivas que no respondan a una necesidad concreta del MVP.

## 7. Reparto de responsabilidades por herramienta

### Gemini para UI

Gemini se utiliza para generar y explorar propuestas de interfaz, jerarquia visual, composicion, estados de pantalla y alternativas de experiencia de usuario. Sus resultados sirven como material de ideacion y contraste.

Antes de trasladar una propuesta a Compose se revisan:

- Coherencia con Material 3 y el design system de Carbura.
- Accesibilidad, semantica y tamanos tactiles.
- Comportamiento en distintas dimensiones de pantalla.
- Estados vacios, carga, error y confirmacion destructiva.
- Interaccion con teclado, insets, gestos y TalkBack.
- Viabilidad tecnica dentro de la arquitectura Android existente.

La implementacion final se revisa y prueba en emulador o dispositivo. Una imagen o propuesta generada no se considera evidencia de que la UI funciona.

### Perplexity para investigacion

Perplexity se utiliza como punto de partida para investigacion externa, especialmente cuando es necesario comparar enfoques o localizar documentacion relevante. No se trata como fuente de verdad independiente.

El procedimiento de investigacion es:

1. Delimitar la pregunta y el contexto tecnico.
2. Localizar fuentes primarias o referencias relevantes.
3. Comprobar fecha, version y aplicabilidad al stack del proyecto.
4. Contrastar las conclusiones con documentacion oficial y codigo real.
5. Registrar en OpenSpec las decisiones que afecten al contrato o la arquitectura.

No se incorpora una recomendacion solamente porque aparezca en una respuesta sintetizada. Se revisan las fuentes y se valida experimentalmente cuando es posible.

### ChatGPT y OpenAI para codigo y documentacion

Los modelos OpenAI, utilizados principalmente mediante OpenCode, apoyan:

- Exploracion y explicacion del repositorio.
- Diseno tecnico y evaluacion de alternativas.
- Generacion y modificacion de codigo Kotlin, SQL, Gradle y Compose.
- Creacion y mantenimiento de tests.
- Diagnostico de errores de compilacion, ejecucion y concurrencia.
- Revision de codigo orientada a defectos y regresiones.
- Redaccion de OpenSpec, documentacion tecnica y notas de entrega.

El contexto principal procede del repositorio. El modelo debe leer las implementaciones y convenciones existentes antes de editar, y debe preferir cambios pequenos y verificables frente a reescrituras amplias.

## 8. Controles humanos y de calidad

La revision humana se aplica en los siguientes puntos:

- Priorizacion y alcance del producto.
- Aceptacion de propuestas OpenSpec.
- Eleccion entre alternativas arquitectonicas.
- Evaluacion visual y de usabilidad.
- Revision del diff y de cambios potencialmente destructivos.
- Aprobacion de commits, push, pull requests y releases.
- Gestion de credenciales y configuracion de servicios externos.

La salida de IA debe superar controles reproducibles. Dependiendo del cambio, se utilizan:

```bash
./gradlew qualityCheck
./gradlew test
./gradlew :app:android:assembleDebug
openspec validate <change> --strict
git diff --check
```

Tambien se ejecutan tests de modulos concretos, tests Desktop para logica compartida, tests instrumentados Android y recorridos manuales en emulador. La seleccion final depende de las capas afectadas.

Un cambio no se considera correcto porque el modelo indique que lo es. Debe compilar, satisfacer sus tests, respetar las reglas de arquitectura y conservar el comportamiento esperado.

## 9. Ejemplos de aplicacion en Carbura

### Arquitectura y producto

La IA ayudo a convertir el PRD en capacidades OpenSpec, separar Android como entregable funcional de la vision multiplataforma y mantener trazabilidad entre historias, contratos, tareas y codigo.

### Sincronizacion local-first

La implementacion de sincronizacion se trabajo mediante propuestas, tests de repositorios y validacion de escenarios offline. La revision humana acoto el alcance a una estrategia v0 asumible para el MVP y evito presentar capacidades futuras como ya implementadas.

### Cancelacion de corrutinas

El analisis asistido permitio localizar un caso donde la cancelacion normal del ciclo de vida se convertia en un error visible de sincronizacion. La solucion se amplio a una politica coherente de excepciones, tests adversariales y un guardrail arquitectonico. La decision final fue eliminar `runCatching` del codigo Kotlin y usar manejo explicito de cancelacion y errores.

### Refinado de UI Android

Las propuestas visuales y tecnicas se contrastaron con Material 3, accesibilidad y pruebas instrumentadas. Se ajustaron teclado, desplazamiento, acciones destructivas y gestos despues de comprobar el comportamiento real en Android.

### Documentacion

La IA ha ayudado a mantener PRD, specs, planes, decisiones y documentacion tecnica. La revision humana corrige afirmaciones obsoletas, elimina capacidades no verificadas y alinea la documentacion con el estado real del repositorio.

## 10. Ajustes humanos habituales

Los resultados generados se modifican cuando presentan alguno de estos problemas:

- Soluciones mas amplias de lo necesario.
- Abstracciones sin un caso de uso actual.
- Suposiciones que no coinciden con el repositorio.
- APIs o recomendaciones correspondientes a otra version del stack.
- Tratamiento incorrecto de cancelacion o concurrencia.
- UI generica, poco accesible o incoherente con el producto.
- Tests que verifican la implementacion en lugar del comportamiento.
- Documentacion que describe intenciones como funcionalidades terminadas.

La mejora antes/despues no se mide por cantidad de codigo generado, sino por la reduccion de ambiguedad, la cobertura de regresiones, la simplicidad del resultado y la evidencia de que funciona.

## 11. Seguridad, privacidad y limitaciones

- No se incluyen secretos, tokens, claves de Supabase ni credenciales OAuth en solicitudes a modelos o archivos versionados.
- Los valores sensibles permanecen en `local.properties` u otros mecanismos locales excluidos de Git.
- Las respuestas externas pueden estar desactualizadas, incompletas o ser incorrectas.
- El codigo generado puede compilar y aun contener defectos de comportamiento, seguridad o accesibilidad.
- Las recomendaciones se contrastan con documentacion oficial, tests y ejecucion real.
- La IA no realiza commits, push, publicaciones o cambios destructivos sin autorizacion humana explicita.

## 12. Evidencias y trazabilidad

El uso de IA y su resultado puede auditarse mediante artefactos verificables del repositorio:

- `openspec/changes/`: propuestas, decisiones y tareas en curso.
- `openspec/changes/archive/`: historial de cambios aceptados.
- `openspec/specs/`: comportamiento funcional consolidado.
- Tests unitarios, compartidos, Desktop e instrumentados Android.
- Historial Git y pull requests.
- Comandos de calidad y builds reproducibles.
- Documentacion de producto y arquitectura.

Esta trazabilidad sustituye un listado de prompts aislados por evidencia del proceso completo: necesidad, analisis, decision humana, implementacion, verificacion y resultado final. El procedimiento tecnico completo se mantiene en [`docs/toolchain/carbura_toolchain.md`](docs/toolchain/carbura_toolchain.md).
