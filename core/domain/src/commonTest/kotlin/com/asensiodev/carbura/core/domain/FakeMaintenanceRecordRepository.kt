package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

internal class FakeMaintenanceRecordRepository : MaintenanceRecordRepository {
    val savedRecords = mutableListOf<MaintenanceRecord>()
    var failDeletes = false
    val notificationCancellationIds = mutableListOf<ReminderId>()
    val notificationMutations = mutableListOf<ReminderNotificationMutation>()

    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        savedRecords.removeAll { it.id == record.id }
        savedRecords += record
    }

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> =
        savedRecords.filter { it.vehicleId == vehicleId }

    override suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId) {
        if (failDeletes) error("maintenance delete failed")
        savedRecords.removeAll { it.id == recordId }
    }

    override suspend fun saveMaintenanceRecordWithNotification(
        record: MaintenanceRecord,
        mutation: ReminderNotificationMutation,
    ) {
        saveMaintenanceRecord(record)
        notificationMutations += mutation
    }

    override suspend fun deleteMaintenanceRecordWithNotifications(
        recordId: MaintenanceRecordId,
        reminderIds: List<ReminderId>,
    ) {
        deleteMaintenanceRecord(recordId)
        notificationCancellationIds += reminderIds
    }
}
