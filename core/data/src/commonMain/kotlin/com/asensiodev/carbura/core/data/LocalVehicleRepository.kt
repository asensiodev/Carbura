package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.NoOpNotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

class LocalVehicleRepository(
    private val database: CarburaDatabase,
    private val notificationRecovery: NotificationOutboxRecovery = NoOpNotificationOutboxRecovery,
) : VehicleRepository {
    private val notificationOutbox = SqlDelightNotificationOutbox(database)
    private val reminderMutations = SqlDelightReminderMutations(database, notificationOutbox)
    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> =
        database.carburaDatabaseQueries
            .selectVehiclesByFamily(familyId.value)
            .executeAsList()
            .map { it.toVehicle() }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        val now = currentTimeMillis()
        saveVehicle(vehicle, now)
    }

    override suspend fun saveVehicleWithNotifications(
        vehicle: Vehicle,
        mutations: List<ReminderNotificationMutation>,
    ) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            saveVehicle(vehicle, now)
            mutations.forEach { reminderMutations.apply(it, now) }
        }
        notificationRecovery.request()
    }

    private fun saveVehicle(
        vehicle: Vehicle,
        now: Long,
    ) {
        database.carburaDatabaseQueries.upsertVehicle(
            id = vehicle.id.value,
            familyId = vehicle.familyId.value,
            name = vehicle.name,
            type = vehicle.type.name,
            brand = vehicle.brand,
            model = vehicle.model,
            licensePlate = vehicle.licensePlate,
            currentOdometerKm = vehicle.currentOdometerKm.toLong(),
            nextItvDate = vehicle.nextItvDate?.iso8601,
            insuranceRenewalDate = vehicle.insuranceRenewalDate?.iso8601,
            nextServiceOdometerKm = vehicle.nextServiceOdometerKm?.toLong(),
            updatedAt = now,
            pendingSync = 1,
            deletedAt = null,
        )
    }

    override suspend fun deleteVehicle(vehicleId: VehicleId) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.deleteMaintenanceRecordsByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
            )
            database.carburaDatabaseQueries.deleteRemindersByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
            )
            database.carburaDatabaseQueries.deleteVehicle(
                deletedAt = now,
                updatedAt = now,
                id = vehicleId.value,
            )
        }
    }

    override suspend fun deleteVehicleWithNotifications(vehicleId: VehicleId) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries
                .selectSyncRemindersByVehicle(vehicleId.value)
                .executeAsList()
                .forEach { notificationOutbox.recordCancel(ReminderId(it.id)) }
            database.carburaDatabaseQueries.deleteMaintenanceRecordsByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
            )
            database.carburaDatabaseQueries.deleteRemindersByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
            )
            database.carburaDatabaseQueries.deleteVehicle(
                deletedAt = now,
                updatedAt = now,
                id = vehicleId.value,
            )
        }
        notificationRecovery.request()
    }
}
