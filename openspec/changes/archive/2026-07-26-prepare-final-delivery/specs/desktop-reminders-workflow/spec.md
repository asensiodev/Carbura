## MODIFIED Requirements

### Requirement: Desktop notification capability is explicit
The Desktop Reminders workspace SHALL provide persistent synchronized reminder management without native Desktop alerts, because native operating-system notification delivery is a mobile-only Carbura capability.

#### Scenario: User views reminder capability information
- **WHEN** the user opens the Desktop Reminders workspace
- **THEN** the interface states that reminders are stored and synchronized while native alerts are delivered only by the mobile app

#### Scenario: Desktop creates a reminder for a synchronized family
- **WHEN** the Desktop user saves a valid reminder and synchronization succeeds
- **THEN** the reminder becomes available to Android for mobile notification scheduling
