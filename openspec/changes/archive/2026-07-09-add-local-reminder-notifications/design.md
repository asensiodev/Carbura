## Context

Carbura reminders are stored locally and synchronized through shared data code, but Android currently only displays them inside the app. For MVP, a local notification is enough to demonstrate that due-date reminders proactively alert the user.

Android notification delivery requires platform-specific APIs, permission handling on Android 13+, and a notification channel on Android 8+. Domain and feature code should not depend directly on Android APIs.

## Goals / Non-Goals

**Goals:**
- Schedule a local Android notification for a reminder with a due date.
- Cancel a scheduled notification when a reminder is completed or deleted.
- Cancel scheduled notifications associated with a vehicle when that vehicle is deleted.
- Request notification permission in Android when needed.
- Keep scheduling behind a common contract usable from shared presentation/data code.

**Non-Goals:**
- Remote push notifications.
- Recurring reminders.
- Exact-alarm permission flow.
- Background sync or server-driven notifications.
- Notifications for odometer-only reminders without a due date.

## Decisions

- Add a shared `ReminderNotificationScheduler` contract in domain so use cases/features can request scheduling without depending on Android APIs.
- Provide a no-op scheduler for non-Android targets to preserve KMP buildability.
- Implement Android scheduling using `AlarmManager` plus a `BroadcastReceiver` that posts a notification through `NotificationManagerCompat`.
- Use inexact alarms for MVP to avoid the Android exact alarm permission surface.
- Schedule at the reminder due date notification time derived locally; if the computed time is already past, schedule soon enough to be observable during smoke testing.
- Request `POST_NOTIFICATIONS` from the Android app shell on Android 13+ because posting notifications without it will be blocked.

## Risks / Trade-offs

- Inexact alarms may be delayed by the OS. Mitigation: acceptable for MVP, avoids extra exact-alarm permission complexity.
- Notifications require runtime permission on Android 13+. Mitigation: app shell requests permission and scheduler remains safe if denied.
- Device reboot clears alarms. Mitigation: out of scope for MVP; can be added later with boot receiver/reschedule.
