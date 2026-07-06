## 1. Persistence Setup

- [x] 1.1 Add SQLDelight Gradle plugin, runtime, Android driver and JVM test driver dependencies.
- [x] 1.2 Configure `core:data` with SQLDelight schema generation for common repositories and Android driver wiring.
- [x] 1.3 Define database tables and queries for vehicles and maintenance records.

## 2. Repository Implementation

- [x] 2.1 Implement persisted vehicle repository in `core:data` with domain model mapping.
- [x] 2.2 Implement persisted maintenance repository in `core:data` with date-descending history reads.
- [x] 2.3 Add repository tests proving vehicles and maintenance records persist through a recreated repository/database instance.
- [x] 2.4 Move production Koin repository bindings from feature modules into `core:data`.

## 3. Navigation 3 Host

- [x] 3.1 Replace manual `CarburaRoute` state switching in `MainActivity` with a Navigation 3 back stack host.
- [x] 3.2 Wire garage vehicle selection to push vehicle history route and history back action to pop the stack.
- [x] 3.3 Keep feature screens navigation-agnostic by preserving callback/effect boundaries.

## 4. Cleanup And Verification

- [x] 4.1 Remove production in-memory repository implementations from feature modules or move them to tests if still needed.
- [x] 4.2 Run `./gradlew test`.
- [x] 4.3 Run `./gradlew assembleDebug`.
- [x] 4.4 Run `git diff --check` and inspect working tree status.
