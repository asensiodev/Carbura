package com.asensiodev.carbura.core.domain.vehicle.repository

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

interface VehicleRepository {
    suspend fun observeVehicles(scope: ActiveFamilyScope): List<Vehicle>

    suspend fun saveVehicle(
        scope: ActiveFamilyScope,
        vehicle: Vehicle,
    )

    suspend fun saveVehicleWithNotifications(
        scope: ActiveFamilyScope,
        vehicle: Vehicle,
        mutations: List<ReminderNotificationMutation>,
    ) = saveVehicle(scope, vehicle)

    suspend fun deleteVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    )

    suspend fun deleteVehicleWithNotifications(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ) = deleteVehicle(scope, vehicleId)
}
