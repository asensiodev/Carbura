package com.asensiodev.carbura.app.shared

import androidx.navigation3.runtime.NavKey
import com.asensiodev.carbura.core.model.VehicleId
import kotlinx.serialization.Serializable

sealed interface CarburaRoute : NavKey {
    @Serializable
    data object Garage : CarburaRoute

    @Serializable
    data class VehicleDetail(
        val vehicleId: String,
    ) : CarburaRoute {
        companion object {
            fun from(vehicleId: VehicleId): VehicleDetail = VehicleDetail(vehicleId.value)
        }
    }

    @Serializable
    data object Reminders : CarburaRoute

    @Serializable
    data object User : CarburaRoute
}
