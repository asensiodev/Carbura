package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.model.FamilyId

internal interface LocalSyncDataSource {
    suspend fun getPendingVehicles(): List<SyncVehicle>

    suspend fun getPendingMaintenanceRecords(): List<SyncMaintenanceRecord>

    suspend fun getPendingReminders(): List<SyncReminder>

    suspend fun getVehicles(familyId: FamilyId): List<SyncVehicle>

    suspend fun getMaintenanceRecords(familyId: FamilyId): List<SyncMaintenanceRecord>

    suspend fun getReminders(familyId: FamilyId): List<SyncReminder>

    suspend fun upsertSyncedVehicle(vehicle: SyncVehicle)

    suspend fun upsertSyncedMaintenanceRecord(record: SyncMaintenanceRecord)

    suspend fun upsertSyncedReminder(reminder: SyncReminder)

    suspend fun markVehicleSynced(id: String)

    suspend fun markMaintenanceRecordSynced(id: String)

    suspend fun markReminderSynced(id: String)

    suspend fun adoptLegacyLocalFamily(familyId: FamilyId)
}

internal class SqlDelightLocalSyncDataSource(
    private val database: CarburaDatabase,
) : LocalSyncDataSource {
    override suspend fun getPendingVehicles(): List<SyncVehicle> =
        database.carburaDatabaseQueries
            .selectPendingSyncVehicles()
            .executeAsList()
            .map { it.toSyncVehicle() }

    override suspend fun getPendingMaintenanceRecords(): List<SyncMaintenanceRecord> =
        database.carburaDatabaseQueries
            .selectPendingSyncMaintenanceRecords()
            .executeAsList()
            .map { it.toSyncMaintenanceRecord() }

    override suspend fun getPendingReminders(): List<SyncReminder> =
        database.carburaDatabaseQueries
            .selectPendingSyncReminders()
            .executeAsList()
            .map { it.toSyncReminder() }

    override suspend fun getVehicles(familyId: FamilyId): List<SyncVehicle> =
        database.carburaDatabaseQueries
            .selectSyncVehiclesByFamily(familyId.value)
            .executeAsList()
            .map { it.toSyncVehicle() }

    override suspend fun getMaintenanceRecords(familyId: FamilyId): List<SyncMaintenanceRecord> =
        database.carburaDatabaseQueries
            .selectSyncMaintenanceRecordsByFamily(
                familyId.value,
            ).executeAsList()
            .map { it.toSyncMaintenanceRecord() }

    override suspend fun getReminders(familyId: FamilyId): List<SyncReminder> =
        database.carburaDatabaseQueries
            .selectSyncRemindersByFamily(familyId.value)
            .executeAsList()
            .map { it.toSyncReminder() }

    override suspend fun upsertSyncedVehicle(vehicle: SyncVehicle) {
        database.carburaDatabaseQueries.upsertVehicle(
            id = vehicle.id,
            familyId = vehicle.familyId,
            name = vehicle.name,
            type = vehicle.type.name,
            brand = vehicle.brand,
            model = vehicle.model,
            licensePlate = vehicle.licensePlate,
            currentOdometerKm = vehicle.currentOdometerKm.toLong(),
            nextItvDate = vehicle.nextItvDate,
            insuranceRenewalDate = vehicle.insuranceRenewalDate,
            nextServiceOdometerKm = vehicle.nextServiceOdometerKm?.toLong(),
            updatedAt = vehicle.updatedAt,
            pendingSync = 0,
            deletedAt = vehicle.deletedAt,
        )
    }

    override suspend fun upsertSyncedMaintenanceRecord(record: SyncMaintenanceRecord) {
        database.carburaDatabaseQueries.upsertMaintenanceRecord(
            id = record.id,
            familyId = record.familyId,
            vehicleId = record.vehicleId,
            maintenanceTypeId = record.maintenanceTypeId,
            maintenanceTypeCode = record.maintenanceTypeCode,
            performedOn = record.performedOn,
            odometerKm = record.odometerKm?.toLong(),
            costCents = record.costCents?.toLong(),
            currency = record.currency,
            workshop = record.workshop,
            notes = record.notes,
            nextDueDate = record.nextDueDate,
            updatedAt = record.updatedAt,
            pendingSync = 0,
            deletedAt = record.deletedAt,
        )
    }

    override suspend fun upsertSyncedReminder(reminder: SyncReminder) {
        database.carburaDatabaseQueries.upsertReminder(
            id = reminder.id,
            familyId = reminder.familyId,
            vehicleId = reminder.vehicleId,
            maintenanceTypeId = reminder.maintenanceTypeId,
            title = reminder.title,
            dueDate = reminder.dueDate,
            dueOdometerKm = reminder.dueOdometerKm?.toLong(),
            notifyDaysBefore = reminder.notifyDaysBefore.toLong(),
            isCompleted = if (reminder.isCompleted) 1 else 0,
            updatedAt = reminder.updatedAt,
            pendingSync = 0,
            deletedAt = reminder.deletedAt,
        )
    }

    override suspend fun markVehicleSynced(id: String) {
        database.carburaDatabaseQueries.markVehicleSynced(id)
    }

    override suspend fun markMaintenanceRecordSynced(id: String) {
        database.carburaDatabaseQueries.markMaintenanceRecordSynced(id)
    }

    override suspend fun markReminderSynced(id: String) {
        database.carburaDatabaseQueries.markReminderSynced(id)
    }

    override suspend fun adoptLegacyLocalFamily(familyId: FamilyId) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.adoptLocalVehiclesFamily(familyId.value, now)
            database.carburaDatabaseQueries.adoptLocalMaintenanceRecordsFamily(familyId.value, now)
            database.carburaDatabaseQueries.adoptLocalRemindersFamily(familyId.value, now)
        }
    }
}
