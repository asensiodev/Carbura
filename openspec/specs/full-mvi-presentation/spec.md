## ADDED Requirements

### Requirement: Full MVI Feature Contract
Feature presentation SHALL use explicit `UiState`, `Event` and `Effect` contracts.

#### Scenario: User input enters through events
- **WHEN** the UI receives user input
- **THEN** it forwards that input to the ViewModel as an event

#### Scenario: Persistent data is rendered from state
- **WHEN** the UI renders a feature screen
- **THEN** it reads persistent render data from immutable UI state

#### Scenario: One-off actions are emitted as effects
- **WHEN** the ViewModel needs to trigger a transient action
- **THEN** it emits an effect instead of storing that action as persistent state

### Requirement: MVI Contract File Separation
Feature MVI contracts SHALL be separated from the ViewModel implementation when the feature has more than one contract type.

#### Scenario: Feature defines state, events and effects
- **WHEN** a feature has `UiState`, `Event` and `Effect`
- **THEN** each contract is defined in its own file or clearly separated from the ViewModel implementation

### Requirement: Testable Coroutine Dispatching
Shared presentation and domain code SHALL use an injectable dispatcher provider when launching or switching coroutine contexts.

#### Scenario: ViewModel dispatchers are test controlled
- **WHEN** a shared ViewModel performs asynchronous work
- **THEN** tests can inject test dispatchers instead of relying on global coroutine dispatchers

### Requirement: Flow Testing With Turbine
Flow-based state or effects SHALL be tested with Turbine when assertions depend on emissions.

#### Scenario: One-off effect is emitted
- **WHEN** a ViewModel action emits an effect
- **THEN** the test observes the effect with Turbine and asserts the emitted item
