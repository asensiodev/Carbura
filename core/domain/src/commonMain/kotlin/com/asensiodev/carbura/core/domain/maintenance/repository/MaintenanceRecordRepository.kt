package com.asensiodev.carbura.core.domain.maintenance.repository

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

interface MaintenanceRecordRepository {
    suspend fun saveMaintenanceRecord(record: MaintenanceRecord)

    suspend fun saveMaintenanceRecordWithNotification(
        record: MaintenanceRecord,
        mutation: ReminderNotificationMutation,
    ) {
        saveMaintenanceRecord(record)
    }

    suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord>

    suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId)

    suspend fun deleteMaintenanceRecordWithNotifications(
        recordId: MaintenanceRecordId,
        reminderIds: List<ReminderId>,
    ) {
        deleteMaintenanceRecord(recordId)
    }
}
