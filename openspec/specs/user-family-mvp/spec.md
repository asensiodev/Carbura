## Purpose

Define the Android MVP user and family workspace summary shown to authenticated users.

## Requirements

### Requirement: Authenticated User Summary
The system SHALL show the authenticated user's profile summary in the Android `Usuario` tab.

#### Scenario: Profile has display name and email
- **WHEN** the authenticated user opens the `Usuario` tab and the profile contains display name and email
- **THEN** the screen shows both the display name and email

#### Scenario: Profile email is unavailable
- **WHEN** the authenticated user opens the `Usuario` tab and the profile email is unavailable
- **THEN** the screen shows the display name and omits the email row without blocking sign-out

### Requirement: Family Workspace Summary
The system SHALL show the authenticated user's current family workspace context in the Android `Usuario` tab.

#### Scenario: Family name is available
- **WHEN** the authenticated user opens the `Usuario` tab and the current family name is available
- **THEN** the screen shows the family name as the active workspace

#### Scenario: Family name is unavailable
- **WHEN** the authenticated user opens the `Usuario` tab and the family name is unavailable
- **THEN** the screen shows a generic personal family label without blocking the rest of the tab

### Requirement: Deferred Family Management Copy
The system SHALL communicate that family member and invitation management is not part of the current MVP.

#### Scenario: User views family management section
- **WHEN** the authenticated user views the family section
- **THEN** the screen shows that member invitations will be available in a later version

### Requirement: Sign Out Remains Available
The system SHALL keep sign-out available from the Android `Usuario` tab.

#### Scenario: User signs out from profile tab
- **WHEN** the authenticated user taps the sign-out action in the `Usuario` tab
- **THEN** the app signs out and returns to the unauthenticated onboarding flow
