package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

internal class FakeVehicleRepository : VehicleRepository {
    val savedVehicles = mutableListOf<Vehicle>()
    val notificationDeletionIds = mutableListOf<VehicleId>()
    val notificationMutations = mutableListOf<ReminderNotificationMutation>()

    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> = savedVehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        savedVehicles.removeAll { it.id == vehicle.id }
        savedVehicles += vehicle
    }

    override suspend fun deleteVehicle(vehicleId: VehicleId) {
        savedVehicles.removeAll { it.id == vehicleId }
    }

    override suspend fun saveVehicleWithNotifications(
        vehicle: Vehicle,
        mutations: List<ReminderNotificationMutation>,
    ) {
        saveVehicle(vehicle)
        notificationMutations += mutations
    }

    override suspend fun deleteVehicleWithNotifications(vehicleId: VehicleId) {
        deleteVehicle(vehicleId)
        notificationDeletionIds += vehicleId
    }
}
