## ADDED Requirements

### Requirement: Desktop production presentation
The Desktop application SHALL present implemented workflows as production functionality and SHALL NOT display preview or migration-placeholder labeling in the final delivery.

#### Scenario: User opens the Desktop shell
- **WHEN** the final Desktop application starts
- **THEN** navigation and feature content contain no Desktop preview banner or supported-system promotional placeholder

### Requirement: Native package evidence
The final delivery SHALL generate each native Desktop package on a host that supplies the required `jpackage` tool and SHALL not equate a JVM jar with a native distribution.

#### Scenario: macOS package task runs with a full JDK
- **WHEN** the DMG packaging task executes on macOS
- **THEN** it produces an installable Carbura DMG for artifact inspection and manual acceptance

#### Scenario: Packaging tool is unavailable
- **WHEN** the selected JDK lacks `jpackage`
- **THEN** packaging remains incomplete and the delivery does not claim that a native artifact was verified
