## Why

Carbura necesita completar el flujo principal del MVP: no basta con crear vehiculos, el usuario debe poder registrar mantenimientos y consultar el historial fiable de cada vehiculo. Este cambio aporta el siguiente vertical slice de valor y activa la navegacion real entre pantallas.

## What Changes

- Add maintenance history capability for a selected vehicle.
- Add an in-memory maintenance repository for the MVP slice.
- Add shared MVI presentation for vehicle maintenance history and creation.
- Add Android Compose UI to show empty history, maintenance form and record list.
- Wire garage vehicle selection to a vehicle detail/history route using the shared route model.
- Add tests for empty history, valid creation, validation errors and descending history ordering.

## Capabilities

### New Capabilities

- `maintenance-history`: Registro de mantenimientos por vehiculo y consulta de historial ordenado.

### Modified Capabilities

- `vehicle-management`: El garaje permite seleccionar un vehiculo para navegar a su historial.

## Impact

- `feature:maintenance`: shared MVI ViewModel, state/event/effect contracts, repository implementation and Android UI.
- `feature:garage`: vehicle selection event/effect and Android callback to navigate.
- `app:android`: route state between garage and vehicle maintenance screen.
- `app:shared`: route definitions may be reused or adjusted for vehicle history.
- Tests in `feature:maintenance` and existing garage tests as needed.
