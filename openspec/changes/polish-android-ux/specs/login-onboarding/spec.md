## ADDED Requirements

### Requirement: User-Facing Authentication Recovery
Android onboarding SHALL present actionable user-facing authentication errors without exposing raw exceptions, local configuration keys, or backend implementation details in release UI.

#### Scenario: Google sign-in fails
- **WHEN** Credential Manager or Supabase authentication fails
- **THEN** onboarding explains that sign-in could not complete and offers retry while technical detail remains outside primary release copy

### Requirement: Responsive And Accessible Onboarding
Android onboarding SHALL remain scrollable, width-constrained, and accessible when errors are long, the window is short, or font scale is increased.

#### Scenario: Authentication error appears with large text
- **WHEN** an asynchronous authentication error is shown at large font scale
- **THEN** the error is announced, remains readable, and does not hide the retry action
