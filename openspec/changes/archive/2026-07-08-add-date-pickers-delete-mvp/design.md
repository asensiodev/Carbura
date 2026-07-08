# Design

## Approach

- Keep date handling in shared ViewModels as ISO `YYYY-MM-DD` strings so Android and future Desktop can reuse the same KMP state and domain validation.
- Use Android Material date pickers only in Android presentation to avoid manual date typing on mobile.
- Keep domain validation for dates and non-negative numeric fields as a safety net.
- Add delete use cases and repository methods in shared domain contracts so Android and future Desktop use the same behavior.
- Use local SQLDelight hard deletes for the MVP local-first storage. Sync v0 can later map deletion semantics to remote delete or soft-delete without changing presentation events.

## UX

- Delete actions require confirmation.
- Confirmation dialogs identify the target with a separate semibold text line to avoid interpolated styled strings and keep localization straightforward.
- Delete icons use the error color consistently across Garaje, Mantenimiento and Recordatorios.
