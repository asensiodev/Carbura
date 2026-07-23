## Context

Garage, Reminders, and Maintenance use Compose Desktop `DialogWindow` for their primary forms. Those windows inherit a platform default size while the form surfaces impose only a fixed width, so long forms can extend below the viewport and expose more content only after manual window resizing.

## Goals / Non-Goals

**Goals:**
- Keep transactional forms inside the main application window.
- Ensure every field remains reachable without resizing the application.
- Keep titles and primary actions visible while long content scrolls.
- Preserve each workflow's mutation and dismissal guards.
- Use one reusable modal layout across Desktop workspaces.

**Non-Goals:**
- Redesigning field order or changing validation behavior.
- Replacing short confirmation alerts.
- Changing Android dialogs.
- Adding independent multi-window editing.

## Decisions

### Use Compose in-app `Dialog`

The reusable form container will use Compose `Dialog`, which renders a modal overlay attached to the current application window. Native `DialogWindow` was rejected because these forms are short-lived transactions rather than independent documents and because native default sizing caused the defect.

### Constrain the modal surface

The modal surface will use a stable preferred width with `widthIn` rather than an unconditional fixed window size. Its height will be bounded to the parent window, allowing the overlay to fit smaller Desktop layouts.

### Separate fixed chrome from scrolling content

The container will expose title, body, and footer slots. Only the body will use vertical scrolling; the title and Cancel/Save footer remain visible. This prevents validation errors or additional planning fields from pushing actions outside the reachable area.

### Preserve close guards at the boundary

The container receives `onDismissRequest` and a dismissal-enabled flag. Escape, outside click, and close actions will respect active mutations exactly as existing native-window close handlers do.

## Risks / Trade-offs

- [Risk] A shared modal abstraction becomes overly generic. → Keep only width, title, body, and footer behavior in the shared component.
- [Risk] Nested scroll containers can create confusing wheel behavior. → Use one vertical scroll state in each form body and no scrolling in the modal shell.
- [Risk] Very small windows may still constrain the form. → Apply safe outer padding and a maximum modal height derived from available constraints.
- [Trade-off] Users cannot move a form to another monitor independently. → This is intentional for transactional forms that depend on the current workspace.

## Migration Plan

1. Add the reusable in-app form dialog component.
2. Migrate vehicle, reminder, and maintenance forms without changing event wiring.
3. Remove obsolete native `DialogWindow` imports.
4. Add layout contract tests and run Desktop plus full-project verification.

Rollback is a source-only revert with no data migration.

## Open Questions

None.
