## 1. Desktop Planning Forms

- [x] 1.1 Add optional ITV date, insurance renewal date, and next service odometer controls to Desktop vehicle creation.
- [x] 1.2 Add the same planning controls to Desktop vehicle editing with persisted values prefilled.
- [x] 1.3 Render shared validation and mutation-disabled states for the planning fields.

## 2. Confirmation Workflows

- [x] 2.1 Render vehicle reminder suggestions from shared state in a Desktop confirmation dialog.
- [x] 2.2 Wire save-only and save-with-reminders actions to existing `VehicleFormEvent` contracts and require an explicit decision.
- [x] 2.3 Preserve accurate Desktop notification-availability disclosure for generated date reminders.

## 3. Quick Odometer Workflow

- [x] 3.1 Add a quick-odometer action to Desktop vehicle cards and render its focused input dialog.
- [x] 3.2 Wire quick-update validation and success feedback through shared state and effects.
- [x] 3.3 Render old and proposed values in the odometer-decrease confirmation and wire confirm/cancel actions.

## 4. Verification

- [x] 4.1 Extend Desktop integration tests for planning-field persistence and generated-reminder reconciliation.
- [x] 4.2 Extend Desktop integration tests for quick odometer increases and confirmed decreases.
- [x] 4.3 Run Desktop tests, repository quality checks, and the CI-equivalent build command.
