## Why

Current Android and Desktop forms accept or misclassify several malformed numeric and date values, while some cancellation, keyboard, and narrow-window behaviors can hide actions or silently retain unintended state. Desktop Account also gives technical storage details more prominence than account actions and can clip sign-out controls.

## What Changes

- Enforce finite, bounded, non-negative, precision-safe numeric input for odometers and maintenance costs.
- Reject malformed reminder targets instead of silently dropping invalid values.
- Validate impossible calendar dates and report errors against the responsible field.
- Make Android date-picker state follow the displayed value and keep primary actions reachable with the keyboard open.
- Provide user-friendly calendar selection on Desktop while preserving localized date display.
- Make Desktop forms adapt to narrow windows and enlarged text without clipping fields or actions.
- Reset abandoned create drafts deliberately and protect dirty edit dismissal consistently.
- Rework Desktop Account so account actions fit, while local storage remains available as a compact secondary capability.
- Add regression tests for malformed, negative, overflowing, stale, and layout-sensitive input cases.

## Capabilities

### New Capabilities
- `cross-platform-input-validation`: Defines accepted numeric and calendar input, localized date presentation, field-level feedback, and explicit draft lifecycle across Android and Desktop.

### Modified Capabilities
- `android-ux-resilience`: Keep date selection synchronized with field state and primary form actions reachable under keyboard and large-text constraints.
- `desktop-form-dialogs`: Adapt dialog fields and actions to narrow windows, large text, and guarded dirty-form dismissal.
- `desktop-local-account`: Prioritize synchronized account identity and actions while retaining local storage access as secondary information.

## Impact

- Shared maintenance, reminder, and vehicle presentation/domain validation.
- Android Garage, Maintenance, and Reminders forms and date pickers.
- Desktop Garage, Maintenance, Reminders, form-dialog, and Account composables.
- Localized strings and cross-platform regression tests.
- No persistence schema, Supabase API, or synchronization protocol changes.
