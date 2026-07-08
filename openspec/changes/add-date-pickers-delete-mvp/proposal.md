# Add Date Pickers And Delete MVP

## Why

Manual date entry makes Android maintenance and reminder creation error-prone and adds avoidable validation friction. Users also need a safe way to remove mistaken maintenance records and reminders during MVP demos and real local-first usage.

## What Changes

- Use Android date picker UI for maintenance service dates and reminder due dates.
- Keep domain validation as a safety net, but avoid requiring users to type ISO dates manually on Android.
- Allow deleting maintenance records from a vehicle history with confirmation.
- Allow deleting pending reminders with confirmation.

## Impact

- Android presentation changes for maintenance and reminders.
- Domain repository contracts gain delete operations for maintenance records and reminders.
- Local SQLDelight repositories delete records locally for the MVP.
- Shared ViewModels expose delete events/effects and remain KMP-ready for Desktop.
