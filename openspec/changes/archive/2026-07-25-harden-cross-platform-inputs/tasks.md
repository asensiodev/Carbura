## 1. Shared Input Validation

- [x] 1.1 Add regression tests for negative, malformed, non-finite, overflowing, and excess-precision numeric input
- [x] 1.2 Implement exact maintenance cost parsing without floating-point truncation
- [x] 1.3 Reject malformed optional odometer input instead of silently treating it as absent
- [x] 1.4 Classify impossible calendar values as field validation failures
- [x] 1.5 Add field-specific validation state where one shared error currently marks multiple controls

## 2. Android Form Resilience

- [x] 2.1 Synchronize Garage, Maintenance, and Reminders date-picker state with each current field value
- [x] 2.2 Keep primary save actions reachable while the software keyboard is visible
- [x] 2.3 Add explicit create cancellation reset and preserve dirty-edit confirmation behavior
- [x] 2.4 Add Android tests for cleared dates, malformed pasted values, negative values, keyboard reachability, and field error semantics

## 3. Desktop Date And Form UX

- [x] 3.1 Add a reusable integrated Desktop calendar field with localized display and canonical ISO state
- [x] 3.2 Replace free-text date entry in Garage, Maintenance, and Reminders with calendar selection
- [x] 3.3 Make paired fields and dialog footer actions adapt to constrained width and enlarged text
- [x] 3.4 Add create reset and dirty-edit dismissal protection to Desktop forms
- [x] 3.5 Add Desktop tests for date formatting, calendar selection, narrow layouts, and draft lifecycle

## 4. Desktop Account Layout

- [x] 4.1 Make synchronized account identity and actions a responsive full-width primary card
- [x] 4.2 Convert local storage into a compact secondary section with optional exact-path details
- [x] 4.3 Add layout and behavior tests for constrained account actions and storage details

## 5. Verification

- [x] 5.1 Run affected shared, Android, Desktop, and instrumented tests
- [x] 5.2 Run `qualityCheck`, `test`, `assembleDebug`, Desktop JAR, strict OpenSpec validation, and `git diff --check`
- [x] 5.3 Review all local changes with the user without committing or pushing
