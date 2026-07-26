package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

internal class FakeVehicleRepository : VehicleRepository {
    val savedVehicles = mutableListOf<Vehicle>()
    val notificationDeletionIds = mutableListOf<VehicleId>()
    val notificationMutations = mutableListOf<ReminderNotificationMutation>()

    override suspend fun observeVehicles(scope: ActiveFamilyScope): List<Vehicle> = savedVehicles.filter { it.familyId == scope.familyId }

    override suspend fun saveVehicle(
        scope: ActiveFamilyScope,
        vehicle: Vehicle,
    ) {
        savedVehicles.removeAll { it.id == vehicle.id }
        savedVehicles += vehicle
    }

    override suspend fun deleteVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ) {
        savedVehicles.removeAll { it.id == vehicleId }
    }

    override suspend fun saveVehicleWithNotifications(
        scope: ActiveFamilyScope,
        vehicle: Vehicle,
        mutations: List<ReminderNotificationMutation>,
    ) {
        saveVehicle(scope, vehicle)
        notificationMutations += mutations
    }

    override suspend fun deleteVehicleWithNotifications(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ) {
        deleteVehicle(scope, vehicleId)
        notificationDeletionIds += vehicleId
    }
}
