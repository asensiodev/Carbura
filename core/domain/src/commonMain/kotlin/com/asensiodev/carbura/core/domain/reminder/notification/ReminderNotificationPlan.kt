package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId

enum class ReminderAlertKind {
    Manual,
    Itv60Days,
    Itv30Days,
    Itv7Days,
    Insurance45Days,
    Insurance37Days,
    Insurance7Days,
}

data class ReminderAlert(
    val kind: ReminderAlertKind,
    val daysBefore: Int,
)

data class ReminderNotificationPlan(
    val reminder: Reminder,
    val alerts: List<ReminderAlert>,
    val revision: NotificationRevision? = null,
) {
    init {
        require(alerts.map(ReminderAlert::kind).distinct().size == alerts.size) {
            "Reminder alert kinds must be unique within a plan"
        }
    }
}

fun manualReminderNotificationPlan(reminder: Reminder): ReminderNotificationPlan =
    ReminderNotificationPlan(
        reminder = reminder,
        alerts = listOf(ReminderAlert(ReminderAlertKind.Manual, reminder.notifyDaysBefore)),
    )

fun maintenanceReminderNotificationPlan(
    reminder: Reminder,
    maintenanceTypeCode: MaintenanceTypeCode,
): ReminderNotificationPlan? {
    val alerts =
        when (maintenanceTypeCode) {
            MaintenanceTypeCode.Itv ->
                listOf(
                    ReminderAlert(ReminderAlertKind.Itv60Days, 60),
                    ReminderAlert(ReminderAlertKind.Itv30Days, 30),
                    ReminderAlert(ReminderAlertKind.Itv7Days, 7),
                )
            MaintenanceTypeCode.Insurance ->
                listOf(
                    ReminderAlert(ReminderAlertKind.Insurance45Days, 45),
                    ReminderAlert(ReminderAlertKind.Insurance37Days, 37),
                    ReminderAlert(ReminderAlertKind.Insurance7Days, 7),
                )
            else -> return null
        }
    return ReminderNotificationPlan(reminder, alerts)
}

fun maintenanceReminderId(recordId: MaintenanceRecordId): ReminderId = ReminderId("maintenance-reminder:${recordId.value}")

fun plannedMaintenanceReminderId(recordId: MaintenanceRecordId): ReminderId = ReminderId("planned-maintenance-reminder:${recordId.value}")

fun reminderAlertIdentity(
    reminderId: ReminderId,
    alertKind: ReminderAlertKind,
): String = "${reminderId.value}:${alertKind.name}"
