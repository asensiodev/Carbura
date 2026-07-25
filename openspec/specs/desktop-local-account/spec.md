# desktop-local-account Specification

## Purpose
Define the Desktop account workspace, its local-mode disclosure, storage visibility, responsive actions, and safe native integrations.

## Requirements
### Requirement: Dedicated Desktop Account Workspace
The Desktop app SHALL render a dedicated Account workspace instead of generic migration placeholder content.

#### Scenario: Open Account destination
- **WHEN** the Desktop user selects Account in the application shell
- **THEN** the app displays the local Account workspace

### Requirement: Local Mode Disclosure
The Desktop Account workspace SHALL state that Desktop data is stored locally without sign-in or cloud synchronization.

#### Scenario: Review current Desktop mode
- **WHEN** the Desktop Account workspace is visible
- **THEN** the user can distinguish local mode from an authenticated synchronized account

### Requirement: Storage Location Visibility
The Desktop Account workspace SHALL keep the exact application data directory and database file path available through explicit storage details without giving technical paths equal prominence to account identity and actions.

#### Scenario: Review local storage summary
- **WHEN** the Desktop Account workspace is visible
- **THEN** the user sees a compact explanation that Carbura stores data on this computer and can open the data folder

#### Scenario: Review exact local storage paths
- **WHEN** the user requests storage details
- **THEN** the displayed directory and database path match the SQLDelight driver's configured location

### Requirement: Account actions have responsive priority
The Desktop Account workspace SHALL prioritize synchronized account identity and actions over secondary storage diagnostics.

#### Scenario: Authenticated Account is shown at constrained width
- **WHEN** the Account workspace is narrower than the combined width of account actions
- **THEN** Sync and Sign out remain fully visible and usable without label clipping

#### Scenario: Account and storage are shown together
- **WHEN** an authenticated account and local storage information are both available
- **THEN** the account occupies the primary full-width region and storage appears as a compact secondary section

### Requirement: Safe Native Platform Actions
The Desktop Account workspace SHALL allow the user to request opening the local data directory and the Carbura project website through supported operating-system actions.

#### Scenario: Open supported data directory
- **WHEN** the user requests the data folder and the operating system supports folder opening
- **THEN** the app delegates the exact application data directory to the native platform action

#### Scenario: Open supported project website
- **WHEN** the user requests the project website and the operating system supports URI browsing
- **THEN** the app delegates the Carbura HTTPS project URI to the native platform action

#### Scenario: Native action unavailable
- **WHEN** a requested native action is unsupported or fails
- **THEN** the Account workspace remains available and shows user-visible failure feedback

### Requirement: Non-Destructive Local Account Scope
The Desktop Account workspace MUST NOT expose local data deletion, reset, or simulated authentication controls.

#### Scenario: Review Account controls
- **WHEN** the user reviews the local Account workspace
- **THEN** all available controls preserve local application data and accurately represent current capabilities
