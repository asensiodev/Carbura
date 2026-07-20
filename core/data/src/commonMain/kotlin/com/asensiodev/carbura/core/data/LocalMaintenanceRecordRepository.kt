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

class LocalMaintenanceRecordRepository(
    private val database: CarburaDatabase,
    private val notificationRecovery: NotificationOutboxRecovery = NoOpNotificationOutboxRecovery,
) : MaintenanceRecordRepository {
    private val notificationOutbox = SqlDelightNotificationOutbox(database)
    private val reminderMutations = SqlDelightReminderMutations(database, notificationOutbox)
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        val now = currentTimeMillis()
        saveMaintenanceRecord(record, now)
    }

    override suspend fun saveMaintenanceRecordWithNotification(
        record: MaintenanceRecord,
        mutation: ReminderNotificationMutation,
    ) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            saveMaintenanceRecord(record, now)
            reminderMutations.apply(mutation, now)
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

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> =
        database.carburaDatabaseQueries
            .selectMaintenanceRecordsByVehicle(vehicleId.value)
            .executeAsList()
            .map { it.toMaintenanceRecord() }

    override suspend fun getActiveMaintenanceRecord(
        recordId: MaintenanceRecordId,
        familyId: FamilyId,
        vehicleId: VehicleId,
    ): MaintenanceRecord? =
        database.carburaDatabaseQueries
            .selectActiveMaintenanceRecord(recordId.value, familyId.value, vehicleId.value)
            .executeAsOneOrNull()
            ?.toMaintenanceRecord()

    override suspend fun updateMaintenanceRecordWithNotifications(
        record: MaintenanceRecord,
        expectedFamilyId: FamilyId,
        expectedVehicleId: VehicleId,
        mutations: List<ReminderNotificationMutation>,
    ): Boolean {
        val now = currentTimeMillis()
        var updated = false
        database.carburaDatabaseQueries.transaction {
            updated =
                database.carburaDatabaseQueries
                    .selectActiveMaintenanceRecord(record.id.value, expectedFamilyId.value, expectedVehicleId.value)
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
                    familyId = expectedFamilyId.value,
                    vehicleId = expectedVehicleId.value,
                )
                mutations.forEach { reminderMutations.apply(it, now) }
            }
        }
        if (updated) notificationRecovery.request()
        return updated
    }

    override suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.deleteMaintenanceRecord(
            deletedAt = now,
            updatedAt = now,
            id = recordId.value,
        )
    }

    override suspend fun deleteMaintenanceRecordWithNotifications(
        recordId: MaintenanceRecordId,
        reminderIds: List<ReminderId>,
    ) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            reminderIds.forEach { reminderId ->
                database.carburaDatabaseQueries.deleteReminder(
                    deletedAt = now,
                    updatedAt = now,
                    id = reminderId.value,
                )
                notificationOutbox.recordCancel(reminderId)
            }
            database.carburaDatabaseQueries.deleteMaintenanceRecord(
                deletedAt = now,
                updatedAt = now,
                id = recordId.value,
            )
        }
        notificationRecovery.request()
    }
}
