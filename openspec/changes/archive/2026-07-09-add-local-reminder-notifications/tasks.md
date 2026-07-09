## 1. Domain and DI

- [x] 1.1 Add a shared notification scheduling contract for reminder notifications.
- [x] 1.2 Provide Android and non-Android bindings without leaking Android APIs into feature/domain code.

## 2. Android Platform Notifications

- [x] 2.1 Add Android notification permission to the manifest.
- [x] 2.2 Request runtime notification permission on Android versions that require it.
- [x] 2.3 Create a reminder notification channel.
- [x] 2.4 Implement Android scheduler for date-based reminders.
- [x] 2.5 Implement notification receiver that posts reminder notifications.

## 3. Reminder Integration

- [x] 3.1 Schedule notification after creating a reminder with due date.
- [x] 3.2 Do not schedule notification for odometer-only reminders.
- [x] 3.3 Cancel notification when completing a reminder.
- [x] 3.4 Cancel notification when deleting a reminder.
- [x] 3.5 Cancel associated reminder notifications when deleting a vehicle.

## 4. Tests and Verification

- [x] 4.1 Add tests for scheduling/cancel behavior in the shared reminder repository flow.
- [x] 4.2 Add tests for vehicle deletion cancelling reminder notifications if practical at shared layer.
- [x] 4.3 Run targeted feature tests.
- [x] 4.4 Run Android build verification.
- [x] 4.5 Validate the OpenSpec change strictly.
