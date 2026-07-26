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
    val nextItvDate: CalendarDate? = null,
    val insuranceRenewalDate: CalendarDate? = null,
    val nextServiceOdometerKm: Int? = null,
)

enum class VehicleType {
    Car,
    Motorcycle,
    Van,
    Other,
}
