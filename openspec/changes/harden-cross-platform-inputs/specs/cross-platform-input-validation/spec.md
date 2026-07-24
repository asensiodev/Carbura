## ADDED Requirements

### Requirement: Numeric input is exact and bounded
Carbura SHALL reject malformed, non-finite, negative, overflowing, or over-precision numeric input instead of truncating it or silently removing the entered value.

#### Scenario: Maintenance cost has excess precision
- **WHEN** a user submits a maintenance cost with more than two decimal places
- **THEN** the cost field remains unchanged and exposes validation feedback without persisting the record

#### Scenario: Numeric target is malformed
- **WHEN** a reminder or vehicle odometer contains non-digit, decimal, negative, or overflowing input
- **THEN** the responsible field exposes validation feedback and no mutation is dispatched

#### Scenario: Optional numeric field is blank
- **WHEN** an optional numeric field is blank and all other required values are valid
- **THEN** Carbura treats that optional value as absent rather than invalid

### Requirement: Calendar input is valid and canonical
Carbura SHALL accept only real calendar dates, SHALL retain ISO-8601 values in state and persistence, and SHALL present dates to users in the active locale.

#### Scenario: Impossible date is submitted
- **WHEN** input represents a syntactically plausible but impossible date such as 31 February
- **THEN** the responsible date field exposes validation feedback and no mutation is dispatched

#### Scenario: Persisted date is displayed
- **WHEN** a valid persisted date is rendered in Android or Desktop read-only content
- **THEN** it is formatted according to the active user locale without changing the stored ISO value

#### Scenario: User selects a date
- **WHEN** a user chooses a date from a platform calendar control
- **THEN** the field state receives the corresponding canonical ISO-8601 date

### Requirement: Validation identifies the responsible field
Form validation SHALL associate recoverable input failures with the field that requires correction and SHALL not misclassify them as persistence failures.

#### Scenario: One of two dates is invalid
- **WHEN** only one date field fails validation
- **THEN** only that field exposes error state and supporting guidance

#### Scenario: Persistence fails after valid input
- **WHEN** all input is valid but storage fails
- **THEN** the form preserves the draft and presents a persistence-level retry message

### Requirement: Draft cancellation is explicit
Carbura SHALL clear abandoned create drafts on explicit cancellation and SHALL protect unsaved dirty edits from accidental dismissal.

#### Scenario: Create form is cancelled
- **WHEN** the user explicitly cancels a create form and later opens it again
- **THEN** the form starts with clean default values and no previous validation or persistence error

#### Scenario: Dirty edit is dismissed
- **WHEN** the user attempts to dismiss an edit containing unsaved changes
- **THEN** Carbura requires confirmation before discarding those changes
