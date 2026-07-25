## MODIFIED Requirements

### Requirement: Storage Location Visibility
The Desktop Account workspace SHALL keep the exact application data directory and database file path available through explicit storage details without giving technical paths equal prominence to account identity and actions.

#### Scenario: Review local storage summary
- **WHEN** the Desktop Account workspace is visible
- **THEN** the user sees a compact explanation that Carbura stores data on this computer and can open the data folder

#### Scenario: Review exact local storage paths
- **WHEN** the user requests storage details
- **THEN** the displayed directory and database path match the SQLDelight driver's configured location

## ADDED Requirements

### Requirement: Account actions have responsive priority
The Desktop Account workspace SHALL prioritize synchronized account identity and actions over secondary storage diagnostics.

#### Scenario: Authenticated Account is shown at constrained width
- **WHEN** the Account workspace is narrower than the combined width of account actions
- **THEN** Sync and Sign out remain fully visible and usable without label clipping

#### Scenario: Account and storage are shown together
- **WHEN** an authenticated account and local storage information are both available
- **THEN** the account occupies the primary full-width region and storage appears as a compact secondary section
