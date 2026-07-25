## Context

Carbura shares form state and domain use cases across Android and Desktop but each platform owns its controls. Numeric strings are currently converted independently at submission boundaries, Desktop dates are free text, Android picker state can outlive the displayed value, and form containers do not always adapt to constrained space. Account Desktop also places an account card and a wider technical storage card in one rigid row.

## Goals / Non-Goals

**Goals:**
- Centralize strict parsing semantics for money, non-negative odometers, and ISO calendar dates.
- Preserve typed domain failures and field-specific presentation feedback.
- Use platform-appropriate calendar controls while storing canonical ISO-8601 dates.
- Make Android and Desktop form actions reachable under compact, large-text, and keyboard conditions.
- Make explicit cancellation reset create drafts and protect dirty edits.
- Prioritize account actions while retaining discoverable local storage details.

**Non-Goals:**
- Change persisted date or money representations.
- Add locale-dependent free-text parsing.
- Add native Desktop notifications or modify sync behavior.
- Redesign unrelated Garage, Maintenance, or Reminders list content.

## Decisions

1. **Parse at shared presentation/domain boundaries with exact decimal semantics.** Money accepts at most two decimal places and is converted without floating-point arithmetic. Odometers accept digits-only non-negative integers within model bounds. Alternatives such as filtering keystrokes alone are insufficient because paste, accessibility, tests, and Desktop input can bypass keyboard hints.

2. **Keep ISO-8601 as canonical state and localize only presentation.** Existing persistence and synchronization remain unchanged. Display-only labels use the current locale, while editable state is updated through calendar selection rather than locale-dependent text parsing.

3. **Use Material date pickers on Android and an integrated calendar dialog on Desktop.** Android picker state is keyed or synchronized to the current field value. Desktop date fields become read-only calendar launchers with clear actions, avoiding impossible dates and format ambiguity without adding an external dependency.

4. **Use responsive composition rather than fixed paired rows.** Paired Desktop fields stack when available width is constrained. Dialog footers allow actions to reflow, and the application enforces a practical minimum window size.

5. **Treat Cancel as abandonment for create forms and as guarded dismissal for dirty edits.** Create cancellation clears draft values and errors. Dirty edits request confirmation; active mutations remain non-dismissible.

6. **Keep storage capability but demote its technical detail.** The synchronized account card uses full available width. Storage becomes a compact secondary full-width section with an open-folder action; exact paths remain available behind explicit details disclosure to satisfy diagnostics and backup needs.

## Risks / Trade-offs

- [Existing drafts may previously have survived Cancel] → Reset only on explicit cancellation, not recomposition, refresh, or transient navigation.
- [Calendar controls increase Desktop UI code] → Reuse one internal date-field/calendar component without a new dependency.
- [Stricter parsing rejects values previously truncated] → Preserve the raw input and show field feedback so users can correct it.
- [Responsive thresholds can vary by font metrics] → Cover narrow widths and enlarged text with deterministic layout tests and manual Desktop inspection.
- [Exact paths are less immediately visible] → Keep them available through a details affordance and retain the direct open-folder action.
