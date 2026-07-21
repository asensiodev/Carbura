## MODIFIED Requirements

### Requirement: Maintenance Record Creation Form
The system SHALL allow the Android user to create a maintenance record for the selected vehicle using a canonical maintenance type, picker-selected performed date, optional next due date for ITV or insurance, and the remaining MVP fields supported by the domain. A Custom type SHALL retain user-entered maintenance labels.

#### Scenario: Valid canonical maintenance creation
- **WHEN** the user submits a canonical maintenance record with picker-selected valid date and non-negative odometer value
- **THEN** the canonical maintenance type code is saved through the domain maintenance creation use case and the record appears in the history

#### Scenario: ITV next due date
- **WHEN** the user selects ITV and supplies a picker-selected next ITV date
- **THEN** the date is stored as `nextDueDate` and maintenance-generated reminder orchestration runs

#### Scenario: Insurance next due date
- **WHEN** the user selects insurance and supplies a picker-selected renewal date
- **THEN** the date is stored as `nextDueDate` and maintenance-generated reminder orchestration runs

#### Scenario: Blank custom maintenance type error
- **WHEN** the user selects Custom and submits without a custom type label
- **THEN** the screen shows a validation error and the record is not added to the history

#### Scenario: Invalid maintenance odometer error
- **WHEN** the user submits a maintenance record with a negative odometer value
- **THEN** the screen shows a validation error and the record is not added to the history

#### Scenario: Optional cost display
- **WHEN** the user submits a valid maintenance record with cost
- **THEN** the saved record shows the cost in the history list

#### Scenario: Successful automatic reminder feedback
- **WHEN** maintenance creation also creates a generated reminder
- **THEN** Android confirms that both maintenance and its reminder were created

## ADDED Requirements

### Requirement: Maintenance Reminder Source Cleanup
Deleting a maintenance record SHALL also delete only the deterministic reminder generated from that source record and cancel its local alerts.

#### Scenario: Delete maintenance with generated reminder
- **WHEN** the user confirms deletion of an ITV or insurance record that generated a reminder
- **THEN** the record and generated reminder are retained as synchronization tombstones, their active UI entries disappear, and the generated reminder alerts are canceled
