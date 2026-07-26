# user-input-casing Specification

## Purpose
Define preservation of user-entered display casing while keeping technical identifiers and canonical labels deterministic.

## Requirements
### Requirement: User-entered display text preserves casing
The system SHALL preserve meaningful capitalization and punctuation in user-entered display text after trimming surrounding whitespace.

#### Scenario: Custom maintenance label is saved
- **WHEN** a user saves `eBike ECU Check` as a custom maintenance type
- **THEN** Android, Desktop, editing, persistence, and synchronization retain and display `eBike ECU Check`

#### Scenario: Technical identity is normalized separately
- **WHEN** user-entered text requires a deterministic technical identifier
- **THEN** the identifier MAY be normalized but SHALL NOT replace the preserved display value

### Requirement: Canonical maintenance types use semantic labels
The system SHALL display canonical maintenance types from their localized type code rather than reconstructing text from technical IDs.

#### Scenario: ITV record is displayed
- **WHEN** a record has the canonical ITV type code
- **THEN** Android displays the localized `ITV` label and Desktop displays its canonical label

### Requirement: Legacy maintenance labels remain readable
The system SHALL support existing records that do not contain a preserved custom label.

#### Scenario: Legacy custom record is loaded
- **WHEN** a custom record has no stored label
- **THEN** the UI displays a readable humanized technical-ID fallback without failing

### Requirement: Unaffected user text remains case-preserving
The system SHALL continue preserving casing for vehicle names and plates, reminder titles, workshop names, notes, and profile display names.

#### Scenario: Mixed-case text is persisted
- **WHEN** a user saves mixed-case text in an unaffected display field
- **THEN** persistence and presentation retain that casing
