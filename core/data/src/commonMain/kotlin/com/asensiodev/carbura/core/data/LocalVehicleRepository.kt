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
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway

class LocalVehicleRepository(
    private val database: CarburaDatabase,
    private val notificationRecovery: NotificationOutboxRecovery = NoOpNotificationOutboxRecovery,
    private val familyScope: ActiveFamilyScopeGateway = SqlDelightActiveFamilyScopeGateway(database),
) : VehicleRepository {
    private val notificationOutbox = SqlDelightNotificationOutbox(database)
    private val reminderMutations = SqlDelightReminderMutations(database, notificationOutbox)
    override suspend fun observeVehicles(scope: ActiveFamilyScope): List<Vehicle> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectVehiclesByFamily(scope.familyId.value)
            .executeAsList()
            .map { it.toVehicle() }

    override suspend fun saveVehicle(scope: ActiveFamilyScope, vehicle: Vehicle) {
        requireScope(scope, vehicle.familyId)
        val now = currentTimeMillis()
        saveVehicle(vehicle, now)
    }

    override suspend fun saveVehicleWithNotifications(
        scope: ActiveFamilyScope,
        vehicle: Vehicle,
        mutations: List<ReminderNotificationMutation>,
    ) {
        requireScope(scope, vehicle.familyId)
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            saveVehicle(vehicle, now)
            familyScope.requireCurrent(scope)
            mutations.forEach { reminderMutations.apply(scope, it, now) }
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

    override suspend fun deleteVehicle(scope: ActiveFamilyScope, vehicleId: VehicleId) {
        familyScope.requireCurrent(scope)
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.deleteMaintenanceRecordsByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
                familyId = scope.familyId.value,
            )
            database.carburaDatabaseQueries.deleteRemindersByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
                familyId = scope.familyId.value,
            )
            database.carburaDatabaseQueries.deleteVehicle(
                deletedAt = now,
                updatedAt = now,
                id = vehicleId.value,
                familyId = scope.familyId.value,
            )
        }
    }

    override suspend fun deleteVehicleWithNotifications(scope: ActiveFamilyScope, vehicleId: VehicleId) {
        familyScope.requireCurrent(scope)
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries
                .selectSyncRemindersByVehicle(scope.familyId.value, vehicleId.value)
                .executeAsList()
                .forEach { notificationOutbox.recordCancel(scope, ReminderId(it.id)) }
            database.carburaDatabaseQueries.deleteMaintenanceRecordsByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
                familyId = scope.familyId.value,
            )
            database.carburaDatabaseQueries.deleteRemindersByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
                familyId = scope.familyId.value,
            )
            database.carburaDatabaseQueries.deleteVehicle(
                deletedAt = now,
                updatedAt = now,
                id = vehicleId.value,
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
