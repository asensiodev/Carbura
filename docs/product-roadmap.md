# Hoja de ruta de producto de Carbura

Este documento recoge mejoras de producto que tienen sentido, pero quedan fuera de Entrega 2 para proteger estabilidad.

## Recordatorios Proactivos Por Vehiculo

**Estado:** implementados en Android para objetivos opcionales de ITV, seguro y revision por kilometraje, con confirmacion del usuario, IDs estables, reconciliacion y prevencion de duplicados.

### Problema

El flujo actual permite crear recordatorios custom, pero el usuario debe saber que necesita crear uno para ITV, seguro o revisiones. Eso da flexibilidad, pero es menos proactivo de lo que Carbura puede ser.

### Direccion Propuesta

El alta y la edicion de vehiculo permiten capturar datos clave:

- Fecha de ITV.
- Fecha de renovacion del seguro.
- Kilometraje actual.
- Intervalos habituales de mantenimiento si el usuario quiere configurarlos.

Con esos datos, Carbura podria generar recordatorios sugeridos automaticamente y pedir permiso para avisar al usuario en contexto.

### Experiencia Deseada

1. El usuario anade un vehiculo.
2. La app pregunta datos utiles opcionales: ITV, seguro, kilometros.
3. Carbura propone avisos automaticamente.
4. Si el usuario acepta los avisos con fecha, se programan notificaciones locales.
5. Los recordatorios custom siguen existiendo para necesidades no previstas.

### Evolucion Pendiente

- Integrar `nextDueDate` en el formulario de mantenimiento para generar el recordatorio asociado a una ITV o seguro registrado.
- Evaluar intervalos configurables sin convertirlos en campos obligatorios.
- Mantener diferenciados los recordatorios sugeridos y los personalizados.

## Notificaciones Y Navegacion

Las notificaciones locales deben abrir la app en un contexto util. Para recordatorios, el destino natural es la pantalla de Recordatorios.

En el MVP parcial basta con abrir Recordatorios. Mas adelante se podria navegar al detalle del recordatorio si existe pantalla detalle.

## Sync De Arranque

Al restaurar una sesion existente en un dispositivo sin datos locales, la app debe sincronizar antes de mostrar las pantallas principales. Si no, la UI puede cargar listas vacias y requerir una sincronizacion manual posterior.

La estrategia actual de Entrega 2 es:

- Restaurar sesion.
- Resolver perfil/familia.
- Ejecutar primera sync.
- Mostrar la app principal.
- Si la sync falla, mostrar la app con datos locales y error no bloqueante.
