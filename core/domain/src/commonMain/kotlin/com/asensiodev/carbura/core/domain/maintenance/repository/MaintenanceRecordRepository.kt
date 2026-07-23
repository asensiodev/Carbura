package com.asensiodev.carbura.core.domain.maintenance.repository

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

interface MaintenanceRecordRepository {
    suspend fun saveMaintenanceRecord(
        scope: ActiveFamilyScope,
        record: MaintenanceRecord,
    )

    suspend fun saveMaintenanceRecordWithNotification(
        scope: ActiveFamilyScope,
        record: MaintenanceRecord,
        mutation: ReminderNotificationMutation,
    ) = saveMaintenanceRecord(scope, record)

    suspend fun getVehicleHistory(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): List<MaintenanceRecord>

    suspend fun getActiveMaintenanceRecord(
        recordId: MaintenanceRecordId,
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): MaintenanceRecord? = getVehicleHistory(scope, vehicleId).firstOrNull { it.id == recordId }

    suspend fun updateMaintenanceRecordWithNotifications(
        record: MaintenanceRecord,
        scope: ActiveFamilyScope,
        expectedVehicleId: VehicleId,
        mutations: List<ReminderNotificationMutation>,
    ): Boolean = false

    suspend fun deleteMaintenanceRecord(
        scope: ActiveFamilyScope,
        recordId: MaintenanceRecordId,
    )

    suspend fun deleteMaintenanceRecordWithNotifications(
        scope: ActiveFamilyScope,
        recordId: MaintenanceRecordId,
        reminderIds: List<ReminderId>,
    ) = deleteMaintenanceRecord(scope, recordId)
}
