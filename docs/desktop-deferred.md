# Desktop Deferred For Entrega 2

Desktop remains part of Carbura's architecture, but it is deferred during `bootstrap-kmp-project` to keep the Android scaffold stable.

Reason:

- Entrega 2 prioritizes an Android MVP with backend and database connected.
- Desktop should not block Gradle setup, Android build, or feature delivery.
- Shared `core:*` and `feature:*` modules are prepared so Desktop can be added later with a thin `app:desktop` shell.

Revisit condition:

- Android build is stable.
- Vehicle and maintenance flows are functional.
- There is time left in the 6-10 July buffer.
