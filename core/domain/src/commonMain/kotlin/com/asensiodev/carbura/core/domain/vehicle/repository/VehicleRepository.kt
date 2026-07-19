package com.asensiodev.carbura.core.domain.vehicle.repository

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

interface VehicleRepository {
    suspend fun observeVehicles(familyId: FamilyId): List<Vehicle>

    suspend fun saveVehicle(vehicle: Vehicle)

    suspend fun saveVehicleWithNotifications(
        vehicle: Vehicle,
        mutations: List<ReminderNotificationMutation>,
    ) {
        saveVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicleId: VehicleId)

    suspend fun deleteVehicleWithNotifications(vehicleId: VehicleId) {
        deleteVehicle(vehicleId)
    }
}
