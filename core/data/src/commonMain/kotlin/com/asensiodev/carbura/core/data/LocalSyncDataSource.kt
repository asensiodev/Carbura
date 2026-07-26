package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.NoOpNotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway

internal interface LocalSyncDataSource {
    suspend fun getPendingVehicles(scope: ActiveFamilyScope): List<SyncVehicle>

    suspend fun getPendingMaintenanceRecords(scope: ActiveFamilyScope): List<SyncMaintenanceRecord>

    suspend fun getPendingReminders(scope: ActiveFamilyScope): List<SyncReminder>

    suspend fun getVehicles(scope: ActiveFamilyScope): List<SyncVehicle>

    suspend fun getMaintenanceRecords(scope: ActiveFamilyScope): List<SyncMaintenanceRecord>

    suspend fun getReminders(scope: ActiveFamilyScope): List<SyncReminder>

    suspend fun upsertSyncedVehicle(scope: ActiveFamilyScope, vehicle: SyncVehicle)

    suspend fun upsertSyncedMaintenanceRecord(scope: ActiveFamilyScope, record: SyncMaintenanceRecord)

    suspend fun upsertSyncedReminder(scope: ActiveFamilyScope, reminder: SyncReminder)

    suspend fun markVehicleSynced(
        scope: ActiveFamilyScope,
        id: String,
        uploadedUpdatedAt: Long,
    )

    suspend fun markMaintenanceRecordSynced(
        scope: ActiveFamilyScope,
        id: String,
        uploadedUpdatedAt: Long,
    )

    suspend fun markReminderSynced(
        scope: ActiveFamilyScope,
        id: String,
        uploadedUpdatedAt: Long,
    )
}

internal class SqlDelightLocalSyncDataSource(
    private val database: CarburaDatabase,
    private val notificationRecovery: NotificationOutboxRecovery = NoOpNotificationOutboxRecovery,
    private val familyScope: ActiveFamilyScopeGateway = SqlDelightActiveFamilyScopeGateway(database),
) : LocalSyncDataSource {
    private val notificationOutbox = SqlDelightNotificationOutbox(database)
    override suspend fun getPendingVehicles(scope: ActiveFamilyScope): List<SyncVehicle> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectPendingSyncVehicles(scope.familyId.value)
            .executeAsList()
            .map { it.toSyncVehicle() }

    override suspend fun getPendingMaintenanceRecords(scope: ActiveFamilyScope): List<SyncMaintenanceRecord> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectPendingSyncMaintenanceRecords(scope.familyId.value)
            .executeAsList()
            .map { it.toSyncMaintenanceRecord() }

    override suspend fun getPendingReminders(scope: ActiveFamilyScope): List<SyncReminder> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectPendingSyncReminders(scope.familyId.value)
            .executeAsList()
            .map { it.toSyncReminder() }

    override suspend fun getVehicles(scope: ActiveFamilyScope): List<SyncVehicle> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectSyncVehiclesByFamily(scope.familyId.value)
            .executeAsList()
            .map { it.toSyncVehicle() }

    override suspend fun getMaintenanceRecords(scope: ActiveFamilyScope): List<SyncMaintenanceRecord> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectSyncMaintenanceRecordsByFamily(
                scope.familyId.value,
            ).executeAsList()
            .map { it.toSyncMaintenanceRecord() }

    override suspend fun getReminders(scope: ActiveFamilyScope): List<SyncReminder> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectSyncRemindersByFamily(scope.familyId.value)
            .executeAsList()
            .map { it.toSyncReminder() }

    override suspend fun upsertSyncedVehicle(scope: ActiveFamilyScope, vehicle: SyncVehicle) {
        requireScope(scope, vehicle.familyId)
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

    override suspend fun upsertSyncedMaintenanceRecord(scope: ActiveFamilyScope, record: SyncMaintenanceRecord) {
        requireScope(scope, record.familyId)
        database.carburaDatabaseQueries.upsertMaintenanceRecord(
            id = record.id,
            familyId = record.familyId,
            vehicleId = record.vehicleId,
            maintenanceTypeId = record.maintenanceTypeId,
            maintenanceTypeCode = record.maintenanceTypeCode,
            maintenanceTypeLabel = record.maintenanceTypeLabel,
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

    override suspend fun upsertSyncedReminder(scope: ActiveFamilyScope, reminder: SyncReminder) {
        requireScope(scope, reminder.familyId)
        database.carburaDatabaseQueries.transaction {
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
            val domainReminder = reminder.toReminder()
            if (reminder.deletedAt == null && !reminder.isCompleted && reminder.dueDate != null) {
                notificationOutbox.recordSchedule(scope, manualReminderNotificationPlan(domainReminder))
            } else {
                notificationOutbox.recordCancel(scope, domainReminder.id)
            }
        }
        notificationRecovery.request()
    }

    override suspend fun markVehicleSynced(
        scope: ActiveFamilyScope,
        id: String,
        uploadedUpdatedAt: Long,
    ) {
        familyScope.requireCurrent(scope)
        database.carburaDatabaseQueries.markVehicleSynced(scope.familyId.value, id, uploadedUpdatedAt)
    }

    override suspend fun markMaintenanceRecordSynced(
        scope: ActiveFamilyScope,
        id: String,
        uploadedUpdatedAt: Long,
    ) {
        familyScope.requireCurrent(scope)
        database.carburaDatabaseQueries.markMaintenanceRecordSynced(scope.familyId.value, id, uploadedUpdatedAt)
    }

    override suspend fun markReminderSynced(
        scope: ActiveFamilyScope,
        id: String,
        uploadedUpdatedAt: Long,
    ) {
        familyScope.requireCurrent(scope)
        database.carburaDatabaseQueries.markReminderSynced(scope.familyId.value, id, uploadedUpdatedAt)
    }

    private fun requireScope(scope: ActiveFamilyScope, familyId: String) {
        familyScope.requireCurrent(scope)
        require(scope.familyId.value == familyId)
    }
}

private fun SyncReminder.toReminder(): Reminder =
    Reminder(
        id = ReminderId(id),
        familyId = FamilyId(familyId),
        vehicleId = VehicleId(vehicleId),
        maintenanceTypeId = maintenanceTypeId?.let(::MaintenanceTypeId),
        title = title,
        dueDate = dueDate?.let(::CalendarDate),
        dueOdometerKm = dueOdometerKm,
        notifyDaysBefore = notifyDaysBefore,
        isCompleted = isCompleted,
    )
