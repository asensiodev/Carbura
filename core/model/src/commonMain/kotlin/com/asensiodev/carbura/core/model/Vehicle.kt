package com.asensiodev.carbura.core.model

data class Vehicle(
    val id: VehicleId,
    val familyId: FamilyId,
    val name: String,
    val type: VehicleType,
    val brand: String? = null,
    val model: String? = null,
    val licensePlate: String? = null,
    val currentOdometerKm: Int,
)

enum class VehicleType {
    Car,
    Motorcycle,
    Van,
    Other,
}
