# Desktop Deferred For Entrega 2

Desktop remains part of Carbura's architecture, but it is deferred for Entrega 2 to keep the Android MVP stable.

Reason:

- Entrega 2 prioritizes an Android MVP with backend and database connected.
- Desktop should not block Gradle setup, Android build, or feature delivery.
- Shared `core:*`, `feature:*` and `app:shared` modules are prepared so Desktop can be added later with a thin `app:desktop` shell.

Revisit condition:

- Android build remains stable after Entrega 2.
- Vehicle, maintenance, reminders, auth and sync flows are functionally closed.
- Entrega final scope has room for Desktop without risking CI/release/E2E evidence.
