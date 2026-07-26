## ADDED Requirements

### Requirement: Vehicle Planning Fields
The shared vehicle model and local and remote vehicle records SHALL support nullable next ITV date, insurance renewal date, and next service odometer fields.

#### Scenario: Existing vehicle has no planning fields
- **WHEN** an existing vehicle created before the planning-field migration is loaded
- **THEN** its new planning fields are represented as empty without losing existing vehicle data

#### Scenario: Vehicle planning fields round trip
- **WHEN** a vehicle with planning fields is saved and loaded
- **THEN** all provided due targets retain their typed values
