package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.NoOpNotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway

class LocalMaintenanceRecordRepository(
    private val database: CarburaDatabase,
    private val notificationRecovery: NotificationOutboxRecovery = NoOpNotificationOutboxRecovery,
    private val familyScope: ActiveFamilyScopeGateway = SqlDelightActiveFamilyScopeGateway(database),
) : MaintenanceRecordRepository {
    private val notificationOutbox = SqlDelightNotificationOutbox(database)
    private val reminderMutations = SqlDelightReminderMutations(database, notificationOutbox)
    override suspend fun saveMaintenanceRecord(scope: ActiveFamilyScope, record: MaintenanceRecord) {
        requireScope(scope, record.familyId)
        val now = currentTimeMillis()
        saveMaintenanceRecord(record, now)
    }

    override suspend fun saveMaintenanceRecordWithNotification(
        scope: ActiveFamilyScope,
        record: MaintenanceRecord,
        mutation: ReminderNotificationMutation,
    ) {
        requireScope(scope, record.familyId)
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            saveMaintenanceRecord(record, now)
            familyScope.requireCurrent(scope)
            reminderMutations.apply(scope, mutation, now)
        }
        notificationRecovery.request()
    }

    private fun saveMaintenanceRecord(
        record: MaintenanceRecord,
        now: Long,
    ) {
        database.carburaDatabaseQueries.upsertMaintenanceRecord(
            id = record.id.value,
            familyId = record.familyId.value,
            vehicleId = record.vehicleId.value,
            maintenanceTypeId = record.maintenanceTypeId.value,
            maintenanceTypeCode = record.maintenanceTypeCode?.name,
            maintenanceTypeLabel = record.maintenanceTypeLabel,
            performedOn = record.performedOn.iso8601,
            odometerKm = record.odometerKm?.toLong(),
            costCents = record.costCents?.toLong(),
            currency = record.currency,
            workshop = record.workshop,
            notes = record.notes,
            nextDueDate = record.nextDueDate?.iso8601,
            updatedAt = now,
            pendingSync = 1,
            deletedAt = null,
        )
    }

    override suspend fun getVehicleHistory(scope: ActiveFamilyScope, vehicleId: VehicleId): List<MaintenanceRecord> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectMaintenanceRecordsByVehicle(scope.familyId.value, vehicleId.value)
            .executeAsList()
            .map { it.toMaintenanceRecord() }

    override suspend fun getActiveMaintenanceRecord(
        recordId: MaintenanceRecordId,
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): MaintenanceRecord? =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectActiveMaintenanceRecord(recordId.value, scope.familyId.value, vehicleId.value)
            .executeAsOneOrNull()
            ?.toMaintenanceRecord()

    override suspend fun updateMaintenanceRecordWithNotifications(
        record: MaintenanceRecord,
        scope: ActiveFamilyScope,
        expectedVehicleId: VehicleId,
        mutations: List<ReminderNotificationMutation>,
    ): Boolean {
        requireScope(scope, record.familyId)
        val now = currentTimeMillis()
        var updated = false
        database.carburaDatabaseQueries.transaction {
            updated =
                database.carburaDatabaseQueries
                    .selectActiveMaintenanceRecord(record.id.value, scope.familyId.value, expectedVehicleId.value)
                    .executeAsOneOrNull() != null
            if (updated) {
                database.carburaDatabaseQueries.updateMaintenanceRecord(
                    maintenanceTypeId = record.maintenanceTypeId.value,
                    maintenanceTypeCode = record.maintenanceTypeCode?.name,
                    maintenanceTypeLabel = record.maintenanceTypeLabel,
                    performedOn = record.performedOn.iso8601,
                    odometerKm = record.odometerKm?.toLong(),
                    costCents = record.costCents?.toLong(),
                    workshop = record.workshop,
                    notes = record.notes,
                    nextDueDate = record.nextDueDate?.iso8601,
                    updatedAt = now,
                    id = record.id.value,
                    familyId = scope.familyId.value,
                    vehicleId = expectedVehicleId.value,
                )
                familyScope.requireCurrent(scope)
                mutations.forEach { reminderMutations.apply(scope, it, now) }
            }
        }
        if (updated) notificationRecovery.request()
        return updated
    }

    override suspend fun deleteMaintenanceRecord(scope: ActiveFamilyScope, recordId: MaintenanceRecordId) {
        familyScope.requireCurrent(scope)
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.deleteMaintenanceRecord(
            deletedAt = now,
            updatedAt = now,
            id = recordId.value,
            familyId = scope.familyId.value,
        )
    }

    override suspend fun deleteMaintenanceRecordWithNotifications(
        scope: ActiveFamilyScope,
        recordId: MaintenanceRecordId,
        reminderIds: List<ReminderId>,
    ) {
        familyScope.requireCurrent(scope)
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            reminderIds.forEach { reminderId ->
                database.carburaDatabaseQueries.deleteReminder(
                    deletedAt = now,
                    updatedAt = now,
                    id = reminderId.value,
                    familyId = scope.familyId.value,
                )
                notificationOutbox.recordCancel(scope, reminderId)
            }
            database.carburaDatabaseQueries.deleteMaintenanceRecord(
                deletedAt = now,
                updatedAt = now,
                id = recordId.value,
                familyId = scope.familyId.value,
            )
        }
        notificationRecovery.request()
    }

    private fun requireScope(scope: ActiveFamilyScope, familyId: FamilyId) {
        familyScope.requireCurrent(scope)
        require(scope.familyId == familyId)
    }
}
