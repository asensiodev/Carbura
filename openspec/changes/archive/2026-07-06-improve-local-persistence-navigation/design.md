## Context

Garage vehicles are currently persisted by `feature:garage` through `InMemoryVehicleRepository`, and maintenance records are persisted by `feature:maintenance` through `InMemoryMaintenanceRecordRepository`. This keeps the MVP simple but violates the intended architecture because data implementations live in feature modules and all user-created data is lost after process death.

The app already defines typed route keys in shared code using Navigation 3 `NavKey`, but Android rendering still uses a manual `mutableStateOf(CarburaRoute.Garage)` switch in `MainActivity`. That blocks natural back-stack behavior and will become brittle as reminders and future detail screens are added.

## Goals / Non-Goals

**Goals:**
- Move concrete repository implementations into `core:data` behind domain contracts.
- Persist Android MVP vehicle and maintenance records locally across app restarts.
- Keep domain and ViewModels platform-agnostic.
- Replace manual navigation state with a typed Navigation 3 back stack and entry provider.
- Preserve existing garage and maintenance user behavior.

**Non-Goals:**
- Supabase synchronization or auth-scoped remote persistence.
- Full offline conflict resolution or multi-user data separation beyond the existing MVP family id.
- Desktop/iOS persistence wiring in this change.
- Deep links, bottom navigation, or a complete app-wide navigation graph beyond current MVP screens.

## Decisions

### Use SQLDelight for local persistence

SQLDelight is the preferred storage dependency because it is KMP-friendly, type-safe, testable on JVM, and can support Android-first now while preserving future Desktop/iOS paths. Room is Android-centric and would force either platform-specific abstractions immediately or duplicated persistence later.

The first schema will include `VehicleEntity` and `MaintenanceRecordEntity` tables. Domain repositories will map between SQL rows and `core:model` value objects in `core:data`.

### Keep repository contracts in `core:domain`

The existing `VehicleRepository` and `MaintenanceRecordRepository` contracts remain the boundary. Features continue receiving repositories/use cases through Koin and do not depend on SQLDelight or Android database APIs.

### Register persistence from `core:data`

`dataModule` will own local storage dependencies and bind `VehicleRepository` and `MaintenanceRecordRepository`. Feature modules should stop binding in-memory repositories in production DI. Tests can keep fake or in-memory repositories where useful.

### Android driver first, common database API

Common code will define database/repository behavior; Android will provide the SQL driver through platform-specific `androidMain` code. Desktop/iOS drivers are deferred.

### Navigation 3 host in Android app boundary

Route keys remain in `app:shared`. Android `MainActivity` will own the Navigation 3 back stack and render entries for current Android screens. Feature screens keep callback-based navigation outputs (`onVehicleSelected`, `onBack`) so they remain independent from Navigation 3.

## Risks / Trade-offs

- SQLDelight plugin setup can require convention changes → keep the plugin configuration minimal and only apply it where needed.
- Persisted schema introduces migration responsibility → start with schema version 1 and no destructive migration code beyond development defaults.
- Existing tests may rely on feature-local in-memory repositories → keep test-only construction explicit and avoid production bindings in feature modules.
- Navigation 3 API details may differ from examples → implement the smallest supported host pattern with current dependencies and verify through `assembleDebug`.
