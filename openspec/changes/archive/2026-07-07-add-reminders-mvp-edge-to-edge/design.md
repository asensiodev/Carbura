## Context

Carbura already has the Android MVP core loop for login, vehicle management, maintenance history, and local persistence. `core:model` already defines `Reminder`, `ReminderId`, and reminder due fields, while `core:domain` only exposes `ReminderRepository.saveReminder` for automatic reminder creation. `feature:reminders` is currently only a route constant, so the user-facing reminder slice is still missing.

The Friday delivery goal is to maximize product value without opening large infrastructure fronts. This change keeps reminders local-first, Android-first, and manually actionable. It also adds edge-to-edge support so the app presents correctly on modern Android devices.

## Goals / Non-Goals

**Goals:**
- Implement a minimal Android reminders screen using the existing shared MVI pattern.
- Persist reminders locally through `core:data` and SQLDelight.
- Allow manual reminder creation for a vehicle with due date and/or due odometer.
- Allow marking reminders as completed.
- Enable edge-to-edge rendering and safe system-bar insets on top-level Android screens.
- Keep the implementation small enough to complete and verify before the Friday MVP delivery.

**Non-Goals:**
- Local notifications or background scheduling.
- Remote reminder sync through Supabase.
- Recurring reminders or advanced reminder rules.
- Multi-family invitation/sharing workflows.
- Desktop implementation for this change.
- Full visual redesign.

## Decisions

### Keep Reminders Local-First For Friday

Use SQLDelight-backed local persistence, matching vehicles and maintenance records.

Alternatives considered:
- Supabase-first reminders: higher final-product value but would require RLS, sync semantics, conflict handling, and runtime testing risk.
- In-memory reminders: fastest but weaker product value because reminders disappear on restart.

### Extend The Existing Domain Contract Minimally

Add reminder read and completion operations to `ReminderRepository` rather than introducing a second repository abstraction. Add small use cases only where validation or intent is meaningful.

Expected operations:
- list pending reminders for a family or vehicle
- save/create reminder
- mark reminder completed

Alternatives considered:
- Put all reminder behavior directly in the ViewModel: faster but leaks business rules into presentation.
- Build a richer reminder service: unnecessary for the MVP slice.

### Use A Dedicated Reminders Feature MVI Slice

Implement `RemindersUiState`, `RemindersEvent`, `RemindersEffect`, `RemindersViewModel`, and Android `RemindersScreen`, following garage and maintenance patterns.

The first version should optimize for demo clarity:
- pending reminders list
- simple create form
- completed action per row
- empty state

### Navigation Should Be Simple

Expose reminders from the existing Android app navigation with minimal routing. If time is tight, a top-level reminders entry is preferable to deeply integrating reminder creation into every vehicle detail flow.

### Edge-To-Edge Should Be A Polish Pass, Not A Redesign

Enable Android edge-to-edge at the activity level and apply safe drawing insets to app content. Reuse existing spacing tokens and avoid raw `dp` in feature screens.

Alternatives considered:
- Redesign all screens: too broad for Friday.
- Ignore edge-to-edge: acceptable functionally, but weakens polish and can cause visual overlap on some devices.

## Risks / Trade-offs

- Reminder scope creep into notifications and scheduling -> keep notifications explicitly out of this change.
- Navigation complexity across vehicle-specific and global reminder views -> start with one reachable reminders screen and pass vehicle context only where already available.
- Date input validation can become UX-heavy -> use the existing `CalendarDate` string approach for MVP and show clear validation errors.
- Edge-to-edge can introduce content overlap -> verify login, garage, maintenance, and reminders on a device/emulator after applying insets.
- Local-only reminders are not shared across devices -> acceptable for Friday MVP, document as deferred.

## Migration Plan

- Add a local `reminders` SQLDelight table and queries.
- Map SQLDelight rows to `Reminder` in `core:data`.
- Register the local reminder repository in Koin.
- Add feature presentation and Android screen.
- Enable edge-to-edge and verify existing screens.
- Rollback is straightforward: remove navigation to reminders and disable edge-to-edge if a blocking UI regression appears.

## Open Questions

- Should the first reminders screen be top-level global pending reminders, vehicle-scoped, or both? Recommendation: top-level pending list plus vehicle selector only if easy with current data.
- Should completed reminders remain visible in this MVP? Recommendation: hide by default; completion is enough for Friday.
