# Carbura Product Roadmap

Este documento recoge mejoras de producto que tienen sentido, pero quedan fuera del MVP parcial actual para proteger estabilidad.

## Recordatorios Proactivos Por Vehiculo

### Problema

El flujo actual permite crear recordatorios custom, pero el usuario debe saber que necesita crear uno para ITV, seguro o revisiones. Eso da flexibilidad, pero es menos proactivo de lo que Carbura puede ser.

### Direccion Propuesta

En una iteracion posterior, el alta o edicion de vehiculo deberia permitir capturar datos clave:

- Fecha de ITV.
- Fecha de renovacion del seguro.
- Kilometraje actual.
- Intervalos habituales de mantenimiento si el usuario quiere configurarlos.

Con esos datos, Carbura podria generar recordatorios sugeridos automaticamente y pedir permiso para avisar al usuario en contexto.

### Experiencia Deseada

1. El usuario anade un vehiculo.
2. La app pregunta datos utiles opcionales: ITV, seguro, kilometros.
3. Carbura propone avisos automaticamente.
4. Si el usuario activa avisos, se programan notificaciones locales.
5. Los recordatorios custom siguen existiendo para necesidades no previstas.

### Por Que No En El MVP Parcial

- Requiere ampliar formularios, dominio y sync.
- Necesita un modelo claro para recordatorios generados vs custom.
- Conviene pensar bien edicion de vehiculo antes de meter campos obligatorios.
- El MVP actual ya demuestra el ciclo basico: vehiculos, mantenimiento, recordatorios, notificaciones y sync.

## Notificaciones Y Navegacion

Las notificaciones locales deben abrir la app en un contexto util. Para recordatorios, el destino natural es la pantalla de Recordatorios.

En el MVP parcial basta con abrir Recordatorios. Mas adelante se podria navegar al detalle del recordatorio si existe pantalla detalle.

## Sync De Arranque

Al restaurar una sesion existente en un dispositivo sin datos locales, la app debe sincronizar antes de mostrar las pantallas principales. Si no, la UI puede cargar listas vacias y requerir una sincronizacion manual posterior.

La estrategia actual debe ser:

- Restaurar sesion.
- Resolver perfil/familia.
- Ejecutar primera sync.
- Mostrar la app principal.
- Si la sync falla, mostrar la app con datos locales y error no bloqueante.
