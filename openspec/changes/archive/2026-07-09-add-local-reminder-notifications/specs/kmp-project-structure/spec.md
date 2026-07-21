## ADDED Requirements

### Requirement: Notification Integrations Use Common Contracts
The system SHALL keep notification scheduling requests behind shared contracts with platform-specific implementations.

#### Scenario: Reminder feature avoids Android notification APIs
- **WHEN** a developer reviews shared reminder presentation and domain code
- **THEN** it does not depend directly on Android notification, alarm, permission, or broadcast APIs

#### Scenario: Non-Android targets remain buildable
- **WHEN** non-Android source sets compile
- **THEN** notification scheduling has a non-Android implementation that does not require Android APIs
