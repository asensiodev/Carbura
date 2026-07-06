## ADDED Requirements

### Requirement: Session-Aware App Startup
The app shell SHALL consume auth session state before selecting the authenticated Navigation 3 content route.

#### Scenario: Startup checks current session
- **WHEN** the Android app content is initialized
- **THEN** it checks `AuthGateway.currentSession()` through shared presentation logic before showing protected MVP content

#### Scenario: Session changes after sign out
- **WHEN** sign out completes
- **THEN** the app shell observes the unauthenticated state and clears protected navigation state
