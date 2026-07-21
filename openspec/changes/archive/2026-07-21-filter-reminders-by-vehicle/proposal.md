## Why

Families with multiple vehicles need a quick way to focus the reminders list without losing the urgency-oriented chronological overview. The reminders already carry vehicle identity, so filtering can improve navigation without changing persistence or synchronization.

## What Changes

- Add a horizontally scrolling row of rounded vehicle filter chips to the reminders list.
- Keep an exclusive `All` filter as the default.
- Allow multiple vehicle filters to be selected at once.
- Communicate selection through chip styling without checkmark icons.
- Preserve the existing due-date ordering within the filtered result.
- Show a filter-specific empty state when reminders exist but none match the selected vehicles.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `reminders-mvp`: Add presentation behavior for accessible multi-vehicle filtering of pending reminders.

## Impact

- Affects shared reminder presentation state, events, and ViewModel behavior.
- Affects the Android reminders list and its localized empty-state copy.
- Adds common presentation and Android Compose tests.
- Does not change reminder persistence, domain models, synchronization, or navigation.
