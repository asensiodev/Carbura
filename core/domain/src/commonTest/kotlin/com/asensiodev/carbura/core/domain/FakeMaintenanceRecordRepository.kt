package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

internal class FakeMaintenanceRecordRepository : MaintenanceRecordRepository {
    val savedRecords = mutableListOf<MaintenanceRecord>()
    var failDeletes = false
    val notificationCancellationIds = mutableListOf<ReminderId>()
    val notificationMutations = mutableListOf<ReminderNotificationMutation>()

    override suspend fun saveMaintenanceRecord(
        scope: ActiveFamilyScope,
        record: MaintenanceRecord,
    ) {
        savedRecords.removeAll { it.id == record.id }
        savedRecords += record
    }

    override suspend fun getVehicleHistory(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): List<MaintenanceRecord> = savedRecords.filter { it.familyId == scope.familyId && it.vehicleId == vehicleId }

    override suspend fun updateMaintenanceRecordWithNotifications(
        record: MaintenanceRecord,
        scope: ActiveFamilyScope,
        expectedVehicleId: VehicleId,
        mutations: List<ReminderNotificationMutation>,
    ): Boolean {
        val index =
            savedRecords.indexOfFirst {
                it.id == record.id && it.familyId == scope.familyId && it.vehicleId == expectedVehicleId
            }
        if (index < 0) return false
        savedRecords[index] = record
        notificationMutations += mutations
        return true
    }

    override suspend fun deleteMaintenanceRecord(
        scope: ActiveFamilyScope,
        recordId: MaintenanceRecordId,
    ) {
        if (failDeletes) error("maintenance delete failed")
        savedRecords.removeAll { it.id == recordId }
    }

    override suspend fun saveMaintenanceRecordWithNotification(
        scope: ActiveFamilyScope,
        record: MaintenanceRecord,
        mutation: ReminderNotificationMutation,
    ) {
        saveMaintenanceRecord(scope, record)
        notificationMutations += mutation
    }

    override suspend fun deleteMaintenanceRecordWithNotifications(
        scope: ActiveFamilyScope,
        recordId: MaintenanceRecordId,
        reminderIds: List<ReminderId>,
    ) {
        deleteMaintenanceRecord(scope, recordId)
        notificationCancellationIds += reminderIds
    }
}
