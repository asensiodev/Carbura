package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.reminderAlertIdentity
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ReminderNotificationPlanTest {
    @Test
    fun maintenanceReminderIdentityUsesSourceRecord() {
        assertEquals(
            ReminderId("maintenance-reminder:record-42"),
            maintenanceReminderId(MaintenanceRecordId("record-42")),
        )
    }

    @Test
    fun alertIdentityIncludesLogicalReminderAndTypedAlert() {
        val reminderId = ReminderId("maintenance-reminder:record-42")

        val first = reminderAlertIdentity(reminderId, ReminderAlertKind.Itv60Days)
        val second = reminderAlertIdentity(reminderId, ReminderAlertKind.Itv30Days)

        assertEquals("maintenance-reminder:record-42:Itv60Days", first)
        assertNotEquals(first, second)
    }

    @Test
    fun itvPolicyUses60_30_7Days() {
        val plan = maintenanceReminderNotificationPlan(reminder(), MaintenanceTypeCode.Itv)

        assertEquals(listOf(60, 30, 7), plan?.alerts?.map { it.daysBefore })
    }

    @Test
    fun insurancePolicyUses45_37_7Days() {
        val plan = maintenanceReminderNotificationPlan(reminder(), MaintenanceTypeCode.Insurance)

        assertEquals(listOf(45, 37, 7), plan?.alerts?.map { it.daysBefore })
    }

    @Test
    fun manualPlanRetainsConfiguredSingleAlert() {
        val plan = manualReminderNotificationPlan(reminder(notifyDaysBefore = 14))

        assertEquals(1, plan.alerts.size)
        assertEquals(ReminderAlertKind.Manual, plan.alerts.single().kind)
        assertEquals(14, plan.alerts.single().daysBefore)
    }

    private fun reminder(notifyDaysBefore: Int = 30): Reminder =
        Reminder(
            id = ReminderId("reminder-1"),
            familyId = FamilyId("family-1"),
            vehicleId = VehicleId("vehicle-1"),
            maintenanceTypeId = null,
            title = "Reminder",
            dueDate = CalendarDate("2027-07-01"),
            notifyDaysBefore = notifyDaysBefore,
        )
}
