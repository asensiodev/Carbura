package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.data.local.Reminders
import com.asensiodev.carbura.core.domain.ReminderRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

class LocalReminderRepository(
    private val database: CarburaDatabase,
) : ReminderRepository {
    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        database.carburaDatabaseQueries
            .selectPendingRemindersByFamily(familyId.value)
            .executeAsList()
            .map { it.toReminder() }

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> =
        database.carburaDatabaseQueries
            .selectSyncRemindersByVehicle(vehicleId.value)
            .executeAsList()
            .map { it.toReminder() }

    override suspend fun saveReminder(reminder: Reminder) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.upsertReminder(
            id = reminder.id.value,
            familyId = reminder.familyId.value,
            vehicleId = reminder.vehicleId.value,
            maintenanceTypeId = reminder.maintenanceTypeId?.value,
            title = reminder.title,
            dueDate = reminder.dueDate?.iso8601,
            dueOdometerKm = reminder.dueOdometerKm?.toLong(),
            notifyDaysBefore = reminder.notifyDaysBefore.toLong(),
            isCompleted = if (reminder.isCompleted) 1 else 0,
            updatedAt = now,
            pendingSync = 1,
            deletedAt = null,
        )
    }

    override suspend fun markReminderCompleted(reminderId: ReminderId) {
        database.carburaDatabaseQueries.markReminderCompleted(
            updatedAt = currentTimeMillis(),
            id = reminderId.value,
        )
    }

    override suspend fun deleteReminder(reminderId: ReminderId) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.deleteReminder(
            deletedAt = now,
            updatedAt = now,
            id = reminderId.value,
        )
    }
}

private fun Reminders.toReminder(): Reminder = Reminder(
    id = ReminderId(id),
    familyId = FamilyId(familyId),
    vehicleId = VehicleId(vehicleId),
    maintenanceTypeId = maintenanceTypeId?.let(::MaintenanceTypeId),
    title = title,
    dueDate = dueDate?.let(::CalendarDate),
    dueOdometerKm = dueOdometerKm?.toInt(),
    notifyDaysBefore = notifyDaysBefore.toInt(),
    isCompleted = isCompleted == 1L,
)
