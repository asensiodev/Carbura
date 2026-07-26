package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.model.FamilyId

internal interface RemoteSyncDataSource {
    suspend fun upsertVehicles(vehicles: List<SyncVehicle>)

    suspend fun upsertMaintenanceRecords(records: List<SyncMaintenanceRecord>)

    suspend fun upsertReminders(reminders: List<SyncReminder>)

    suspend fun getVehicles(familyId: FamilyId): List<SyncVehicle>

    suspend fun getMaintenanceRecords(familyId: FamilyId): List<SyncMaintenanceRecord>

    suspend fun getReminders(familyId: FamilyId): List<SyncReminder>
}
